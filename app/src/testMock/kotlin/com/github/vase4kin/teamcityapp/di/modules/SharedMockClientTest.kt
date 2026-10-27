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
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
        whenever(service.serverInfo()).thenReturn(Single.just(ServerInfo("fixture-version", "https://fixture.example/context/")))
        val api = api()
        try {
            assertEquals("fixture-version", api.server().version)
            verify(service, times(1)).serverInfo()
        } finally {
            api.close()
        }
    }

    @Test fun httpFailureKeepsItsStatusWithoutReplayingTheFixture() = runBlocking<Unit> {
        whenever(service.serverInfo()).thenReturn(Single.error(MockHttpFailure(403)))
        val api = api()
        try {
            try {
                api.server()
                fail("Expected HTTP failure")
            } catch (error: TeamCityHttpFailure) {
                assertEquals(403, error.status)
            }
            verify(service, times(1)).serverInfo()
        } finally {
            api.close()
        }
    }

    @Test fun cancellingAKtorRequestDisposesThePendingFixtureSubscription() = runBlocking<Unit> {
        val pending = SingleSubject.create<ServerInfo>()
        val subscribed = CompletableDeferred<Unit>()
        val disposed = CompletableDeferred<Unit>()
        whenever(service.serverInfo()).thenReturn(pending.doOnSubscribe { subscribed.complete(Unit) }.doOnDispose { disposed.complete(Unit) })
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
}
