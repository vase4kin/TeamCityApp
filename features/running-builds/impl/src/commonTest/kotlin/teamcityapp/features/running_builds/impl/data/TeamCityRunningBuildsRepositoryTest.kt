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

package teamcityapp.features.running_builds.impl.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.running_builds.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.network.testing.AccountNetworkFixture

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityRunningBuildsRepositoryTest {
    private class Fixture(scope: TestScope, handler: MockRequestHandler) : AccountNetworkFixture(scope, handler = handler) {
        lateinit var repository: TeamCityRunningBuildsRepository
        override suspend fun start(initial: AccountPreferences) {
            super.start(initial)
            repository = TeamCityRunningBuildsRepository(account, api, preferences)
        }
    }

    @Test fun monitoringUsesBulkProjectionAndFollowsOpaqueContinuationWithoutDetailRequests() = runTest {
        val fixture = Fixture(this) { request ->
            when (request.url.encodedPath) {
                "/context/guestAuth/app/rest/builds" -> {
                    assertEquals("running:true,branch:default:any,personal:false,pinned:false", request.url.parameters["locator"])
                    assertTrue(request.url.parameters["fields"]!!.contains("properties"))
                    respond("""{"build":[{"id":"1","href":"/context/app/rest/builds/id:1","buildTypeId":"C"}],"nextHref":"/context/app/rest/opaque?cursor=a%2Bb%3D&extra=x"}""")
                }

                "/context/guestAuth/app/rest/opaque" -> {
                    assertEquals("a+b=", request.url.parameters["cursor"])
                    assertEquals("x", request.url.parameters["extra"])
                    respond("""{"build":[{"id":"2","href":"/context/app/rest/builds/id:2","buildTypeId":"C"}]}""")
                }

                else -> error("Unexpected detail request: ${request.url}")
            }
        }
        fixture.start(AccountPreferences(runningFavorites = false))
        assertEquals(listOf("1", "2"), fixture.repository.builds(fixture.repository.query.first(), false).map { it.id })
        assertEquals(2, fixture.urls.size)
    }

    @Test fun accountReplacementCancelsHydrationAndPreventsOldQueryRequests() = runTest {
        val entered = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val fixture = Fixture(this) {
            entered.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                cancelled.complete(Unit)
            }
        }
        fixture.start(AccountPreferences(runningFavorites = false))
        val load = async { fixture.repository.builds(fixture.repository.query.first(), true) }
        entered.await()
        fixture.sessions.saveAndActivate(AccountSecret(fixture.identity.copy(userName = "other")))
        assertFailsWith<CancellationException> { load.await() }
        cancelled.await()
        assertFailsWith<CancellationException> { fixture.repository.builds(RunningBuildsQuery(fixture.identity.key, RunningBuildsFilter.All), true) }
        assertEquals(1, fixture.urls.size)
    }
}
