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
        lateinit var repositories: TeamCityBrowsingRepositories
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
            repositories = TeamCityBrowsingRepositories(account, TeamCityApi(TeamCityTransport(HttpClient(engine), AccountSecret(identity), true)), initial, "launch-one") { sessions.updatePreferences(account, it) }
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
        val recreated = TeamCityBrowsingRepositories(fixture.account, TeamCityApi(TeamCityTransport(HttpClient(fixture.engine), AccountSecret(fixture.identity), true)), saved, "launch-two") { fixture.sessions.updatePreferences(fixture.account, it) }
        assertFalse(home in recreated.home.pendingPrompts())
        assertFalse(recreated.rating.isEligible())
        fixture.vault.fail = true
        assertFailsWith<IllegalStateException> { recreated.historyOnboarding.markShown(teamcityapp.features.build_history.api.BuildHistoryPrompt.Filter) }
        fixture.vault.fail = false
        assertTrue(teamcityapp.features.build_history.api.BuildHistoryPrompt.Filter in recreated.historyOnboarding.pendingPrompts())
        fixture.sessions.signOut()
        assertFailsWith<CancellationException> { recreated.rating.isEligible() }
    }

    @Test fun ratingRequiresThreeDistinctSuccessfulViewsAndALaterAppLaunch() = runTest {
        val fixture = Fixture(this) { error("Local rating policy must not make requests") }
        fixture.start()
        fun repositories(initial: AccountPreferences, launch: String) = TeamCityBrowsingRepositories(fixture.account, TeamCityApi(TeamCityTransport(HttpClient(fixture.engine), AccountSecret(fixture.identity), true)), initial, launch) { fixture.sessions.updatePreferences(fixture.account, it) }
        for (id in listOf("1", "1", "2")) fixture.repositories.buildViewed(id)
        assertEquals(setOf("1", "2"), fixture.sessions.preferences(fixture.account).ratingViewedBuildIds)
        assertFalse(fixture.repositories.rating.isEligible())
        fixture.repositories.buildViewed("3")
        fixture.repositories.buildViewed("4")
        val qualified = fixture.sessions.preferences(fixture.account)
        assertEquals(setOf("1", "2", "3"), qualified.ratingViewedBuildIds)
        assertEquals("launch-one", qualified.ratingQualifiedLaunch)
        assertFalse(fixture.repositories.rating.isEligible())
        assertFalse(repositories(qualified, "launch-one").rating.isEligible()) // account/host recreation is not another launch
        val later = repositories(qualified, "launch-two")
        assertTrue(later.rating.isEligible())
        later.rating.markHandled()
        assertFalse(later.rating.isEligible())
        assertFalse(repositories(fixture.sessions.preferences(fixture.account), "launch-three").rating.isEligible())
        assertTrue(fixture.requests.isEmpty())
        fixture.sessions.signOut()
        assertFailsWith<CancellationException> { fixture.repositories.buildViewed("1") }
    }

    @Test fun ratingMilestoneWritesAreRetryableAndCannotCrossAccounts() = runTest {
        val fixture = Fixture(this) { error("No request") }
        fixture.start()
        fixture.repositories.buildViewed("1")
        fixture.vault.fail = true
        assertFails { fixture.repositories.buildViewed("2") }
        assertEquals(setOf("1"), fixture.sessions.preferences(fixture.account).ratingViewedBuildIds)
        fixture.vault.fail = false
        fixture.repositories.buildViewed("2")
        assertEquals(setOf("1", "2"), fixture.sessions.preferences(fixture.account).ratingViewedBuildIds)
        val bob = fixture.identity.copy(userName = "bob")
        fixture.sessions.saveAndActivate(AccountSecret(bob))
        val active = (fixture.sessions.state.value as AccountSessionState.Active).scope
        assertEquals(AccountPreferences(), fixture.sessions.preferences(active))
        assertFailsWith<CancellationException> { fixture.repositories.buildViewed("3") }
        assertEquals(AccountPreferences(), fixture.sessions.preferences(active))
        val stored = fixture.storage.preferences(fixture.identity.key)
        assertEquals(setOf("1", "2"), stored.ratingViewedBuildIds)
        assertNull(stored.ratingQualifiedLaunch)
    }

    @Test fun favoritesKeepSavedIdsWhenSomeOrEveryConfigurationFails() = runTest {
        val fixture = Fixture(this) { request ->
            if (request.url.encodedPath.contains("id:valid")) {
                respond("""{"id":"valid","name":"Build","projectId":"P","projectName":"Project"}""")
            } else {
                respond("", HttpStatusCode.NotFound)
            }
        }
        fixture.start(AccountPreferences(favoriteIds = listOf("valid", "missing")))
        val partial = fixture.repositories.favorites.favorites(false)
        assertEquals(listOf("valid", "missing"), partial.savedIds)
        assertEquals(listOf("missing"), partial.unavailableIds)
        assertEquals(listOf("valid"), partial.configurations.map { it.id })
        fixture.repositories.setFavorite("valid", false)
        val failed = fixture.repositories.favorites.favorites(true)
        assertEquals(listOf("missing"), failed.savedIds)
        assertTrue(failed.configurations.isEmpty())
        assertEquals(listOf("missing"), fixture.storage.preferences(fixture.identity.key).favoriteIds)
    }

    @Test fun filtersAndFavoritesPersistBeforeQueryChangesAndAreAccountIsolated() = runTest {
        val fixture = Fixture(this) { respond("{}") }
        fixture.start()
        fixture.repositories.setFavorite("one", true)
        fixture.repositories.filters.apply(QuickFilter.RunningAll)
        fixture.repositories.filters.apply(QuickFilter.QueuedAll)
        fixture.repositories.filters.apply(QuickFilter.AgentsDisconnected)
        val saved = fixture.storage.preferences(fixture.identity.key)
        assertEquals(AccountPreferences(listOf("one"), false, false, false), saved)
        assertEquals(emptyList(), fixture.repositories.running.query.first().favoriteConfigurationIds)
        assertEquals(AgentsFilter.Disconnected, fixture.repositories.agents.filter.first())
        fixture.vault.fail = true
        assertFailsWith<IllegalStateException> { fixture.repositories.setFavorite("two", true) }
        assertEquals(listOf("one"), fixture.repositories.home.query.first().favoriteIds)
        fixture.vault.fail = false
        fixture.sessions.saveAndActivate(AccountSecret(fixture.identity.copy(userName = "other")))
        val other = (fixture.sessions.state.value as AccountSessionState.Active).scope
        assertEquals(AccountPreferences(), fixture.sessions.preferences(other))
        assertFailsWith<CancellationException> { fixture.repositories.isFavorite("one") }
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

    @Test fun monitoringUsesBulkProjectionAndFollowsOpaqueContinuationWithoutDetailRequests() = runTest {
        val fixture = Fixture(this) { request ->
            when (request.url.encodedPath) {
                "/context/guestAuth/app/rest/builds" -> {
                    assertEquals("running:true,branch:default:any,personal:false,pinned:false", request.url.parameters["locator"])
                    assertTrue(request.url.parameters["fields"]!!.contains("properties"))
                    respond("""{"build":[{"id":"1","href":"/context/app/rest/builds/id:1","buildTypeId":"C"}],"nextHref":"/context/app/rest/opaque?cursor=a%2Bb%3D&extra=x"}""")
                }

                "/context/guestAuth/app/rest/opaque" -> {
                    assertEquals("a+b=", request.url.parameters["cursor"])
                    assertEquals("x", request.url.parameters["extra"])
                    respond("""{"build":[{"id":"2","href":"/context/app/rest/builds/id:2","buildTypeId":"C"}]}""")
                }

                else -> error("Unexpected detail request: ${request.url}")
            }
        }
        fixture.start(AccountPreferences(runningFavorites = false))
        assertEquals(listOf("1", "2"), fixture.repositories.running.builds(fixture.repositories.running.query.first(), false).map { it.id })
        assertEquals(2, fixture.requests.size)
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
        val page = fixture.repositories.tests.tests(url, TestsFilter.Failed, null, false)
        assertEquals(TestStatus.Failed, page.items.single().status)
        assertFails { fixture.repositories.tests.summary(url) }
        assertEquals(page, fixture.repositories.tests.tests(url, TestsFilter.Failed, null, false))
        assertNull(fixture.repositories.tests.tests(url, TestsFilter.Passed, page.nextHref, false).nextHref)
        failCount = false
        assertEquals(TestsSummary(7, TestsCounts(4, 2, 1)), fixture.repositories.tests.summary(url))
    }

    @Test fun changesHydrateFilesAndPreserveDateAndStableIdentity() = runTest {
        val fixture = Fixture(this) { request ->
            if (request.url.encodedPath.endsWith("/changes")) {
                respond("""{"count":1,"change":[{"href":"/context/app/rest/changes/id:5"}],"nextHref":"/context/app/rest/changes?cursor=next"}""")
            } else {
                respond("""{"username":"author","date":"20261021T102030+0000","comment":"Fix","version":"abc","files":{"file":[{"file":"README.md","changeType":"added"}]}}""")
            }
        }
        fixture.start()
        val page = fixture.repositories.changes.changes("/context/app/rest/changes?locator=build:(id:7)", null, false)
        assertEquals("/context/app/rest/changes/id:5", page.items.single().id)
        assertEquals("21 Oct 26 10:20", page.items.single().date)
        assertEquals("README.md", page.items.single().files.single().name)
    }

    @Test fun accountReplacementCancelsHydrationAndPreventsOldQueryRequests() = runTest {
        val entered = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val fixture = Fixture(this) {
            entered.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                cancelled.complete(Unit)
            }
        }
        fixture.start(AccountPreferences(runningFavorites = false))
        val load = async { fixture.repositories.running.builds(fixture.repositories.running.query.first(), true) }
        entered.await()
        fixture.sessions.saveAndActivate(AccountSecret(fixture.identity.copy(userName = "other")))
        assertFailsWith<CancellationException> { load.await() }
        cancelled.await()
        assertFailsWith<CancellationException> { fixture.repositories.running.builds(RunningBuildsQuery(fixture.identity.key, RunningBuildsFilter.All), true) }
        assertEquals(1, fixture.requests.size)
    }

    @Test fun continuationCannotEscapeAccountOrigin() = runTest {
        val fixture = Fixture(this) { error("Must reject before HTTP") }
        fixture.start()
        assertFails { fixture.repositories.tests.tests("/context/app/rest/tests", TestsFilter.Failed, "https://evil.example/steal", false) }
        assertTrue(fixture.requests.isEmpty())
    }
}
