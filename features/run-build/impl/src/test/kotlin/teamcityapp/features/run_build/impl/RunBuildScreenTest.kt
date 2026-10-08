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

package teamcityapp.features.run_build.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RunBuildScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun personalSwitchOnlyChangesItsOptionAndQueueUsesItsOwnCallback() {
        val state = mutableStateOf(RunBuildUiState(branches = emptyList(), agents = emptyList(), request = BuildRequest("bt1")))
        var queued = 0
        compose.setContent { TeamCityTheme { RunBuildScreen(state.value, { state.value = state.value.copy(request = it) }, { queued++ }, {}, {}, {}, {}) } }
        compose.onNodeWithTag("run-build:personal").performClick()
        assertTrue(state.value.request.personal)
        assertTrue(state.value.request.cleanSources)
        assertFalse(state.value.request.queueAtTop)
        compose.onNodeWithTag("run-build:submit").performClick()
        assertEquals(1, queued)
    }

    @Test fun agentDialogReturnsTheSelectedAgentIdentity() {
        val agents = listOf(BuildAgent("1", "Linux"), BuildAgent("2", "Windows"))
        var selected: BuildAgent? = null
        compose.setContent { TeamCityTheme { RunBuildScreen(RunBuildUiState(agents = agents), {}, {}, {}, {}, {}, {}, agentDialog = true, onAgentSelected = { selected = it }) } }
        compose.onNodeWithText("Windows").performClick()
        assertEquals(agents[1], selected)
    }
}
