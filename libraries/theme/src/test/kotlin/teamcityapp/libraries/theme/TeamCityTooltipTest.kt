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

package teamcityapp.libraries.theme

import android.app.Application
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class TeamCityTooltipTest {
    @get:Rule val compose = createComposeRule()

    @Test fun accessibilityLongClickExplainsTheButtonWithoutActivatingIt() {
        var clicks = 0
        compose.setContent {
            TeamCityTheme {
                val label = stringResource(R.string.action_close)
                TeamCityTooltip(label) {
                    IconButton({ clicks++ }) { Icon(painterResource(R.drawable.ic_close_black_24dp), label) }
                }
            }
        }
        val button = compose.onNodeWithContentDescription("Close")
        button.assertTouchWidthIsEqualTo(48.dp).assertTouchHeightIsEqualTo(48.dp)
        button.performSemanticsAction(SemanticsActions.OnLongClick) { it() }
        compose.onNode(hasText("Close") and hasAnyAncestor(isPopup())).assertIsDisplayed()
        assertEquals(0, clicks)
        compose.mainClock.advanceTimeBy(2500)
        compose.onNode(isPopup()).assertDoesNotExist()
        button.performClick()
        assertEquals(1, clicks)
    }

    @Test fun keyboardFocusExplainsTheAnchorWithoutTrappingFocus() {
        compose.setContent {
            TeamCityTheme {
                Row {
                    TeamCityTooltip("Close") {
                        IconButton({}) { Icon(painterResource(R.drawable.ic_close_black_24dp), "Close") }
                    }
                    TeamCityTooltip("Back") {
                        IconButton({}) { Icon(painterResource(R.drawable.ic_arrow_back_24dp), "Back") }
                    }
                }
            }
        }
        val first = compose.onNodeWithContentDescription("Close")
        first.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNode(hasText("Close") and hasAnyAncestor(isPopup())).assertIsDisplayed()
        first.performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithContentDescription("Back").assertIsFocused()
        compose.onNode(hasText("Back") and hasAnyAncestor(isPopup())).assertIsDisplayed()
    }

    @Test fun mouseHoverExplainsTheButtonWithoutClickingIt() {
        var clicks = 0
        compose.setContent {
            TeamCityTheme {
                TeamCityTooltip("Close") {
                    IconButton({ clicks++ }) { Icon(painterResource(R.drawable.ic_close_black_24dp), "Close") }
                }
            }
        }
        val button = compose.onNodeWithContentDescription("Close")
        button.performMouseInput { enter(center) }
        compose.onNode(hasText("Close") and hasAnyAncestor(isPopup())).assertIsDisplayed()
        assertEquals(0, clicks)
        button.performMouseInput { exit() }
        compose.mainClock.advanceTimeBy(500)
        compose.onNode(isPopup()).assertDoesNotExist()
    }

    @Test fun disablingTooltipInputPreservesTheButtonClick() {
        var clicks = 0
        compose.setContent {
            TeamCityTheme {
                TeamCityTooltip("Close", enabled = false) {
                    IconButton({ clicks++ }) { Icon(painterResource(R.drawable.ic_close_black_24dp), "Close") }
                }
            }
        }
        val button = compose.onNodeWithContentDescription("Close")
        button.performTouchInput { longClick() }
        compose.onNode(isPopup()).assertDoesNotExist()
        // With tooltip gestures disabled, releasing the press retains IconButton's normal click.
        assertEquals(1, clicks)
        button.performClick()
        assertEquals(2, clicks)
    }
}
