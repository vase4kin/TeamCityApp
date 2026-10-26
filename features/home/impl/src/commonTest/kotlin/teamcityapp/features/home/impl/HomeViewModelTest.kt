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

package teamcityapp.features.home.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterRepository
import teamcityapp.features.home.api.*
import teamcityapp.features.home.impl.resources.*
import teamcityapp.features.home.impl.tracker.HomeTracker

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val query = MutableStateFlow(HomeQuery("account-one", listOf("saved"), QuickFilter.RunningFavorites, QuickFilter.QueuedFavorites, QuickFilter.AgentsConnected))
    private val counts = mapOf(HomeTab.Running to HomeCount.Available(2), HomeTab.Queue to HomeCount.Unavailable, HomeTab.Agents to HomeCount.Available(3))
    private var calls = 0
    private var cancelled = 0
    private val requestedTabs = mutableListOf<HomeTab>()
    private var load: suspend (HomeQuery, HomeTab) -> HomeCount = { _, tab -> counts.getValue(tab) }
    private var pending: suspend () -> Set<HomePrompt> = { setOf(HomePrompt.Drawer, HomePrompt.Favorites, HomePrompt.Running) }
    private val acknowledgements = mutableListOf<HomePrompt>()
    private val events = mutableListOf<String>()
    private var queryFlow: Flow<HomeQuery> = query
    private val applied = mutableListOf<QuickFilter>()
    private var applyFilter: suspend (QuickFilter) -> Unit = { filter ->
        applied += filter
        query.value = when (filter) {
            QuickFilter.RunningAll, QuickFilter.RunningFavorites -> query.value.copy(runningFilter = filter)
            QuickFilter.QueuedAll, QuickFilter.QueuedFavorites -> query.value.copy(queueFilter = filter)
            else -> query.value.copy(agentsFilter = filter)
        }
    }
    private val filters = object : QuickFilterRepository {
        override suspend fun apply(filter: QuickFilter) = applyFilter(filter)
    }
    private val repository = object : HomeRepository {
        override val query: Flow<HomeQuery> get() = queryFlow
        override suspend fun count(query: HomeQuery, tab: HomeTab): HomeCount {
            calls++
            requestedTabs += tab
            try {
                return load(query, tab)
            } catch (error: CancellationException) {
                cancelled++
                throw error
            }
        }
        override suspend fun pendingPrompts() = pending()
        override suspend fun acknowledgePrompt(prompt: HomePrompt) {
            acknowledgements += prompt
        }
    }
    private val tracker = object : HomeTracker {
        override fun viewed() {
            events += "viewed"
        }
        override fun selected(tab: HomeTab) {
            events += "selected:$tab"
        }
        override fun fab(tab: HomeTab) {
            events += "fab:$tab"
        }
        override fun addProjects() {
            events += "add"
        }
    }

    @BeforeTest fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun model(handle: SavedStateHandle = SavedStateHandle()) = HomeViewModel(handle, repository, tracker, filters).also { store.put("home", it) }
    private fun TestScope.observe(model: HomeViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect {} }

    @Test fun countsAreCollectionDrivenAndOptionalFailuresRetainOtherBadges() = runTest(dispatcher) {
        val model = model()
        runCurrent()
        assertEquals(0, calls)
        observe(model)
        runCurrent()
        assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
        assertTrue(model.state.value.badges[HomeTab.Queue] is HomeBadge.Failed)
        assertEquals(HomeBadge.Count(3), model.state.value.badges[HomeTab.Agents])
    }

    @Test fun hidingCancelsUnfinishedCountsAndResubscriptionRetriesThem() = runTest(dispatcher) {
        load = { _, _ -> awaitCancellation() }
        val model = model()
        val collector = observe(model)
        runCurrent()
        assertEquals(3, calls)
        collector.cancel()
        runCurrent()
        assertEquals(3, cancelled)
        load = { _, tab -> counts.getValue(tab) }
        observe(model)
        runCurrent()
        assertEquals(6, calls)
        assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
    }

    @Test fun aChangedFilterCancelsTheOldRequestAndOnlyPublishesTheNewCount() = runTest(dispatcher) {
        val old = CompletableDeferred<HomeCount>()
        load = { query, tab ->
            if (tab == HomeTab.Running) {
                if (query.runningFilter == QuickFilter.RunningFavorites) old.await() else HomeCount.Available(9)
            } else {
                counts.getValue(tab)
            }
        }
        val model = model()
        observe(model)
        runCurrent()
        query.value = query.value.copy(runningFilter = QuickFilter.RunningAll)
        runCurrent()
        old.complete(HomeCount.Available(2))
        runCurrent()
        assertEquals(1, cancelled)
        assertEquals(HomeBadge.Count(9), model.state.value.badges[HomeTab.Running])
        assertEquals(QuickFilter.RunningAll, model.state.value.query?.runningFilter)
    }

    @Test fun changingRunningFilterRetainsOtherBadgesThroughoutTheRefresh() = runTest(dispatcher) {
        pending = { emptySet() }
        val result = CompletableDeferred<HomeCount>()
        load = { query, tab ->
            when {
                tab == HomeTab.Running && query.runningFilter == QuickFilter.RunningAll -> result.await()
                tab == HomeTab.Queue -> HomeCount.Available(6)
                else -> counts.getValue(tab)
            }
        }
        val model = model()
        val emitted = mutableListOf<HomeUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.toList(emitted) }
        runCurrent()
        emitted.clear()
        query.value = query.value.copy(runningFilter = QuickFilter.RunningAll)
        runCurrent()
        assertEquals(HomeBadge.Loading, model.state.value.badges[HomeTab.Running])
        result.complete(HomeCount.Available(9))
        runCurrent()
        assertEquals(HomeBadge.Count(9), model.state.value.badges[HomeTab.Running])
        assertTrue(emitted.isNotEmpty())
        emitted.forEach {
            assertEquals(HomeBadge.Count(6), it.badges[HomeTab.Queue])
            assertEquals(HomeBadge.Count(3), it.badges[HomeTab.Agents])
        }
        assertEquals(2, requestedTabs.count { it == HomeTab.Running })
        assertEquals(1, requestedTabs.count { it == HomeTab.Queue })
        assertEquals(1, requestedTabs.count { it == HomeTab.Agents })
    }

    @Test fun changingRunningFilterDoesNotCancelAnUnfinishedQueueCount() = runTest(dispatcher) {
        val queueCount = CompletableDeferred<HomeCount>()
        var queueCancelled = false
        load = { _, tab ->
            if (tab == HomeTab.Queue) {
                try {
                    queueCount.await()
                } catch (error: CancellationException) {
                    queueCancelled = true
                    throw error
                }
            } else {
                counts.getValue(tab)
            }
        }
        val model = model()
        observe(model)
        runCurrent()
        query.value = query.value.copy(runningFilter = QuickFilter.RunningAll)
        runCurrent()
        assertFalse(queueCancelled)
        assertEquals(1, requestedTabs.count { it == HomeTab.Queue })
        queueCount.complete(HomeCount.Available(6))
        runCurrent()
        assertEquals(HomeBadge.Count(6), model.state.value.badges[HomeTab.Queue])
    }

    @Test fun changingQueueOrAgentsFilterOnlyReloadsThatSectionsCount() = runTest(dispatcher) {
        val model = model()
        observe(model)
        runCurrent()
        requestedTabs.clear()
        query.value = query.value.copy(queueFilter = QuickFilter.QueuedAll)
        runCurrent()
        assertEquals(listOf(HomeTab.Queue), requestedTabs)
        assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
        assertEquals(HomeBadge.Count(3), model.state.value.badges[HomeTab.Agents])
        requestedTabs.clear()
        query.value = query.value.copy(agentsFilter = QuickFilter.AgentsDisconnected)
        runCurrent()
        assertEquals(listOf(HomeTab.Agents), requestedTabs)
        assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
    }

    @Test fun savedFavoritesOnlyReloadCountsThatDependOnThem() = runTest(dispatcher) {
        val model = model()
        observe(model)
        runCurrent()
        requestedTabs.clear()
        query.value = query.value.copy(favoriteIds = listOf("saved", "added"))
        runCurrent()
        assertEquals(setOf(HomeTab.Running, HomeTab.Queue), requestedTabs.toSet())
        assertEquals(2, requestedTabs.size)
        assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Favorites])
        query.value = query.value.copy(runningFilter = QuickFilter.RunningAll, queueFilter = QuickFilter.QueuedAll)
        runCurrent()
        requestedTabs.clear()
        query.value = query.value.copy(favoriteIds = listOf("saved"))
        runCurrent()
        assertTrue(requestedTabs.isEmpty())
        assertEquals(HomeBadge.Count(1), model.state.value.badges[HomeTab.Favorites])
    }

    @Test fun switchingAccountsClearsEveryPreviousAccountsBadge() = runTest(dispatcher) {
        val finish = CompletableDeferred<HomeCount>()
        load = { query, tab -> if (query.accountKey == "account-two") finish.await() else counts.getValue(tab) }
        val model = model()
        val emitted = mutableListOf<HomeUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.toList(emitted) }
        runCurrent()
        requestedTabs.clear()
        emitted.clear()
        query.value = query.value.copy(accountKey = "account-two", favoriteIds = emptyList())
        runCurrent()
        assertEquals(setOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents), requestedTabs.toSet())
        emitted.filter { it.query?.accountKey == "account-two" }.forEach { state ->
            listOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents).forEach { assertEquals(HomeBadge.Loading, state.badges[it]) }
            assertEquals(HomeBadge.Count(0), state.badges[HomeTab.Favorites])
        }
        finish.complete(HomeCount.Available(8))
        runCurrent()
        listOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents).forEach { assertEquals(HomeBadge.Count(8), model.state.value.badges[it]) }
    }

    @Test fun savedTabOverridesTheIncomingDefaultAndPersistsSelection() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Queue.ordinal))
        val model = model(handle)
        observe(model)
        runCurrent()
        assertEquals(HomeTab.Queue, model.state.value.selected)
        model.select(HomeTab.Agents)
        runCurrent()
        assertEquals(HomeTab.Agents.ordinal, handle.get<Int>(HomeViewModel.SELECTED_TAB))
        assertEquals(HomeTab.Agents, model.state.value.selected)
    }

    @Test fun activityRestoresItsLastViewAcrossOtherDestinationsAndRecreation() = runTest(dispatcher) {
        pending = { emptySet() }
        val handle = SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Queue.ordinal))
        var model = model(handle)
        var collector = observe(model)
        runCurrent()
        model.select(HomeTab.Projects)
        runCurrent()
        model.selectActivity()
        runCurrent()
        assertEquals(HomeTab.Queue, model.state.value.selected)
        model.select(HomeTab.Agents)
        model.select(HomeTab.Favorites)
        runCurrent()
        collector.cancel()
        store.clear()
        model = model(handle)
        collector = observe(model)
        runCurrent()
        model.selectActivity()
        runCurrent()
        assertEquals(HomeTab.Agents, model.state.value.selected)
        collector.cancel()
    }

    @Test fun invalidSavedActivityViewFallsBackToRunning() = runTest(dispatcher) {
        val model = model(SavedStateHandle(mapOf(HomeViewModel.SELECTED_ACTIVITY_TAB to HomeTab.Favorites.ordinal)))
        observe(model)
        model.selectActivity()
        runCurrent()
        assertEquals(HomeTab.Running, model.state.value.selected)
    }

    @Test fun changingAgentsFilterUpdatesItsBadge() = runTest(dispatcher) {
        val old = CompletableDeferred<HomeCount>()
        load = { query, tab ->
            if (tab == HomeTab.Agents) {
                if (query.agentsFilter == QuickFilter.AgentsConnected) old.await() else HomeCount.Available(7)
            } else {
                counts.getValue(tab)
            }
        }
        val model = model()
        observe(model)
        runCurrent()
        query.value = query.value.copy(agentsFilter = QuickFilter.AgentsDisconnected)
        runCurrent()
        old.complete(HomeCount.Available(3))
        runCurrent()
        assertEquals(1, cancelled)
        assertEquals(HomeBadge.Count(7), model.state.value.badges[HomeTab.Agents])
        assertEquals(QuickFilter.AgentsDisconnected, model.state.value.query?.agentsFilter)
    }

    @Test fun directFiltersPersistIndependentlyAcrossTabsAndViewModelRecreation() = runTest(dispatcher) {
        pending = { emptySet() }
        val handle = SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Running.ordinal))
        var model = model(handle)
        var collector = observe(model)
        runCurrent()
        model.selectFilter(QuickFilter.RunningFavorites)
        model.selectFilter(QuickFilter.QueuedAll)
        assertTrue(applied.isEmpty())
        model.selectFilter(QuickFilter.RunningAll)
        runCurrent()
        model.select(HomeTab.Queue)
        runCurrent()
        model.selectFilter(QuickFilter.QueuedAll)
        runCurrent()
        model.select(HomeTab.Agents)
        runCurrent()
        model.selectFilter(QuickFilter.AgentsDisconnected)
        runCurrent()
        assertEquals(listOf(QuickFilter.RunningAll, QuickFilter.QueuedAll, QuickFilter.AgentsDisconnected), applied)
        collector.cancel()
        store.clear()
        model = model(handle)
        collector = observe(model)
        runCurrent()
        listOf(HomeTab.Running to QuickFilter.RunningAll, HomeTab.Queue to QuickFilter.QueuedAll, HomeTab.Agents to QuickFilter.AgentsDisconnected).forEach { (tab, filter) ->
            model.select(tab)
            runCurrent()
            assertEquals(filter, model.state.value.filterOptions.single { it.selected }.filter)
        }
        collector.cancel()
        assertTrue(events.containsAll(listOf("fab:Running", "fab:Queue", "fab:Agents")))
    }

    @Test fun pendingFilterIgnoresDuplicateTapsAndSurvivesResubscriptionWithoutReapplying() = runTest(dispatcher) {
        val finish = CompletableDeferred<Unit>()
        applyFilter = {
            applied += it
            finish.await()
            query.value = query.value.copy(runningFilter = it)
        }
        val model = model(SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Running.ordinal)))
        val collector = observe(model)
        runCurrent()
        model.selectFilter(QuickFilter.RunningAll)
        model.selectFilter(QuickFilter.RunningAll)
        runCurrent()
        assertEquals(QuickFilter.RunningAll, model.state.value.applyingFilter)
        assertEquals(QuickFilter.RunningFavorites, model.state.value.filterOptions.single { it.selected }.filter)
        collector.cancel()
        runCurrent()
        observe(model)
        runCurrent()
        model.selectFilter(QuickFilter.RunningAll)
        finish.complete(Unit)
        runCurrent()
        assertEquals(listOf(QuickFilter.RunningAll), applied)
        assertNull(model.state.value.applyingFilter)
        assertEquals(QuickFilter.RunningAll, model.state.value.filterOptions.single { it.selected }.filter)
    }

    @Test fun failedFilterRetainsSelectionAndCanBeRetriedFromTheChip() = runTest(dispatcher) {
        applyFilter = {
            applied += it
            if (applied.size == 1) error("storage")
            query.value = query.value.copy(runningFilter = it)
        }
        val model = model(SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Running.ordinal)))
        observe(model)
        runCurrent()
        model.selectFilter(QuickFilter.RunningAll)
        runCurrent()
        assertTrue(model.state.value.filterFailed)
        assertNull(model.state.value.applyingFilter)
        assertEquals(QuickFilter.RunningFavorites, model.state.value.filterOptions.single { it.selected }.filter)
        model.selectFilter(QuickFilter.RunningAll)
        runCurrent()
        assertFalse(model.state.value.filterFailed)
        assertEquals(QuickFilter.RunningAll, model.state.value.filterOptions.single { it.selected }.filter)
    }

    @Test fun accountSwitchCancelsAPendingFilterEvenWhileHomeIsHidden() = runTest(dispatcher) {
        var cancelled = false
        applyFilter = {
            applied += it
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val model = model(SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Running.ordinal)))
        val collector = observe(model)
        runCurrent()
        model.selectFilter(QuickFilter.RunningAll)
        runCurrent()
        collector.cancel()
        runCurrent()
        query.value = query.value.copy(accountKey = "account-two")
        runCurrent()
        assertTrue(cancelled)
        observe(model)
        runCurrent()
        assertNull(model.state.value.applyingFilter)
        assertFalse(model.state.value.filterFailed)
        assertEquals(QuickFilter.RunningFavorites, model.state.value.filterOptions.single { it.selected }.filter)
    }

    @Test fun staleAccountSelectionNeverReachesTheFilterRepository() = runTest(dispatcher) {
        val model = model(SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Running.ordinal)))
        observe(model)
        runCurrent()
        query.value = query.value.copy(accountKey = "account-two")
        model.selectFilter(QuickFilter.RunningAll)
        runCurrent()
        assertTrue(applied.isEmpty())
        assertFalse(model.state.value.filterFailed)
    }

    @Test fun coldQueryIsValidatedBeforeApplyingAFilter() = runTest(dispatcher) {
        queryFlow = flow {
            delay(10)
            emitAll(query)
        }
        val model = model(SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Running.ordinal)))
        observe(model)
        advanceTimeBy(10)
        runCurrent()
        model.selectFilter(QuickFilter.RunningAll)
        runCurrent()
        query.value = query.value.copy(accountKey = "account-two")
        advanceTimeBy(10)
        runCurrent()
        assertTrue(applied.isEmpty())
        assertFalse(model.state.value.filterFailed)
    }

    @Test fun accountPresentationUsesOnlyTheCurrentQueriesPublicIdentity() {
        val state = HomeUiState(query = query.value.copy(account = HomeAccount("andrey.tolpeev", "https://teamcity.example")))
        assertEquals("AT", state.accountInitials)
        assertNull(HomeUiState().accountInitials)
        assertNull(state.copy(query = query.value.copy(account = HomeAccount("._-", "https://teamcity.example"))).accountInitials)
        assertEquals("GU", state.copy(query = query.value.copy(account = HomeAccount("Guest user", "https://teamcity.example"))).accountInitials)
    }

    @Test fun drawerPromptPrecedesTheSelectedTabsPromptAndAcknowledgementsArePersisted() = runTest(dispatcher) {
        val model = model()
        observe(model)
        model.select(HomeTab.Favorites)
        runCurrent()
        assertEquals(HomePrompt.Drawer, model.state.value.prompt?.prompt)
        model.dismissPrompt()
        runCurrent()
        assertEquals(HomePrompt.Favorites, model.state.value.prompt?.prompt)
        model.dismissPrompt()
        runCurrent()
        assertNull(model.state.value.prompt)
        assertEquals(listOf(HomePrompt.Drawer, HomePrompt.Favorites), acknowledgements)
    }

    @Test fun onboardingFailureIsExplicitAndRetryRecoversWithoutBlockingCounts() = runTest(dispatcher) {
        pending = { error("storage") }
        val model = model()
        observe(model)
        runCurrent()
        assertTrue(model.state.value.onboardingFailed)
        assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
        pending = { emptySet() }
        model.retry()
        runCurrent()
        assertFalse(model.state.value.onboardingFailed)
    }

    @Test fun addProjectsUsesTheCurrentTabAndRetainsAnalytics() = runTest(dispatcher) {
        val model = model()
        observe(model)
        model.select(HomeTab.Favorites)
        runCurrent()
        model.fabClicked()
        model.addProjects()
        runCurrent()
        assertEquals(HomeTab.Projects, model.state.value.selected)
        assertTrue("fab:Favorites" in events)
        assertTrue("add" in events)
    }
}
