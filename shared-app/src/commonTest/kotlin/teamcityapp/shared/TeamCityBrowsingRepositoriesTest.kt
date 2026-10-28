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
import io.ktor.http.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import teamcityapp.features.agents.api.AgentsFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.HomeTab
import teamcityapp.features.running_builds.api.*
import teamcityapp.features.tests.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityBrowsingRepositoriesTest {
    private class Vault : SecureVault {
        var value: String? = null
        var fail = false
        override suspend fun read() = value
        override suspend fun write(value: String) {
            check(!fail)
            this.value = value
        }
    }
    private class Fixture(val scope: TestScope, handler: MockRequestHandler) {
        val vault = Vault()
        val storage = VaultAccountStorage(vault)
        val sessions = AccountSessionManager(storage, scope.backgroundScope.coroutineContext)
        lateinit var account: AccountScope
        lateinit var repositories: SharedAccountFeatures
        val identity = AccountIdentity("https://teamcity.example/context/", "guest", true)
        val requests = mutableListOf<Url>()
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler { request ->
                    requests += request.url
                    handler(request)
                }
            }
        )
        suspend fun start(initial: AccountPreferences = AccountPreferences()) {
            sessions.saveAndActivate(AccountSecret(identity))
            account = (sessions.state.value as AccountSessionState.Active).scope
            sessions.updatePreferences(account) { initial }
            repositories = sharedRepositoryFixture(account, TeamCityApi(TeamCityTransport(HttpClient(engine), AccountSecret(identity), true)), initial, "launch-one") { sessions.updatePreferences(account, it) }
        }
    }

    @Test fun educationAndRatingPersistWithTheAccountAndAreSharedAcrossRepositoryRecreation() = runTest {
        val fixture = Fixture(this) { error("Education must not make a network request") }
        fixture.start()
        val home = teamcityapp.features.home.api.HomePrompt.Drawer
        val history = teamcityapp.features.build_history.api.BuildHistoryPrompt.Run
        val details = teamcityapp.features.build_details.api.BuildDetailsPrompt.Share
        assertTrue(home in fixture.repositories.home.pendingPrompts())
        assertFalse(fixture.repositories.rating.isEligible())
        fixture.repositories.home.acknowledgePrompt(home)
        fixture.repositories.historyOnboarding.markShown(history)
        fixture.repositories.metadata.acknowledgePrompt(details)
        fixture.repositories.rating.markHandled()
        assertFalse(home in fixture.repositories.home.pendingPrompts())
        assertFalse(history in fixture.repositories.historyOnboarding.pendingPrompts())
        assertFalse(details in fixture.repositories.metadata.pendingPrompts())
        assertFalse(fixture.repositories.rating.isEligible())
        val saved = fixture.sessions.preferences(fixture.account)
        assertEquals(setOf("home.Drawer", "history.Run", "details.Share"), saved.shownPrompts)
        val recreated = sharedRepositoryFixture(fixture.account, TeamCityApi(TeamCityTransport(HttpClient(fixture.engine), AccountSecret(fixture.identity), true)), saved, "launch-two") { fixture.sessions.updatePreferences(fixture.account, it) }
        assertFalse(home in recreated.home.pendingPrompts())
        assertFalse(recreated.rating.isEligible())
        fixture.vault.fail = true
        assertFailsWith<IllegalStateException> { recreated.historyOnboarding.markShown(teamcityapp.features.build_history.api.BuildHistoryPrompt.Filter) }
        fixture.vault.fail = false
        assertTrue(teamcityapp.features.build_history.api.BuildHistoryPrompt.Filter in recreated.historyOnboarding.pendingPrompts())
        fixture.sessions.signOut()
        assertFailsWith<CancellationException> { recreated.rating.isEligible() }
    }

    @Test fun filtersAndFavoritesPersistBeforeQueryChangesAndAreAccountIsolated() = runTest {
        val fixture = Fixture(this) { respond("{}") }
        fixture.start()
        fixture.repositories.history.setFavorite("one", true)
        fixture.repositories.quickFilters.apply(QuickFilter.RunningAll)
        fixture.repositories.quickFilters.apply(QuickFilter.QueuedAll)
        fixture.repositories.quickFilters.apply(QuickFilter.AgentsDisconnected)
        val saved = fixture.storage.preferences(fixture.identity.key)
        assertEquals(AccountPreferences(listOf("one"), false, false, false), saved)
        assertEquals(emptyList(), fixture.repositories.running.query.first().favoriteConfigurationIds)
        assertEquals(AgentsFilter.Disconnected, fixture.repositories.agents.filter.first())
        fixture.vault.fail = true
        assertFailsWith<IllegalStateException> { fixture.repositories.history.setFavorite("two", true) }
        assertEquals(listOf("one"), fixture.repositories.home.query.first().favoriteIds)
        fixture.vault.fail = false
        fixture.sessions.saveAndActivate(AccountSecret(fixture.identity.copy(userName = "other")))
        val other = (fixture.sessions.state.value as AccountSessionState.Active).scope
        assertEquals(AccountPreferences(), fixture.sessions.preferences(other))
        assertFailsWith<CancellationException> { fixture.repositories.history.isFavorite("one") }
        fixture.sessions.activate(fixture.identity.key)
        assertEquals(saved, fixture.sessions.preferences((fixture.sessions.state.value as AccountSessionState.Active).scope))
    }

    @Test fun emptyFavoriteFiltersMakeNoBuildOrCountRequests() = runTest {
        val fixture = Fixture(this) { error("No request expected") }
        fixture.start()
        val query = fixture.repositories.running.query.first()
        assertTrue(fixture.repositories.running.builds(query, false).isEmpty())
        assertEquals(teamcityapp.features.home.api.HomeCount.Available(0), fixture.repositories.home.count(fixture.repositories.home.query.first(), HomeTab.Running))
        assertTrue(fixture.requests.isEmpty())
    }

    @Test fun allQueuedBuildsOmitTheLocatorRatherThanSendingAnInvalidEmptyLocator() = runTest {
        val fixture = Fixture(this) { request ->
            assertTrue(request.url.encodedPath.endsWith("/buildQueue"))
            assertFalse(request.url.parameters.contains("locator"))
            if (request.url.parameters["fields"] == "count") {
                respond("""{"count":1}""")
            } else {
                respond("""{"count":1,"build":[{"id":"4","href":"/context/app/rest/builds/id:4","state":"queued"}]}""")
            }
        }
        fixture.start(AccountPreferences(queueFavorites = false))
        assertEquals("4", fixture.repositories.queue.builds(fixture.repositories.queue.query.first(), false).single().id)
        assertEquals(teamcityapp.features.home.api.HomeCount.Available(1), fixture.repositories.home.count(fixture.repositories.home.query.first(), HomeTab.Queue))
        assertEquals(2, fixture.requests.size)
    }
}
