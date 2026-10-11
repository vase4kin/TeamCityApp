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

package teamcityapp.features.build_queue.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.build_queue.api.BuildQueueRepository
import teamcityapp.features.build_queue.impl.router.BuildQueueRouter
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildQueueRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val router = object : BuildQueueRouter {
        override fun openDrawer() {}
        override fun openBuild(build: BuildLaunchData) {}
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
            listOf(buildRow())
        }
        val visible = mutableStateOf(true)
        compose.setContent {
            TeamCityTheme {
                BuildQueueRoute(
                    router,
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
        compose.onNodeWithTag("build_queue:build:1").assertIsDisplayed()
        assertEquals(listOf(false, false), calls)
    }

    @Test fun hidingAndShowingKeepsScrollPositionAndReloadsCompletedContent() {
        val calls = mutableListOf<Boolean>()
        val rows = (1..30).map { buildRow("$it") }
        val vm = viewModel {
            calls += it
            rows
        }
        val visible = mutableStateOf(true)
        compose.setContent {
            TeamCityTheme {
                BuildQueueRoute(
                    router,
                    vm,
                    visible.value
                )
            }
        }
        compose.onNodeWithTag("build_queue:list").performScrollToNode(hasTestTag("build_queue:build:30"))
        compose.onNodeWithTag("build_queue:build:30").assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        assertEquals(1, calls.size)
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls.size == 2 }
        compose.onNodeWithTag("build_queue:build:30").assertIsDisplayed()
        assertEquals(listOf(false, false), calls)
    }

    private fun viewModel(load: suspend (Boolean) -> List<BuildLaunchData>) = BuildQueueViewModel(object : BuildQueueRepository {
        override val query = MutableStateFlow(teamcityapp.features.build_queue.api.BuildQueueQuery("account", teamcityapp.features.build_queue.api.BuildQueueFilter.All))
        override suspend fun builds(query: teamcityapp.features.build_queue.api.BuildQueueQuery, forceRefresh: Boolean) = load(forceRefresh)
    }).also { store.put("builds", it) }
}
