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

package teamcityapp.libraries.shared_data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.network.*

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityAuthenticationTest {
    private class Vault : SecureVault {
        var value: String? = null
        var fail = false
        override suspend fun read() = value
        override suspend fun write(value: String) {
            check(!fail)
            this.value = value
        }
    }
    private fun client(handler: MockRequestHandler) = HttpClient(
        MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler(handler)
            }
        )
    )
    private val credentials = AccountCredentials("https://teamcity.example/context", "alice", "password")

    @Test fun challengeAuthenticationPersistsBeforePublishingTheAccount() = runTest {
        val vault = Vault()
        val sessions = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        val headers = mutableListOf<String?>()
        val authentication = TeamCityAuthentication(sessions) {
            client { request ->
                headers += request.headers[HttpHeaders.Authorization]
                assertEquals("/context/httpAuth/app/rest/server", request.url.encodedPath)
                if (headers.size == 1) {
                    respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Basic realm=\"TeamCity\""))
                } else {
                    respond("""{"version":"2026.2","webUrl":"https://teamcity.example/context"}""", HttpStatusCode.OK)
                }
            }
        }
        assertEquals(AuthenticationResult.Success, authentication.signIn(credentials, true, true))
        assertNull(headers.first())
        assertTrue(headers.last()!!.startsWith("Basic "))
        val active = (sessions.state.value as AccountSessionState.Active).scope
        assertEquals("https://teamcity.example/context/", active.identity.serverUrl)
        assertEquals("alice", active.identity.userName)
        assertEquals("password", sessions.secret(active).password)
        val restored = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        restored.restore()
        assertEquals(active.identity, (restored.state.value as AccountSessionState.Active).scope.identity)
    }

    @Test fun httpConsentAndDuplicateChecksRunBeforeOpeningAnEngine() = runTest {
        val storage = VaultAccountStorage(Vault())
        val sessions = AccountSessionManager(storage, backgroundScope.coroutineContext)
        var opened = 0
        val auth = TeamCityAuthentication(sessions) {
            opened++
            client { respond("""{"version":"test"}""", HttpStatusCode.OK) }
        }
        assertEquals(-1, (auth.signIn(credentials.copy(serverUrl = "http://teamcity.example"), true, true) as AuthenticationResult.Failure).statusCode)
        assertEquals(0, opened)
        assertEquals(AuthenticationResult.Success, auth.signIn(credentials.copy(serverUrl = "http://teamcity.example"), false, true))
        assertEquals(AuthenticationResult.DuplicateAccount, auth.signIn(credentials.copy(serverUrl = "http://teamcity.example", sslDisabled = true), false, true))
        assertEquals(1, opened)
    }

    @Test fun invalidUrlAndFailedAuthenticationDoNotWriteAccounts() = runTest {
        val vault = Vault()
        val sessions = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        var calls = 0
        val auth = TeamCityAuthentication(sessions) {
            client {
                calls++
                respond("secret server body", HttpStatusCode.Unauthorized)
            }
        }
        val invalid = auth.signIn(credentials.copy(serverUrl = "https://user:password@teamcity.example"), true, true) as AuthenticationResult.Failure
        assertEquals(AuthenticationError.InvalidUrl, invalid.error)
        assertEquals(0, calls)
        val failed = auth.signIn(credentials, true, true) as AuthenticationResult.Failure
        assertEquals(AuthenticationError.Http(401), failed.error)
        assertFalse(failed.toString().contains("secret server body"))
        assertNull(vault.value)
        assertEquals(AccountSessionState.Loading, sessions.state.value)
    }

    @Test fun encryptionFailureKeepsThePreviousAccountAndSession() = runTest {
        val vault = Vault()
        val sessions = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        sessions.saveAndActivate(AccountSecret(AccountIdentity("https://other.example", "bob", false), "old"))
        val previous = sessions.state.value
        vault.fail = true
        val auth = TeamCityAuthentication(sessions) { client { respond("""{"version":"test"}""", HttpStatusCode.OK) } }
        assertEquals(AuthenticationResult.SaveFailed, auth.signIn(credentials, true, true))
        assertSame(previous, sessions.state.value)
    }

    @Test fun sslOverrideIsPassedOnlyWhenExplicitlySelected() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        val flags = mutableListOf<Boolean>()
        val auth = TeamCityAuthentication(sessions) { disabled ->
            flags += disabled
            client { respond("""{"version":"test"}""", HttpStatusCode.OK) }
        }
        auth.signIn(credentials, true, false)
        auth.signIn(credentials.copy(sslDisabled = true), true, false)
        assertEquals(listOf(false, true), flags)
    }

    @Test fun cancelledAuthenticationNeverPersistsOrActivates() = runTest {
        val vault = Vault()
        val sessions = AccountSessionManager(VaultAccountStorage(vault), backgroundScope.coroutineContext)
        val requested = CompletableDeferred<Unit>()
        val auth = TeamCityAuthentication(sessions) {
            client {
                requested.complete(Unit)
                awaitCancellation()
            }
        }
        val call = launch { auth.signIn(credentials, true, true) }
        requested.await()
        call.cancelAndJoin()
        assertNull(vault.value)
        assertEquals(AccountSessionState.Loading, sessions.state.value)
    }

    @Test fun newAccountsCannotReceiveTheOldGraphsCachedResults() = runTest {
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), backgroundScope.coroutineContext)
        val alice = AccountIdentity("https://teamcity.example", "alice", false)
        sessions.saveAndActivate(AccountSecret(alice, "a"))
        val old = (sessions.state.value as AccountSessionState.Active).scope
        val graph = TeamCityAccountGraph(old, TeamCityApi(TeamCityTransport(client { respond("""{"projects":{"project":[{"id":"A","name":"Alice project"}]}}""", HttpStatusCode.OK) }, AccountSecret(alice, "a"))))
        assertEquals(1, graph.entries("_Root", false).size)
        sessions.saveAndActivate(AccountSecret(alice.copy(userName = "bob"), "b"))
        assertFailsWith<CancellationException> { graph.entries("_Root", false) }
    }
}
