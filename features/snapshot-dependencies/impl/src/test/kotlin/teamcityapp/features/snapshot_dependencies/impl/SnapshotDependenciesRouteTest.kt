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

package teamcityapp.features.snapshot_dependencies.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.awaitCancellation
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesRepository
import teamcityapp.features.snapshot_dependencies.impl.router.SnapshotDependenciesRouter
import teamcityapp.features.snapshot_dependencies.impl.tracker.SnapshotDependenciesTracker
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SnapshotDependenciesRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val router = object : SnapshotDependenciesRouter {
        override fun openBuild(build: BuildLaunchData, buildTypeName: String?) {}
        override fun openBuildHistory(configurationId: String, configurationName: String) {}
    }

    @After fun tearDown() {
        store.clear()
    }

    @Test fun hidingResumedTabCancelsItsRequestAndShowingItReloads() {
        val calls = mutableListOf<Boolean>()
        var cancelled = false
        val vm = viewModel { force ->
            calls += force
            if (calls.size == 1) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            listOf(snapshotBuild())
        }
        val visible = mutableStateOf(true)
        compose.setContent {
            TeamCityTheme {
                SnapshotDependenciesRoute(
                    router,
                    object : SnapshotDependenciesTracker {
                        override fun viewShown() {}
                    },
                    vm,
                    visible.value
                )
            }
        }
        compose.waitUntil(5_000) { calls.size == 1 }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.waitUntil(5_000) { cancelled }
        assertEquals(1, calls.size)
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls.size == 2 }
        compose.onNodeWithTag("snapshot:build:1").assertIsDisplayed()
        assertEquals(listOf(false, false), calls)
    }

    @Test fun hidingAndShowingKeepsScrollPositionAndReloadsCompletedContent() {
        val calls = mutableListOf<Boolean>()
        val rows = (1..30).map { snapshotBuild("$it") }
        val vm = viewModel {
            calls += it
            rows
        }
        val visible = mutableStateOf(true)
        compose.setContent {
            TeamCityTheme {
                SnapshotDependenciesRoute(
                    router,
                    object : SnapshotDependenciesTracker {
                        override fun viewShown() {}
                    },
                    vm,
                    visible.value
                )
            }
        }
        compose.onNodeWithTag("snapshot:list").performScrollToNode(hasTestTag("snapshot:build:30"))
        compose.onNodeWithTag("snapshot:build:30").assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        assertEquals(1, calls.size)
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls.size == 2 }
        compose.onNodeWithTag("snapshot:build:30").assertIsDisplayed()
        assertEquals(listOf(false, false), calls)
    }

    @Suppress("DEPRECATION")
    @Test
    fun pagerHintStopsOffscreenCollectionAndRestartsWhenSelected() {
        val fragment = SnapshotDependenciesFragment()
        fragment.userVisibleHint = false
        val calls = mutableListOf<Boolean>()
        var cancelled = false
        val vm = viewModel { force ->
            calls += force
            if (calls.size == 1) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            listOf(snapshotBuild())
        }
        val tracker = object : SnapshotDependenciesTracker {
            override fun viewShown() {}
        }
        compose.setContent { TeamCityTheme { SnapshotDependenciesRoute(router, tracker, vm, fragment.contentVisible) } }
        compose.waitForIdle()
        assertTrue(calls.isEmpty())
        compose.runOnIdle { fragment.userVisibleHint = true }
        compose.waitForIdle()
        assertTrue(fragment.contentVisible)
        compose.waitUntil(5_000) { calls.size == 1 }
        compose.runOnIdle { fragment.userVisibleHint = false }
        compose.waitForIdle()
        assertFalse(fragment.contentVisible)
        compose.waitUntil(5_000) { cancelled }
        compose.runOnIdle { fragment.userVisibleHint = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls.size == 2 }
        compose.onNodeWithTag("snapshot:build:1").assertIsDisplayed()
        assertEquals(listOf(false, false), calls)
    }

    @Suppress("DEPRECATION")
    @Test
    fun hiddenAndPagerHintsMustBothAllowVisibility() {
        val fragment = SnapshotDependenciesFragment()
        assertTrue(fragment.contentVisible)
        fragment.onHiddenChanged(true)
        fragment.userVisibleHint = false
        fragment.userVisibleHint = true
        assertFalse(fragment.contentVisible)
        fragment.onHiddenChanged(false)
        assertTrue(fragment.contentVisible)
        fragment.userVisibleHint = false
        fragment.onHiddenChanged(true)
        fragment.onHiddenChanged(false)
        assertFalse(fragment.contentVisible)
    }

    private fun viewModel(load: suspend (Boolean) -> List<BuildLaunchData>) = SnapshotDependenciesViewModel(
        object : SnapshotDependenciesRepository {
            override suspend fun dependencies(buildId: String, forceRefresh: Boolean) = load(forceRefresh)
        },
        SavedStateHandle(mapOf("id" to "parent"))
    ).also { store.put("snapshot", it) }
}
