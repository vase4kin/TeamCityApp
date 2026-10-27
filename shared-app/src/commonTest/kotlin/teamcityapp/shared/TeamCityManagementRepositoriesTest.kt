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

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.request.HttpRequestData
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.run_build.api.*
import teamcityapp.features.run_build.api.BuildAgent as RequestAgent
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityManagementRepositoriesTest {
    private class Vault : SecureVault {
        var value: String? = null
        override suspend fun read() = value
        override suspend fun write(value: String) {
            this.value = value
        }
    }
    private class Fixture(val scope: TestScope, private val handler: MockRequestHandler) {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), scope.backgroundScope.coroutineContext)
        val identity = AccountIdentity("https://teamcity.example/context/", "alice", false)
        lateinit var repos: SharedAccountFeatures
        lateinit var api: TeamCityApi
        val requests = mutableListOf<HttpRequestData>()
        suspend fun start() {
            sessions.saveAndActivate(AccountSecret(identity, "fixture"))
            val account = (sessions.state.value as AccountSessionState.Active).scope
            api = TeamCityApi(
                TeamCityTransport(
                    HttpClient(
                        MockEngine(
                            MockEngineConfig().apply {
                                dispatcher = Dispatchers.Unconfined
                                addHandler { request ->
                                    requests += request
                                    handler(request)
                                }
                            }
                        )
                    ) { followRedirects = false },
                    AccountSecret(identity, "fixture")
                )
            )
            repos = sharedRepositoryFixture(account, api, AccountPreferences(), "launch-one") { sessions.updatePreferences(account, it) }
        }
    }

    @Test fun queueIsOnePostReturnsAReferenceAndInvalidatesCachedReads() = runTest {
        val fixture = Fixture(this) { request ->
            if (request.method == HttpMethod.Post) respond("""{"id":"44","href":"/context/app/rest/buildQueue/id:44"}""") else respond("""{"branch":[{"name":"main"}]}""")
        }
        fixture.start()
        fixture.repos.run.branches("Build")
        fixture.repos.run.branches("Build")
        assertEquals(1, fixture.requests.size)
        assertEquals(QueueBuildResult.Success("/context/app/rest/buildQueue/id:44"), fixture.repos.run.queue(BuildRequest("Build")))
        fixture.repos.run.branches("Build")
        assertEquals(listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Get), fixture.requests.map { it.method })
        val post = fixture.requests[1]
        assertTrue(post.url.encodedPath.endsWith("/httpAuth/app/rest/buildQueue"))
        assertEquals(ContentType.Application.Json.toString(), post.body.contentType.toString())
    }

    @Test fun forbiddenAndServerErrorsDoNotRetryOrExposeServerText() = runTest {
        for ((code, result) in listOf(HttpStatusCode.Forbidden to QueueBuildResult.Forbidden, HttpStatusCode.InternalServerError to QueueBuildResult.Error)) {
            val fixture = Fixture(this) { respond("private diagnostics", code) }
            fixture.start()
            assertEquals(result, fixture.repos.run.queue(BuildRequest("Build")))
            assertEquals(1, fixture.requests.size)
        }
    }

    @Test fun mutationBasicChallengeReplaysTheIdenticalBodyOnlyOnce() = runTest {
        var calls = 0
        val fixture = Fixture(this) { request ->
            calls++
            if (calls == 1) {
                respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Basic realm=\"TeamCity\""))
            } else {
                respond("""{"href":"/context/app/rest/buildQueue/id:44"}""")
            }
        }
        fixture.start()
        assertIs<QueueBuildResult.Success>(fixture.repos.run.queue(BuildRequest("Build")))
        assertEquals(2, calls)
        assertNull(fixture.requests[0].headers[HttpHeaders.Authorization])
        assertNotNull(fixture.requests[1].headers[HttpHeaders.Authorization])
        assertEquals((fixture.requests[0].body as TextContent).text, (fixture.requests[1].body as TextContent).text)
    }

    @Test fun foreignMutationUrlsAreRejectedBeforeNetworkAccess() = runTest {
        val fixture = Fixture(this) { error("Must not reach engine") }
        fixture.start()
        assertEquals(BuildDetailsMutation.Failed, fixture.repos.details.cancel(BuildLaunchData("42", "https://foreign.example/app/rest/builds/id:42", state = "running"), true))
        assertTrue(fixture.requests.isEmpty())
    }

    @Test fun restartUsesConfirmedConfigurationBranchAndParameters() = runTest {
        val fixture = Fixture(this) { respond("""{"href":"/context/app/rest/buildQueue/id:44"}""") }
        fixture.start()
        val build = BuildLaunchData("42", "/context/app/rest/builds/id:42", state = "finished", buildTypeId = "Latest", branchName = "release", properties = BuildProperties(listOf(BuildProperty("env.KEY", "value"))))
        assertIs<BuildDetailsMutation.Success<String>>(fixture.repos.details.restart(build))
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
        val result = fixture.repos.details.cancel(BuildLaunchData("42", "/context/app/rest/buildQueue/id:42", state = "queued"), true)
        assertIs<BuildDetailsMutation.Success<BuildLaunchData>>(result)
        assertEquals("Verify", result.value.configuration?.name)
        assertFalse(Json.parseToJsonElement((fixture.requests.first().body as TextContent).text).jsonObject["readdIntoQueue"]!!.jsonPrimitive.boolean)
        assertTrue(fixture.requests.last().url.parameters["fields"]!!.contains("properties"))
    }

    @Test fun accountReplacementCancelsPendingMutationAndRejectsLaterWork() = runTest {
        val started = CompletableDeferred<Unit>()
        var cancelled = false
        val fixture = Fixture(this) {
            started.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        fixture.start()
        val result = async { fixture.repos.run.queue(BuildRequest("Build")) }
        started.await()
        fixture.sessions.saveAndActivate(AccountSecret(fixture.identity.copy(userName = "bob"), "fixture"))
        assertFailsWith<CancellationException> { result.await() }
        assertTrue(cancelled)
        assertFailsWith<CancellationException> { fixture.repos.run.queue(BuildRequest("Build")) }
        assertEquals(1, fixture.requests.size)
    }

    @Test fun legacyFilterLocatorsPreserveAllStatusBranchAndFlagRules() {
        val expected = listOf("status:SUCCESS", "status:FAILURE", "status:ERROR", "canceled:true", "failedToStart:true", "running:true", "state:queued", "state:any,canceled:any,failedToStart:any")
        BuildStatusFilter.entries.forEachIndexed { index, status ->
            val suffix = if (status in listOf(BuildStatusFilter.Running, BuildStatusFilter.Queued)) "" else ",count:10"
            assertEquals(expected[index] + ",branch:name:release,personal:true,pinned:" + (if (status == BuildStatusFilter.Queued) "any" else "true") + suffix, BuildFilter(status, "release", true, true).toTeamCityLocator())
        }
        assertEquals("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:false,count:10", BuildFilter().toTeamCityLocator())
    }
}
