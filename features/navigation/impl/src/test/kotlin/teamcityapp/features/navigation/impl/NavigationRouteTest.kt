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

package teamcityapp.features.navigation.impl

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
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
import teamcityapp.features.navigation.api.*
import teamcityapp.features.navigation.impl.router.NavigationRouter
import teamcityapp.features.navigation.impl.tracker.NavigationTracker
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private var launches = 0
    private val router = object : NavigationRouter {
        override fun navigateUp() {}
        override fun open(entry: NavigationEntry) {}
        override fun openRating() {
            launches++
        }
    }
    private val tracker = object : NavigationTracker {
        override fun viewShown() {}
        override fun ratingShown() {}
        override fun ratingCancelled() {}
        override fun ratingSelected() {}
    }

    @After fun tearDown() {
        store.clear()
    }

    @Test fun hiddenHomeTabCancelsPendingLoadAndShowingItResumes() {
        var calls = 0
        var cancelled = false
        val vm = viewModel {
            calls++
            if (calls == 1) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            listOf(NavigationEntry.Project(ProjectReference("child", "Child")))
        }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { NavigationRoute(router, visible.value, vm) } }
        compose.waitUntil(5_000) { calls == 1 }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.waitUntil(5_000) { cancelled }
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls == 2 }
        compose.onNodeWithText("Child").assertIsDisplayed()
    }

    @Test fun hiddenCompletedHomeTabKeepsRowsAndScrollWithoutReloading() {
        var calls = 0
        val rows = (1..30).map { NavigationEntry.Project(ProjectReference("$it", "Project $it")) }
        val vm = viewModel {
            calls++
            rows
        }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { NavigationRoute(router, visible.value, vm) } }
        compose.onNodeWithTag("navigation:list").performScrollToNode(hasTestTag("navigation:row:project:30"))
        compose.onNodeWithTag("navigation:row:project:30").assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.onNodeWithTag("navigation:row:project:30").assertIsDisplayed()
        assertEquals(1, calls)
    }

    @Test fun storeLaunchIsNotRepeatedWhenRouteReturns() {
        val vm = viewModel(eligible = true) { listOf(NavigationEntry.Project(ProjectReference("child", "Child"))) }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { NavigationRoute(router, visible.value, vm) } }
        compose.onNodeWithText("Rate", useUnmergedTree = true).performClick()
        compose.waitUntil(5_000) { launches == 1 }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        assertEquals(1, launches)
        compose.onNodeWithTag("navigation:rating").assertDoesNotExist()
    }

    @Test fun pendingStoreLaunchWaitsForResumedLifecycleAndLaunchesOnce() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        val saved = CompletableDeferred<Unit>()
        val vm = NavigationViewModel(
            SavedStateHandle(),
            object : NavigationRepository {
                override suspend fun entries(projectId: String, forceRefresh: Boolean) = listOf(NavigationEntry.Project(ProjectReference("child", "Child")))
            },
            object : NavigationRatingRepository {
                override suspend fun isEligible() = true
                override suspend fun markHandled() {
                    saved.await()
                }
            },
            tracker
        ).also { store.put("navigation", it) }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                TeamCityTheme { NavigationRoute(router, true, viewModel = vm) }
            }
        }
        compose.onNodeWithText("Rate", useUnmergedTree = true).performClick()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        compose.runOnIdle { saved.complete(Unit) }
        compose.waitForIdle()
        assertEquals(0, launches)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { launches == 1 }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        assertEquals(1, launches)
    }

    @Test fun returningToRootAfterExternalGlobalDismissalHidesRatingWithoutReloadingRows() {
        var handled = false
        var calls = 0
        val vm = viewModel(ratingEligibility = { !handled }) {
            calls++
            listOf(NavigationEntry.Project(ProjectReference("child", "Child")))
        }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { NavigationRoute(router, visible.value, vm) } }
        compose.onNodeWithTag("navigation:rating").assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle {
            handled = true
            visible.value = true
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("navigation:rating").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Child").assertIsDisplayed()
        assertEquals(1, calls)
    }

    private fun viewModel(eligible: Boolean = false, ratingEligibility: suspend () -> Boolean = { eligible }, load: suspend () -> List<NavigationEntry>) = NavigationViewModel(
        SavedStateHandle(),
        object : NavigationRepository {
            override suspend fun entries(projectId: String, forceRefresh: Boolean) = load()
        },
        object : NavigationRatingRepository {
            override suspend fun isEligible() = ratingEligibility()
            override suspend fun markHandled() {}
        },
        tracker
    ).also { store.put("navigation", it) }
}
