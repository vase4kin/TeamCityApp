/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.di.modules

import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.ResponseBody
import okio.Buffer
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.libraries.accounts.AccountIdentity
import teamcityapp.libraries.accounts.AccountSecret
import teamcityapp.libraries.network.*

class SharedMockClientTest {
    private val service = mock<TeamCityService>()
    private fun api() = TeamCityApi(
        TeamCityTransport(
            sharedMockClient { service },
            AccountSecret(AccountIdentity("https://fixture.example/context/", "Guest user", true))
        )
    )

    @Test fun fixtureResponsesGoThroughTheCommonKtorParser() = runBlocking<Unit> {
        doReturn(ServerInfo("fixture-version", "https://fixture.example/context/")).whenever(service) { serverInfo() }
        val api = api()
        try {
            assertEquals("fixture-version", api.server().version)
            verifyBlocking(service, times(1)) { serverInfo() }
        } finally {
            api.close()
        }
    }

    @Test fun httpFailureKeepsItsStatusWithoutReplayingTheFixture() = runBlocking<Unit> {
        doSuspendableAnswer { throw MockHttpFailure(403) }.whenever(service) { serverInfo() }
        val api = api()
        try {
            try {
                api.server()
                fail("Expected HTTP failure")
            } catch (error: TeamCityHttpFailure) {
                assertEquals(403, error.status)
            }
            verifyBlocking(service, times(1)) { serverInfo() }
        } finally {
            api.close()
        }
    }

    @Test fun cancellingAKtorRequestCancelsThePendingFixtureWaiter() = runBlocking<Unit> {
        val pending = PendingFixture<ServerInfo>()
        val subscribed = CompletableDeferred<Unit>()
        val disposed = CompletableDeferred<Unit>()
        doSuspendableAnswer {
            subscribed.complete(Unit)
            try {
                pending.await()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                disposed.complete(Unit)
                throw cancelled
            }
        }.whenever(service) { serverInfo() }
        val api = api()
        try {
            val request = async { api.server() }
            withTimeout(5_000) { subscribed.await() }
            request.cancel()
            request.join()
            withTimeout(5_000) { disposed.await() }
        } finally {
            api.close()
        }
    }

    @Test fun encodedCollectionQueriesReachTheLegacyFixtureWithDecodedValues() = runBlocking<Unit> {
        val href = "/context/guestAuth/app/rest/testOccurrences?locator=build:(id:1),count:2147483647&fields=count,passed,failed,ignored"
        doReturn(TestOccurrences(16)).whenever(service) { listTestOccurrences(href) }
        val api = api()
        try {
            assertEquals(16, api.document(href).count())
            verifyBlocking(service, times(1)) { listTestOccurrences(href) }
        } finally {
            api.close()
        }
    }

    @Test fun cancellationPayloadMapsTheWireFlagToTheLegacyFixtureContract() = runBlocking<Unit> {
        val path = "/context/guestAuth/app/rest/builds/id:1"
        val api = api()
        try {
            for (reAdd in listOf(true, false)) {
                doReturn(Build()).whenever(service) { cancelBuild(path, BuildCancelRequest(reAdd)) }
                api.mutate(path, buildJsonObject { put("readdIntoQueue", reAdd) })
                verifyBlocking(service, times(1)) { cancelBuild(path, BuildCancelRequest(reAdd)) }
            }
        } finally {
            api.close()
        }
    }

    @Test fun cancellingABlockedDownloadClosesItsFixtureBody() = runBlocking<Unit> {
        val entered = CompletableDeferred<Unit>()
        val closed = CompletableDeferred<Unit>()
        val release = java.util.concurrent.CountDownLatch(1)
        val body = object : ResponseBody() {
            private val source = object : Source {
                override fun timeout() = Timeout.NONE
                override fun read(sink: Buffer, byteCount: Long): Long {
                    entered.complete(Unit)
                    check(release.await(10, java.util.concurrent.TimeUnit.SECONDS))
                    return -1L
                }
                override fun close() {
                    closed.complete(Unit)
                    release.countDown()
                }
            }.buffer()
            override fun contentType(): okhttp3.MediaType? = null
            override fun contentLength() = -1L
            override fun source() = source
        }
        doReturn(body).whenever(service) { downloadFile(any()) }
        val api = api()
        try {
            val request = async(Dispatchers.Default) { api.stream("/context/guestAuth/app/rest/builds/id:1/artifacts/content/file.txt", { _, _ -> }, { _, _ -> }) }
            withTimeout(5_000) { entered.await() }
            request.cancel()
            withTimeout(5_000) {
                closed.await()
                request.join()
            }
        } finally {
            release.countDown()
            api.close()
        }
    }
}
