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

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.libraries.theme.TeamCityTheme

/** Android's accessibility framework checks actual exported nodes and hit targets. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class AccessibilityControlsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun compactNavigationPassesAccessibilityChecksAndHintsDoNotNavigate() = checkNavigation(false)

    @Test fun railNavigationPassesAccessibilityChecksAndHintsDoNotNavigate() = checkNavigation(true)

    private fun checkNavigation(expanded: Boolean) {
        var changes = 0
        compose.enableAccessibilityChecks()
        compose.setContent {
            TeamCityTheme {
                HomeScreen(HomeUiState(), expanded, { changes++ }, {}, {}, {}, remember { SnackbarHostState() }) { Text("Project content") }
            }
        }
        compose.onRoot().tryPerformAccessibilityChecks()
        val favorite = compose.onNodeWithTag("home:tab:Favorites")
        favorite.performTouchInput { longClick() }
        compose.onNode(hasText("Favorites") and hasAnyAncestor(isPopup())).assertIsDisplayed()
        assertEquals(0, changes)
        compose.mainClock.advanceTimeBy(2500)
        favorite.performClick()
        assertEquals(1, changes)
    }
}
