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

import androidx.lifecycle.ViewModelStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.HttpTimeout
import kotlin.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.authentication.AccountCredentials
import teamcityapp.libraries.authentication.AuthenticationResult
import teamcityapp.libraries.network.TeamCityClientFactory

@OptIn(ExperimentalCoroutinesApi::class)
class SharedGraphOwnershipTest {
    @Test fun authenticationAndActivatedRepositoriesUseTheSameInjectedWireBoundary() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val models = ViewModelStore()
        val created = mutableListOf<AccountIdentity>()
        val requested = mutableListOf<String>()
        val storage = VaultAccountStorage(object : SecureVault {
            var value: String? = null
            override suspend fun read() = value
            override suspend fun write(value: String) {
                this.value = value
            }
        })
        try {
            val root = SharedAppViewModel(
                storage,
                object : SavedNavigationStore {
                    override suspend fun read(): String? = null
                    override suspend fun write(snapshot: String) {}
                },
                clients = TeamCityClientFactory { identity ->
                    created += identity
                    HttpClient(
                        MockEngine { request ->
                            requested += request.url.encodedPath
                            respond("""{"version":"shared-fixture","webUrl":"https://example.test/context"}""")
                        }
                    ) {
                        followRedirects = false
                        install(HttpTimeout)
                    }
                }
            ).also { models.put("root", it) }
            runCurrent()
            assertEquals(AuthenticationResult.Success, root.authentication.signIn(AccountCredentials("https://example.test/context", "", "", true), true, false))
            val account = (root.sessions.state.value as AccountSessionState.Active).scope
            assertEquals("shared-fixture", root.graph(account).api.server().version)
            assertEquals(2, created.size)
            assertEquals(created.first(), created.last())
            assertEquals(listOf("/context/guestAuth/app/rest/server", "/context/guestAuth/app/rest/server"), requested)
        } finally {
            models.clear()
            Dispatchers.resetMain()
        }
    }

    @Test fun hostRecreationReusesTheGraphButAccountReplacementDoesNot() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val models = ViewModelStore()
        val storage = VaultAccountStorage(object : SecureVault {
            var saved: String? = null
            override suspend fun read() = saved
            override suspend fun write(value: String) {
                saved = value
            }
        })
        val cache = object : teamcityapp.libraries.network.ResponseCacheStore {
            var content: String? = null
            override suspend fun read() = content
            override suspend fun write(value: String) {
                content = value
            }
        }
        try {
            val vm = SharedAppViewModel(
                storage,
                object : SavedNavigationStore {
                    override suspend fun read(): String? = null
                    override suspend fun write(snapshot: String) {}
                },
                cache
            ).also { models.put("root", it) }
            runCurrent()
            vm.sessions.saveAndActivate(AccountSecret(AccountIdentity("https://example.test", "Guest user", true)))
            val original = (vm.sessions.state.value as AccountSessionState.Active).scope
            val graph = vm.graph(original)
            assertSame(graph, vm.graph(original))
            vm.synchronizeSession()
            assertSame(graph, vm.graph(original))
            cache.content = "old authenticated response"
            vm.synchronizeSession(force = true)
            assertEquals("{}", cache.content)
            val replacement = (vm.sessions.state.value as AccountSessionState.Active).scope
            assertNotEquals(original.generation, replacement.generation)
            assertNotSame(graph, vm.graph(replacement))
            assertFailsWith<kotlinx.coroutines.CancellationException> { vm.graph(original) }
            models.clear()
            assertEquals(AccountSessionState.SignedOut, vm.sessions.state.value)
        } finally {
            models.clear()
            Dispatchers.resetMain()
        }
    }
}
