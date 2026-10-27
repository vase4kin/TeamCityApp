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

package teamcityapp.features.run_build.impl.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.network.testing.AccountNetworkFixture

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityRunBuildRepositoryTest {
    private class Fixture(scope: TestScope, handler: MockRequestHandler) : AccountNetworkFixture(scope, AccountIdentity("https://teamcity.example/context/", "alice", false), handler) {
        lateinit var repository: TeamCityRunBuildRepository
        override suspend fun start(initial: AccountPreferences) {
            super.start(initial)
            repository = TeamCityRunBuildRepository(account, api)
        }
    }

    @Test fun queueIsOnePostReturnsAReferenceAndInvalidatesCachedReads() = runTest {
        val fixture = Fixture(this) { request ->
            if (request.method == HttpMethod.Post) respond("""{"id":"44","href":"/context/app/rest/buildQueue/id:44"}""") else respond("""{"branch":[{"name":"main"}]}""")
        }
        fixture.start()
        fixture.repository.branches("Build")
        fixture.repository.branches("Build")
        assertEquals(1, fixture.requests.size)
        assertEquals(QueueBuildResult.Success("/context/app/rest/buildQueue/id:44"), fixture.repository.queue(BuildRequest("Build")))
        fixture.repository.branches("Build")
        assertEquals(listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Get), fixture.requests.map { it.method })
        val post = fixture.requests[1]
        assertTrue(post.url.encodedPath.endsWith("/httpAuth/app/rest/buildQueue"))
        assertEquals(ContentType.Application.Json.toString(), post.body.contentType.toString())
    }

    @Test fun forbiddenAndServerErrorsDoNotRetryOrExposeServerText() = runTest {
        for ((code, result) in listOf(HttpStatusCode.Forbidden to QueueBuildResult.Forbidden, HttpStatusCode.InternalServerError to QueueBuildResult.Error)) {
            val fixture = Fixture(this) { respond("private diagnostics", code) }
            fixture.start()
            assertEquals(result, fixture.repository.queue(BuildRequest("Build")))
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
        assertIs<QueueBuildResult.Success>(fixture.repository.queue(BuildRequest("Build")))
        assertEquals(2, calls)
        assertNull(fixture.requests[0].headers[HttpHeaders.Authorization])
        assertNotNull(fixture.requests[1].headers[HttpHeaders.Authorization])
        assertEquals((fixture.requests[0].body as TextContent).text, (fixture.requests[1].body as TextContent).text)
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
        val result = async { fixture.repository.queue(BuildRequest("Build")) }
        started.await()
        fixture.sessions.saveAndActivate(AccountSecret(fixture.identity.copy(userName = "bob"), "fixture"))
        assertFailsWith<CancellationException> { result.await() }
        assertTrue(cancelled)
        assertFailsWith<CancellationException> { fixture.repository.queue(BuildRequest("Build")) }
        assertEquals(1, fixture.requests.size)
    }
}
