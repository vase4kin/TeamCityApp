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

package teamcityapp.shared

import kotlin.test.*
import kotlin.uuid.Uuid
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import teamcityapp.features.build_details.api.BuildDetailsMutation
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

/** Opt-in local fixture checks; real TeamCity instances are never mutated. */
class SharedRepositoryPlatformTest {
    @Test fun realEnginePreservesManagementPayloadsAndLoadsFullCancellationSnapshots() = runTest {
        val identity = AccountIdentity("http://127.0.0.1:18081/context/manage-library-${Uuid.random()}/", "alice", false)
        val sessions = AccountSessionManager(
            VaultAccountStorage(object : SecureVault {
                private var bytes: String? = null
                override suspend fun read() = bytes
                override suspend fun write(value: String) {
                    bytes = value
                }
            }),
            backgroundScope.coroutineContext
        )
        sessions.saveAndActivate(AccountSecret(identity, "fixture-only-password"))
        val account = (sessions.state.value as AccountSessionState.Active).scope
        val api = TeamCityApi(TeamCityTransport(platformTeamCityClient(false), sessions.secret(account), httpConsent = true))
        val features = sharedRepositoryFixture(account, api, AccountPreferences(), "fixture-launch") { sessions.updatePreferences(account, it) }
        try {
            assertEquals(listOf("main", "release"), features.filters.branches("Demo_Build"))
            assertEquals(listOf("main", "release"), features.run.branches("Demo_Build"))
            assertEquals(3, features.run.agents("Demo_Build").size)
            val request = BuildRequest("Demo_Build", "release", BuildAgent("1", "Linux"), true, true, false, listOf(BuildParameter("env.X", ""), BuildParameter("env.X", "two")))
            val queued = assertIs<QueueBuildResult.Success>(features.run.queue(request))
            val build = features.details.queuedBuild(queued.href)
            assertEquals("queued", build.state)
            assertEquals("release", build.branchName)
            assertTrue(features.details.triggeredByCurrentUser(build))
            assertIs<BuildDetailsMutation.Success<String>>(features.details.restart(build))
            val running = features.overview.build(api.apiPath("builds/id:3"), true)
            val cancelled = assertIs<BuildDetailsMutation.Success<teamcityapp.libraries.builds.BuildLaunchData>>(features.details.cancel(running, true)).value
            assertEquals("finished", cancelled.state)
            assertEquals("Canceled by fixture user", cancelled.statusText)
            assertNotNull(cancelled.configuration)
            val mutations = api.document(api.apiPath("fixtureMutations"), true).items("mutations")
            assertEquals(listOf("run", "run", "cancel"), mutations.map { it.text("action") })
            val body = mutations[0]["body"]!!.jsonObject
            assertEquals(true, body["personal"]!!.jsonPrimitive.boolean)
            assertEquals(true, body["triggeringOptions"]!!.jsonObject["queueAtTop"]!!.jsonPrimitive.boolean)
            assertEquals(false, body["triggeringOptions"]!!.jsonObject["cleanSources"]!!.jsonPrimitive.boolean)
            assertEquals(listOf("", "two"), body["properties"]!!.jsonObject["property"]!!.jsonArray.map { it.jsonObject["value"]!!.jsonPrimitive.content })
            assertEquals(true, mutations[2]["body"]!!.jsonObject["readdIntoQueue"]!!.jsonPrimitive.boolean)
        } finally {
            api.close()
        }
    }
}
