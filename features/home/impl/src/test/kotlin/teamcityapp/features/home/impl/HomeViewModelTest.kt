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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.*
import teamcityapp.features.home.impl.tracker.HomeTracker

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val query = MutableStateFlow(HomeQuery("account-one", listOf("saved"), QuickFilter.RunningFavorites, QuickFilter.QueuedFavorites, QuickFilter.AgentsConnected))
    private val counts = HomeCounts(HomeCount.Available(1), HomeCount.Available(2), HomeCount.Unavailable, HomeCount.Available(3))
    private var calls = 0
    private var cancelled = 0
    private var load: suspend (HomeQuery) -> HomeCounts = { counts }
    private var pending: suspend () -> Set<HomePrompt> = { setOf(HomePrompt.Drawer, HomePrompt.Favorites, HomePrompt.Running) }
    private val acknowledgements = mutableListOf<HomePrompt>()
    private val events = mutableListOf<String>()
    private val repository = object : HomeRepository {
        override val query: Flow<HomeQuery> = this@HomeViewModelTest.query
        override suspend fun counts(query: HomeQuery): HomeCounts {
            calls++
            try {
                return load(query)
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

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun model(handle: SavedStateHandle = SavedStateHandle()) = HomeViewModel(handle, repository, tracker).also { store.put("home", it) }
    private fun TestScope.observe(model: HomeViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect {} }

    @Test fun countsAreCollectionDrivenAndOptionalFailuresRetainOtherBadges() = runTest(dispatcher) {
        val model = model()
        runCurrent()
        Assert.assertEquals(0, calls)
        observe(model)
        runCurrent()
        Assert.assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
        Assert.assertTrue(model.state.value.badges[HomeTab.Queue] is HomeBadge.Failed)
        Assert.assertEquals(HomeBadge.Count(3), model.state.value.badges[HomeTab.Agents])
    }

    @Test fun hidingCancelsUnfinishedCountsAndResubscriptionRetriesThem() = runTest(dispatcher) {
        load = { awaitCancellation() }
        val model = model()
        val collector = observe(model)
        runCurrent()
        Assert.assertEquals(1, calls)
        collector.cancel()
        runCurrent()
        Assert.assertEquals(1, cancelled)
        load = { counts }
        observe(model)
        runCurrent()
        Assert.assertEquals(2, calls)
        Assert.assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
    }

    @Test fun aChangedFilterCancelsTheOldRequestAndOnlyPublishesTheNewCount() = runTest(dispatcher) {
        val old = CompletableDeferred<HomeCounts>()
        load = { if (it.runningFilter == QuickFilter.RunningFavorites) old.await() else counts.copy(running = HomeCount.Available(9)) }
        val model = model()
        observe(model)
        runCurrent()
        query.value = query.value.copy(runningFilter = QuickFilter.RunningAll)
        runCurrent()
        old.complete(counts)
        runCurrent()
        Assert.assertEquals(1, cancelled)
        Assert.assertEquals(HomeBadge.Count(9), model.state.value.badges[HomeTab.Running])
        Assert.assertEquals(QuickFilter.RunningAll, model.state.value.query?.runningFilter)
    }

    @Test fun savedTabOverridesTheIncomingDefaultAndPersistsSelection() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf(HomeViewModel.SELECTED_TAB to HomeTab.Queue.ordinal))
        val model = model(handle)
        observe(model)
        runCurrent()
        Assert.assertEquals(HomeTab.Queue, model.state.value.selected)
        model.select(HomeTab.Agents)
        runCurrent()
        Assert.assertEquals(HomeTab.Agents.ordinal, handle.get<Int>(HomeViewModel.SELECTED_TAB))
        Assert.assertEquals(HomeTab.Agents, model.state.value.selected)
    }

    @Test fun drawerPromptPrecedesTheSelectedTabsPromptAndAcknowledgementsArePersisted() = runTest(dispatcher) {
        val model = model()
        observe(model)
        model.select(HomeTab.Favorites)
        runCurrent()
        Assert.assertEquals(HomePrompt.Drawer, model.state.value.prompt?.prompt)
        model.dismissPrompt()
        runCurrent()
        Assert.assertEquals(HomePrompt.Favorites, model.state.value.prompt?.prompt)
        model.dismissPrompt()
        runCurrent()
        Assert.assertNull(model.state.value.prompt)
        Assert.assertEquals(listOf(HomePrompt.Drawer, HomePrompt.Favorites), acknowledgements)
    }

    @Test fun onboardingFailureIsExplicitAndRetryRecoversWithoutBlockingCounts() = runTest(dispatcher) {
        pending = { error("storage") }
        val model = model()
        observe(model)
        runCurrent()
        Assert.assertTrue(model.state.value.onboardingFailed)
        Assert.assertEquals(HomeBadge.Count(2), model.state.value.badges[HomeTab.Running])
        pending = { emptySet() }
        model.retry()
        runCurrent()
        Assert.assertFalse(model.state.value.onboardingFailed)
    }

    @Test fun addProjectsUsesTheCurrentTabAndRetainsAnalytics() = runTest(dispatcher) {
        val model = model()
        observe(model)
        model.select(HomeTab.Favorites)
        runCurrent()
        model.fabClicked()
        model.addProjects()
        runCurrent()
        Assert.assertEquals(HomeTab.Projects, model.state.value.selected)
        Assert.assertTrue("fab:Favorites" in events)
        Assert.assertTrue("add" in events)
    }
}
