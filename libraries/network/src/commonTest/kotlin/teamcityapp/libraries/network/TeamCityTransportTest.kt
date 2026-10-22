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

package teamcityapp.libraries.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import teamcityapp.libraries.accounts.*

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityTransportTest {
    private fun secret(guest: Boolean = false, url: String = "https://teamcity.example/context/") = AccountSecret(AccountIdentity(url, if (guest) "Guest user" else "alice", guest), "password")
    private fun mockEngine(handler: MockRequestHandler) = MockEngine(
        MockEngineConfig().apply {
            dispatcher = Dispatchers.Unconfined
            addHandler(handler)
        }
    )
    private fun client(handler: MockEngine) = HttpClient(handler) { followRedirects = false }

    @Test fun artifactStreamingOutlivesTheJsonDeadlineWithoutDisablingJsonTimeouts() = runTest {
        val engine = mockEngine {
            delay(200)
            respond("{}")
        }
        val client = HttpClient(engine) {
            followRedirects = false
            install(HttpTimeout) { requestTimeoutMillis = 50 }
        }
        val transport = TeamCityTransport(client, secret(true))
        try {
            var bytes = 0
            transport.stream("/context/app/rest/artifacts/content/report.txt", { _, count -> bytes += count }, { _, _ -> })
            assertEquals(2, bytes)
            assertFailsWith<HttpRequestTimeoutException> { transport.get("/context/app/rest/server") }
        } finally {
            transport.close()
        }
    }

    @Test fun decodedCompressedArtifactsDoNotCompareBytesWithTheEncodedWireLength() = runTest {
        val engine = mockEngine { request ->
            assertEquals("identity", request.headers[HttpHeaders.AcceptEncoding])
            // Models engines which have already decoded a compressed network response.
            respond("decoded", headers = headersOf(HttpHeaders.ContentEncoding to listOf("gzip"), HttpHeaders.ContentLength to listOf("2")))
        }
        val client = HttpClient(engine) { install(HttpTimeout) }
        val transport = TeamCityTransport(client, secret(true))
        try {
            var final: Pair<Long, Long?>? = null
            transport.stream("/context/app/rest/artifacts/content/report.txt", { _, _ -> }, { count, total -> final = count to total })
            assertEquals(7L to null, final)
        } finally {
            transport.close()
        }
    }

    @Test fun privateArtifactStreamingUsesOneBasicChallengeAndReportsItsCompleteBody() = runTest {
        val authorizations = mutableListOf<String?>()
        val engine = mockEngine { request ->
            authorizations += request.headers[HttpHeaders.Authorization]
            if (authorizations.size == 1) {
                respond("challenge", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Basic realm=fixture"))
            } else {
                respond("artifact", headers = headersOf(HttpHeaders.ContentLength, "8"))
            }
        }
        val client = HttpClient(engine) {
            followRedirects = false
            install(HttpTimeout)
        }
        val transport = TeamCityTransport(client, secret())
        try {
            var received = 0
            var final: Pair<Long, Long?>? = null
            transport.stream("/context/app/rest/artifacts/content/report.txt", { _, count -> received += count }, { count, total -> final = count to total })
            assertEquals(8, received)
            assertEquals(8L to 8L, final)
            assertEquals(listOf(null, "Basic YWxpY2U6cGFzc3dvcmQ="), authorizations)
        } finally {
            transport.close()
        }
    }

    @Test fun urlNormalizationPreservesLegacyRootAndContextConventions() {
        assertEquals("https://teamcity.example", normalizeServerUrl("https://teamcity.example"))
        assertEquals("https://teamcity.example/", normalizeServerUrl("https://teamcity.example/"))
        assertEquals("https://teamcity.example/context/", normalizeServerUrl("https://teamcity.example/context"))
        assertFails { normalizeServerUrl("https://alice:password@teamcity.example") }
        assertFails { normalizeServerUrl("file:///tmp/server") }
        assertFails { normalizeServerUrl("https://teamcity.example?token=secret") }
    }

    @Test fun httpCannotStartWithoutExplicitConsent() {
        val http = client(mockEngine { respond("{}") })
        assertFailsWith<HttpConsentRequired> { TeamCityTransport(http, secret(url = "http://teamcity.example")) }
        TeamCityTransport(http, secret(url = "http://teamcity.example"), httpConsent = true).close()
    }

    @Test fun opaqueContinuationKeepsEncodedQueryAndGetsAccountAuthPath() {
        val transport = TeamCityTransport(client(mockEngine { respond("{}") }), secret(true))
        val path = "/context/app/rest/builds?locator=buildType%3A(id%3AA%2BB),start%3A10&fields=build(id)&x=a+b&x=%2B"
        assertEquals("https://teamcity.example/context/guestAuth/app/rest/builds?" + path.substringAfter('?'), transport.resolve(path).toString())
        transport.close()
    }

    @Test fun continuationRejectsOtherOriginsCredentialsContextsAndTraversal() {
        val transport = TeamCityTransport(client(mockEngine { respond("{}") }), secret())
        for (path in listOf("https://elsewhere.example/app/rest/builds", "//elsewhere.example/app/rest/builds", "http://teamcity.example/context/app/rest/builds", "https://alice:x@teamcity.example/context/app/rest/builds", "/app/rest/builds", "/context/app/rest/%2e%2e/projects", "/context/app/rest/%2e%2e%2foutside", "/context/login.html")) {
            assertFails("Must reject $path") { transport.resolve(path) }
        }
        transport.close()
    }

    @Test fun basicCredentialsAreSentOnlyAfterAChallengeAndOnlyOnce() = runTest {
        val auth = mutableListOf<String?>()
        val engine = mockEngine { request ->
            auth += request.headers[HttpHeaders.Authorization]
            if (auth.size == 1) respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Basic realm=\"TeamCity\"")) else respond("{}")
        }
        val transport = TeamCityTransport(client(engine), secret())
        transport.get(transport.apiPath("server"))
        assertEquals(listOf(null, "Basic YWxpY2U6cGFzc3dvcmQ="), auth)
        transport.close()
    }

    @Test fun simultaneousInitialChallengesEachRetryOnlyTheirOwnUnauthorizedRequest() = runTest {
        val bothStarted = CompletableDeferred<Unit>()
        var anonymous = 0
        var authorized = 0
        val transport = TeamCityTransport(
            client(
                mockEngine { request ->
                    if (request.headers[HttpHeaders.Authorization] == null) {
                        anonymous++
                        if (anonymous == 2) bothStarted.complete(Unit)
                        bothStarted.await()
                        respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Basic realm=\"TeamCity\""))
                    } else {
                        authorized++
                        respond("{}")
                    }
                }
            ),
            secret()
        )
        val first = async { transport.get(transport.apiPath("server")) }
        val second = async { transport.get(transport.apiPath("projects")) }
        first.await()
        second.await()
        assertEquals(2, anonymous)
        assertEquals(2, authorized)
        transport.close()
    }

    @Test fun failedBasicAuthenticationDoesNotLoopOrReturnServerBodyAsUiCopy() = runTest {
        var requests = 0
        val engine = mockEngine {
            requests++
            respond("private server diagnostics", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Basic realm=\"TeamCity\""))
        }
        val transport = TeamCityTransport(client(engine), secret())
        val error = assertFailsWith<TeamCityHttpFailure> { transport.get(transport.apiPath("server")) }
        assertEquals(401, error.status)
        assertEquals(2, requests)
        assertFalse(error.toString().contains("private server diagnostics"))
        transport.close()
    }

    @Test fun bearerChallengeDoesNotReceiveBasicCredentials() = runTest {
        var requests = 0
        val engine = mockEngine { request ->
            requests++
            assertNull(request.headers[HttpHeaders.Authorization])
            respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Bearer"))
        }
        val transport = TeamCityTransport(client(engine), secret())
        assertFailsWith<TeamCityHttpFailure> { transport.get(transport.apiPath("server")) }
        assertEquals(1, requests)
        transport.close()
    }

    @Test fun guestRequestsNeverSendAnAuthorizationHeader() = runTest {
        val engine = mockEngine { request ->
            assertNull(request.headers[HttpHeaders.Authorization])
            assertTrue(request.url.encodedPath.contains("/guestAuth/app/rest/"))
            respond("{}")
        }
        val transport = TeamCityTransport(client(engine), secret(guest = true))
        transport.get(transport.apiPath("server"))
        transport.close()
    }

    @Test fun redirectsDoNotForwardCredentialsToAnotherHost() = runTest {
        var requests = 0
        val transport = TeamCityTransport(
            client(
                mockEngine {
                    requests++
                    respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "https://elsewhere.example/private"))
                }
            ),
            secret()
        )
        assertFailsWith<TeamCityHttpFailure> { transport.get(transport.apiPath("server")) }
        assertEquals(1, requests)
        transport.close()
    }

    @Test fun destinationCancellationCancelsTheUnderlyingEngineCall() = runTest {
        val entered = CompletableDeferred<Unit>()
        val stopped = CompletableDeferred<Unit>()
        val transport = TeamCityTransport(
            client(
                mockEngine {
                    entered.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        stopped.complete(Unit)
                    }
                }
            ),
            secret()
        )
        val request = launch { transport.get(transport.apiPath("server")) }
        entered.await()
        request.cancel()
        request.join()
        withTimeout(5_000) { stopped.await() }
        assertTrue(stopped.isCompleted)
        transport.close()
    }

    @Test fun accountSwitchCancelsItsEngineCallAndRejectsStaleCompletion() = runTest {
        val storage = VaultAccountStorage(object : SecureVault {
            var value: String? = null
            override suspend fun read() = value
            override suspend fun write(value: String) {
                this.value = value
            }
        })
        val manager = AccountSessionManager(storage, backgroundScope.coroutineContext)
        manager.saveAndActivate(secret())
        val old = (manager.state.value as AccountSessionState.Active).scope
        val entered = CompletableDeferred<Unit>()
        val stopped = CompletableDeferred<Unit>()
        val transport = TeamCityTransport(
            client(
                mockEngine {
                    entered.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        stopped.complete(Unit)
                    }
                }
            ),
            secret()
        )
        val request = launch { old.run { transport.get(transport.apiPath("server")) } }
        entered.await()
        manager.saveAndActivate(AccountSecret(secret().identity.copy(userName = "bob"), "other"))
        runCurrent()
        request.join()
        assertTrue(request.isCancelled)
        assertTrue(stopped.isCompleted)
        assertFailsWith<CancellationException> { old.requireCurrent() }
        transport.close()
    }
}
