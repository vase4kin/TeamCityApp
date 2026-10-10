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

package teamcityapp.features.agents.impl

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
import teamcityapp.features.agents.api.Agent
import teamcityapp.features.agents.api.AgentsFilter
import teamcityapp.features.agents.api.AgentsRepository
import teamcityapp.features.agents.impl.router.AgentsRouter
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AgentsRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val router = object : AgentsRouter {
        override fun openDrawer() {}
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
            listOf(Agent("1", "Linux agent"))
        }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { AgentsRoute(router, vm, visible.value) } }
        compose.waitUntil(5_000) { calls.size == 1 }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.waitUntil(5_000) { cancelled }
        assertEquals(1, calls.size)
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls.size == 2 }
        compose.onNodeWithTag("agents:row:1").assertIsDisplayed()
        assertEquals(listOf(false, false), calls)
    }

    @Test fun hidingAndShowingKeepsScrollPositionAndReloadsCompletedContent() {
        val calls = mutableListOf<Boolean>()
        val rows = (1..30).map { Agent("$it", "Agent $it") }
        val vm = viewModel {
            calls += it
            rows
        }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { AgentsRoute(router, vm, visible.value) } }
        compose.onNodeWithTag("agents:list").performScrollToNode(hasTestTag("agents:row:30"))
        compose.onNodeWithTag("agents:row:30").assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        assertEquals(1, calls.size)
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls.size == 2 }
        compose.onNodeWithTag("agents:row:30").assertIsDisplayed()
        assertEquals(listOf(false, false), calls)
    }

    private fun viewModel(load: suspend (Boolean) -> List<Agent>) = AgentsViewModel(object : AgentsRepository {
        override val filter = MutableStateFlow(AgentsFilter.Connected)
        override suspend fun agents(filter: AgentsFilter, forceRefresh: Boolean) = load(forceRefresh)
    }).also { store.put("agents", it) }
}
