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

package teamcityapp.features.filter_bottom_sheet.impl

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
import teamcityapp.features.filter_bottom_sheet.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FilterBottomSheetScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun applyingDisablesTheActionAndFailureAllowsRetry() {
        var applied = 0
        val state = mutableStateOf(FilterBottomSheetUiState(applying = true))
        compose.setContent { TeamCityTheme { FilterBottomSheetScreen(state.value) { applied++ } } }
        compose.onNodeWithTag("quick-filter:apply").assertIsNotEnabled().performClick()
        assertEquals(0, applied)
        compose.runOnIdle { state.value = state.value.copy(applying = false, failed = true) }
        compose.onNodeWithTag("quick-filter:apply").assertIsEnabled().performClick()
        assertEquals(1, applied)
    }

    @Test fun currentModeIsSelectedAndOnlyAlternativeChangesTheFilter() {
        var applied = 0
        compose.setContent { TeamCityTheme { FilterBottomSheetScreen(FilterBottomSheetUiState(filter = QuickFilter.AgentsDisconnected)) { applied++ } } }
        compose.onNodeWithTag("quick-filter:selected").assertIsSelected().assertHasNoClickAction()
        compose.onNodeWithTag("quick-filter:apply").assertIsNotSelected().performClick()
        assertEquals(1, applied)
    }

    @Test fun alternativeModeHasFullWidthEdgeHitTargets() {
        var applied = 0
        compose.setContent { TeamCityTheme { FilterBottomSheetScreen(FilterBottomSheetUiState()) { applied++ } } }
        val content = compose.onNodeWithTag("quick-filter:content").fetchSemanticsNode().boundsInRoot
        val row = compose.onNodeWithTag("quick-filter:apply")
        val bounds = row.fetchSemanticsNode().boundsInRoot
        assertEquals(content.left, bounds.left)
        assertEquals(content.right, bounds.right)
        row.performTouchInput { click(androidx.compose.ui.geometry.Offset(1f, height / 2f)) }
        row.performTouchInput { click(androidx.compose.ui.geometry.Offset(width - 1f, height / 2f)) }
        assertEquals(2, applied)
    }
}
