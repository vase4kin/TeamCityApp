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

package teamcityapp.features.tests.impl.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.tests.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.network.testing.AccountNetworkFixture

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityTestsRepositoryTest {
    private class Fixture(scope: TestScope, handler: MockRequestHandler) : AccountNetworkFixture(scope, handler = handler) {
        lateinit var repository: TeamCityTestsRepository
        override suspend fun start(initial: AccountPreferences) {
            super.start(initial)
            repository = TeamCityTestsRepository(account, api)
        }
    }

    @Test fun testFiltersCountsAndOpaquePagesRemainIndependent() = runTest {
        var failCount = true
        val fixture = Fixture(this) { request ->
            if (request.url.parameters["fields"] == "count,passed,failed,ignored") {
                if (failCount) {
                    respond("", HttpStatusCode.InternalServerError)
                } else {
                    respond("""{"count":7,"passed":4,"failed":2,"ignored":1}""")
                }
            } else if (request.url.parameters["cursor"] == "opaque") {
                assertNull(request.url.parameters["locator"])
                respond("""{"testOccurrence":[],"nextHref":"/context/app/rest/loop"}""")
            } else {
                assertEquals("build:(id:7),status:FAILURE,count:10", request.url.parameters["locator"])
                respond("""{"testOccurrence":[{"id":"failure","name":"Test","status":"FAILURE","href":"/context/app/rest/testOccurrences/id:failure"}],"nextHref":"/context/app/rest/testOccurrences?cursor=opaque"}""")
            }
        }
        fixture.start()
        val url = "/context/app/rest/testOccurrences?locator=build:(id:7)"
        val page = fixture.repository.tests(url, TestsFilter.Failed, null, false)
        assertEquals(TestStatus.Failed, page.items.single().status)
        assertFails { fixture.repository.summary(url) }
        assertEquals(page, fixture.repository.tests(url, TestsFilter.Failed, null, false))
        assertNull(fixture.repository.tests(url, TestsFilter.Passed, page.nextHref, false).nextHref)
        failCount = false
        assertEquals(TestsSummary(7, TestsCounts(4, 2, 1)), fixture.repository.summary(url))
    }

    @Test fun continuationCannotEscapeAccountOrigin() = runTest {
        val fixture = Fixture(this) { error("Must reject before HTTP") }
        fixture.start()
        assertFails { fixture.repository.tests("/context/app/rest/tests", TestsFilter.Failed, "https://evil.example/steal", false) }
        assertTrue(fixture.urls.isEmpty())
    }
}
