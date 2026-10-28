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

package teamcityapp.libraries.network.testing

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.request.HttpRequestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class FixtureVault : SecureVault {
    var value: String? = null
    var fail = false
    override suspend fun read() = value
    override suspend fun write(value: String) {
        check(!fail)
        this.value = value
    }
}

/** Deterministic account, persistence and HTTP ports; features construct their own repositories. */
@OptIn(ExperimentalCoroutinesApi::class)
open class AccountNetworkFixture(
    scope: TestScope,
    val identity: AccountIdentity = AccountIdentity("https://teamcity.example/context/", "guest", true),
    handler: MockRequestHandler
) {
    val vault = FixtureVault()
    val storage = VaultAccountStorage(vault)
    val sessions = AccountSessionManager(storage, scope.backgroundScope.coroutineContext)
    lateinit var account: AccountScope
    lateinit var api: TeamCityApi
    lateinit var preferences: AccountPreferenceState
    val requests = mutableListOf<HttpRequestData>()
    val urls get() = requests.map { it.url }
    private val engine = MockEngine(
        MockEngineConfig().apply {
            dispatcher = Dispatchers.Unconfined
            addHandler { request ->
                requests += request
                handler(request)
            }
        }
    )
    open suspend fun start(initial: AccountPreferences = AccountPreferences()) {
        val secret = AccountSecret(identity, "fixture")
        sessions.saveAndActivate(secret)
        account = (sessions.state.value as AccountSessionState.Active).scope
        sessions.updatePreferences(account) { initial }
        preferences = AccountPreferenceState(account, initial) { sessions.updatePreferences(account, it) }
        api = TeamCityApi(TeamCityTransport(HttpClient(engine) { followRedirects = false }, secret, httpConsent = true))
        account.coroutineScope.coroutineContext[Job]!!.invokeOnCompletion { api.close() }
    }
}
