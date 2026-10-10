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

package teamcityapp.features.build_history.impl

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.build_history.api.BuildHistoryPage
import teamcityapp.features.build_history.impl.router.BuildHistoryRouter
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildHistoryRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val repository = FakeHistoryRepository()
    private val tracker = FakeHistoryTracker()
    private val onboarding = FakeHistoryOnboarding()
    private val router = FakeRouter()

    // History collects at RESUMED; test lifecycle transitions remain explicit.
    private val resumedOwner = object : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle = registry
    }

    @After fun cleanup() {
        store.clear()
    }
    private fun viewModel() = BuildHistoryViewModel(SavedStateHandle(mapOf("id" to "configuration", "name" to "Build Android")), repository, onboarding, tracker).also { store.put("history", it) }

    @Test fun rowLaunchesCompleteSnapshotAndRunFilterUseCurrentConfiguration() {
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:build:42").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("history:build:42").performClick()
        compose.onNodeWithTag("history:run").performClick()
        compose.onNodeWithTag("history:filter").performClick()
        assertEquals(listOf(historyBuild()), router.builds)
        assertEquals("Build Android", router.name)
        assertEquals("configuration", router.runId)
        assertEquals("configuration", router.filterId)
        assertEquals(1, tracker.runs)
    }

    @Test fun initialFailureRetryForcesRequestAndRetainsEffectiveLocator() {
        repository.loadPage = { if (it.force) BuildHistoryPage(listOf(historyBuild())) else error("offline") }
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Try again").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:build:42").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
    }

    @Test fun completedEmptySurvivesRecreationAndFailedRefresh() {
        repository.loadPage = { if (it.force) error("offline") else BuildHistoryPage(emptyList()) }
        val vm = viewModel()
        val restoration = StateRestorationTester(compose)
        restoration.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("No builds").fetchSemanticsNodes().isNotEmpty() }
        restoration.emulateSavedInstanceStateRestore()
        compose.waitForIdle()
        assertEquals(1, repository.requests.size)
        compose.onRoot().performTouchInput {
            swipe(start = center.copy(y = height * .2f), end = center.copy(y = height * .9f), durationMillis = 600)
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Couldn’t refresh. Try again.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("No builds").assertIsDisplayed()
        compose.onNodeWithText("Oops").assertDoesNotExist()
    }

    @Test fun queuedResultKeepsShowActionDuringRefreshAndLaunchesFullBuild() {
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { repository.requests.isNotEmpty() }
        compose.runOnIdle { vm.onQueuedBuildResult("/queued") }
        compose.waitUntil(5_000) { repository.requests.size == 2 }
        compose.onNodeWithText("Build is added to build queue").assertIsDisplayed()
        compose.onNodeWithText("Show").performClick()
        compose.waitUntil(5_000) { router.builds.isNotEmpty() }
        assertEquals(listOf(historyBuild("queued")), router.builds)
        assertEquals(listOf("/queued"), repository.queuedRequests)
        assertTrue(repository.requests.last().force)
    }

    @Test fun pendingQueuedShowWaitsForResumedLifecycleAndLaunchesOnlyOnce() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        val response = CompletableDeferred<BuildLaunchData>()
        repository.loadQueued = { response.await() }
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.runOnIdle {
            vm.onQueuedBuildResult("/queued")
            vm.openQueuedBuild()
        }
        compose.waitUntil(5_000) { repository.queuedRequests.isNotEmpty() }
        compose.onNodeWithTag("history:opening-progress").assertIsDisplayed()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        compose.onNodeWithTag("history:opening-progress").assertDoesNotExist()
        compose.runOnIdle { response.complete(historyBuild("queued")) }
        compose.waitForIdle()
        assertTrue(router.builds.isEmpty())
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { router.builds.size == 1 }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        assertEquals(1, router.builds.size)
    }

    @Test fun queryReplacementHidesOldRowsDuringNewPendingRequest() {
        repository.loadPage = { if (it.query.locator == "running:true") awaitCancellation() else BuildHistoryPage(listOf(historyBuild())) }
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:build:42").fetchSemanticsNodes().isNotEmpty() }
        compose.runOnIdle { vm.applyFilter("running:true") }
        compose.waitUntil(5_000) { repository.requests.any { it.query.locator == "running:true" } }
        compose.onNodeWithTag("history:build:42").assertDoesNotExist()
    }

    @Test fun actualReturnUpdatesFavoriteAndDismissedGlobalPromptWithoutReloadingRows() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        onboarding.pending = { listOf(teamcityapp.features.build_history.api.BuildHistoryPrompt.Run) }
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:coachmark:Run").fetchSemanticsNodes().isNotEmpty() }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        compose.runOnIdle {
            repository.loadFavorite = { true }
            onboarding.pending = { emptyList() }
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.waitForIdle()
        compose.waitUntil(5_000) { vm.controls.value.favorite == FavoriteState.Available(true) && vm.controls.value.onboarding == OnboardingState.Available() }
        compose.onNodeWithTag("history:coachmark:Run").assertDoesNotExist()
        compose.onNodeWithTag("history:build:42").assertIsDisplayed()
        compose.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
        assertEquals(1, repository.requests.size)
        assertEquals(2, repository.favoriteCalls)
        assertEquals(2, onboarding.loads)
    }

    @Test fun pendingInitialFailureIsNotRenderedUntilResumeAndRetryUsesCurrentQuery() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        val response = CompletableDeferred<BuildHistoryPage>()
        repository.loadPage = { if (it.force) BuildHistoryPage(listOf(historyBuild())) else response.await() }
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { repository.requests.size == 1 }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.waitForIdle()
        compose.runOnIdle { response.completeExceptionally(IllegalStateException("offline while hidden")) }
        compose.waitForIdle()
        compose.onNodeWithText("Try again").assertDoesNotExist()
        assertTrue(router.builds.isEmpty())
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Try again").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:build:42").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
    }

    @Test fun pendingAppendErrorIsHiddenAndScrollAndPagesSurviveReturn() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        val response = CompletableDeferred<BuildHistoryPage>()
        var recovered = false
        repository.loadPage = {
            if (it.next == null) {
                BuildHistoryPage((0..9).map { id -> historyBuild(id.toString()) }, "opaque-next")
            } else if (recovered) {
                BuildHistoryPage((10..19).map { id -> historyBuild(id.toString()) })
            } else {
                response.await()
            }
        }
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("history:list").performScrollToIndex(8)
        compose.waitUntil(5_000) { repository.requests.size == 2 }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.waitForIdle()
        compose.runOnIdle { response.completeExceptionally(IllegalStateException("hidden append failure")) }
        compose.waitForIdle()
        compose.onNodeWithTag("history:list").assertDoesNotExist()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:build:8").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("history:build:8").assertIsDisplayed()
        assertEquals(2, repository.requests.size)
        compose.onNodeWithTag("history:list").performScrollToNode(hasText("Retry"))
        recovered = true
        compose.onNodeWithText("Retry").performClick()
        compose.waitUntil(5_000) { repository.requests.size == 3 }
        compose.onNodeWithTag("history:list").performScrollToNode(hasTestTag("history:build:18"))
        compose.onNodeWithTag("history:build:18").assertIsDisplayed()
        assertEquals(listOf(false, true, true), repository.requests.map { it.force })
    }

    @Test fun contextualOnboardingIsDisposedWhilePausedAndRestoredOnResume() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        onboarding.pending = { listOf(teamcityapp.features.build_history.api.BuildHistoryPrompt.Run) }
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { BuildHistoryRoute(router, vm) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:coachmark:Run").fetchSemanticsNodes().isNotEmpty() }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.waitForIdle()
        compose.onNodeWithTag("history:coachmark:Run").assertDoesNotExist()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("history:coachmark:Run").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("history:coachmark:Run").assertIsDisplayed()
        assertTrue(onboarding.shown.isEmpty())
    }

    private class FakeRouter : BuildHistoryRouter {
        val builds = mutableListOf<BuildLaunchData>()
        var name = ""
        var runId = ""
        var filterId = ""
        override fun navigateUp() {}
        override fun openBuild(build: BuildLaunchData, configurationName: String) {
            builds += build
            name = configurationName
        }
        override fun openRunBuild(configurationId: String) {
            runId = configurationId
        }
        override fun openFilterBuilds(configurationId: String) {
            filterId = configurationId
        }
        override fun openFavorites() {}
    }
}
