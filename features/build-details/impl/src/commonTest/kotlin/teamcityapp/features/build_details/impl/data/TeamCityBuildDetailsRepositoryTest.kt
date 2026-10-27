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

package teamcityapp.features.build_details.impl.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.network.testing.AccountNetworkFixture

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityBuildDetailsRepositoryTest {
    private class Fixture(scope: TestScope, handler: MockRequestHandler) : AccountNetworkFixture(scope, AccountIdentity("https://teamcity.example/context/", "alice", false), handler) {
        lateinit var repository: TeamCityBuildDetailsRepository
        override suspend fun start(initial: AccountPreferences) {
            super.start(initial)
            repository = TeamCityBuildDetailsRepository(account, api)
        }
    }

    @Test fun restartUsesConfirmedConfigurationBranchAndParameters() = runTest {
        val fixture = Fixture(this) { respond("""{"href":"/context/app/rest/buildQueue/id:44"}""") }
        fixture.start()
        val build = BuildLaunchData("42", "/context/app/rest/builds/id:42", state = "finished", buildTypeId = "Latest", branchName = "release", properties = BuildProperties(listOf(BuildProperty("env.KEY", "value"))))
        assertIs<BuildDetailsMutation.Success<String>>(fixture.repository.restart(build))
        val body = Json.parseToJsonElement((fixture.requests.single().body as TextContent).text).jsonObject
        assertEquals("Latest", body["buildType"]!!.jsonObject["id"]!!.jsonPrimitive.content)
        assertEquals("release", body["branchName"]!!.jsonPrimitive.content)
        assertFalse("triggeringOptions" in body)
    }

    @Test fun cancellationResolvesFullSnapshotAndOnlyRunningCanReadd() = runTest {
        val fixture = Fixture(this) { request ->
            if (request.method == HttpMethod.Post) {
                respond("""{"href":"/context/app/rest/builds/id:42"}""")
            } else {
                respond("""{"id":"42","href":"/context/app/rest/builds/id:42","number":"42","state":"finished","buildTypeId":"Build","buildType":{"id":"Build","name":"Verify"}}""")
            }
        }
        fixture.start()
        val result = fixture.repository.cancel(BuildLaunchData("42", "/context/app/rest/buildQueue/id:42", state = "queued"), true)
        assertIs<BuildDetailsMutation.Success<BuildLaunchData>>(result)
        assertEquals("Verify", result.value.configuration?.name)
        assertFalse(Json.parseToJsonElement((fixture.requests.first().body as TextContent).text).jsonObject["readdIntoQueue"]!!.jsonPrimitive.boolean)
        assertTrue(fixture.requests.last().url.parameters["fields"]!!.contains("properties"))
    }

    @Test fun foreignMutationUrlsAreRejectedBeforeNetworkAccess() = runTest {
        val fixture = Fixture(this) { error("Must not reach engine") }
        fixture.start()
        assertEquals(BuildDetailsMutation.Failed, fixture.repository.cancel(BuildLaunchData("42", "https://foreign.example/app/rest/builds/id:42", state = "running"), true))
        assertTrue(fixture.requests.isEmpty())
    }
}
