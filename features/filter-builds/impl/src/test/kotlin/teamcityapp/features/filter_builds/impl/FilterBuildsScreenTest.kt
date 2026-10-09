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

package teamcityapp.features.filter_builds.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.filter_builds.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FilterBuildsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun applyFabRetainsItsLabelSizeAndAction() {
        var applied = 0
        compose.setContent { TeamCityTheme { FilterBuildsScreen(FilterBuildsUiState(branches = emptyList()), {}, { applied++ }, {}, {}) } }
        compose.onNodeWithTag("filter-builds:apply").assertHeightIsEqualTo(56.dp).performClick()
        compose.onNodeWithText("APPLY FILTERS").assertIsDisplayed()
        assertEquals(1, applied)
    }

    @Test fun queuedFilterHidesPinnedWithoutChangingTheSavedChoice() {
        val state = mutableStateOf(FilterBuildsUiState(branches = emptyList(), filter = BuildFilter(pinned = true)))
        compose.setContent { TeamCityTheme { FilterBuildsScreen(state.value, { state.value = state.value.copy(filter = it) }, {}, {}, {}, dialog = true) } }
        compose.onNodeWithText("Queued").performClick()
        compose.onNodeWithTag("filter-builds:pinned").assertDoesNotExist()
        assertTrue(state.value.filter.pinned)
        assertEquals(BuildStatusFilter.Queued, state.value.filter.status)
    }

    @Test fun singleBranchOffersTheAnyBranchDefault() {
        compose.setContent { TeamCityTheme { FilterBuildsScreen(FilterBuildsUiState(branches = listOf("main")), {}, {}, {}, {}) } }
        compose.onNodeWithTag("branches:input").assertDoesNotExist()
        compose.onNodeWithText("No branches available to filter").assertIsDisplayed()
    }

    @Test fun optionTilesAcceptTouchesInAllTheirPadding() {
        val state = mutableStateOf(FilterBuildsUiState(branches = emptyList(), filter = BuildFilter()))
        var changes = 0
        compose.setContent {
            TeamCityTheme {
                FilterBuildsScreen(state.value, {
                    changes++
                    state.value = state.value.copy(filter = it)
                }, {}, {}, {})
            }
        }
        val screenBounds = compose.onNodeWithTag("filter-builds:scroll").fetchSemanticsNode().boundsInRoot
        listOf("personal", "pinned").forEach { option ->
            val row = compose.onNodeWithTag("filter-builds:$option")
            val rowBounds = row.fetchSemanticsNode().boundsInRoot
            assertEquals(screenBounds.left, rowBounds.left, 0f)
            assertEquals(screenBounds.right, rowBounds.right, 0f)
            row.assertHeightIsEqualTo(72.dp)
            repeat(4) { edge ->
                val before = changes
                row.performTouchInput {
                    click(
                        when (edge) {
                            0 -> Offset(1f, center.y)
                            1 -> Offset(width - 1f, center.y)
                            2 -> Offset(center.x, 1f)
                            else -> Offset(center.x, height - 1f)
                        }
                    )
                }
                compose.runOnIdle { assertEquals(before + 1, changes) }
            }
        }
    }

    @Test fun lightBackgroundContinuesBelowTheFormToTheBottom() {
        assertBackgroundContinuesToBottom(dark = false)
    }

    @Test fun darkBackgroundContinuesBelowTheFormToTheBottom() {
        assertBackgroundContinuesToBottom(dark = true)
    }

    private fun assertBackgroundContinuesToBottom(dark: Boolean) {
        compose.setContent { TeamCityTheme(darkTheme = dark, legacyColors = true) { FilterBuildsScreen(FilterBuildsUiState(branches = emptyList()), {}, {}, {}, {}) } }
        val expected = Color(if (dark) 0xFF000000 else 0xFFF5F5F5)
        val pixels = compose.onNodeWithTag("filter-builds:scroll").captureToImage().toPixelMap()
        assertEquals(expected, pixels[8, pixels.height - 8])
        assertEquals(Color(if (dark) 0xFF121212 else 0xFFFFFFFF), pixels[8, 16])
        val root = compose.onRoot().captureToImage().toPixelMap()
        assertEquals(expected, root[8, root.height - 8])
    }
}
