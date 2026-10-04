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

package teamcityapp.features.drawer.impl
import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.drawer.api.*
import teamcityapp.libraries.theme.TeamCityTheme
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DrawerScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun activeAccountIsFirstAndCannotBeSelected() {
        val selected = mutableListOf<DrawerAccountId>()
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("mixed"), { selected += it }, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag(accountTag(active.id)).assertHasNoClickAction()
        compose.onNodeWithContentDescription("Active account").assertIsDisplayed()
        compose.onNodeWithTag(accountTag(inactive.id)).performClick()
        assertEquals(listOf(inactive.id), selected)
        val activeTop = compose.onNodeWithTag(accountTag(active.id)).fetchSemanticsNode().boundsInRoot.top
        val inactiveTop = compose.onNodeWithTag(accountTag(inactive.id)).fetchSemanticsNode().boundsInRoot.top
        assertTrue(activeTop < inactiveTop)
    }

    @Test fun accountsSharingServerStillHaveSeparateActions() {
        val other = active.copy(id = active.id.copy(userName = "Other"), isActive = false)
        val selected = mutableListOf<DrawerAccountId>()
        compose.setContent { TeamCityTheme { DrawerScreen(DrawerUiState(DrawerAccountsUiState.Content(listOf(active, other))), { selected += it }, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag(accountTag(other.id)).performClick()
        assertEquals(listOf(other.id), selected)
    }

    @Test fun everyMenuAndFooterActionHasItsOwnCallback() {
        val events = mutableListOf<String>()
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("empty"), {}, { events += "add" }, { events += "manage" }, { events += "settings" }, { events += "about" }, { events += "privacy" }, { events += "rate" }) } }
        listOf("add", "manage", "settings", "about", "privacy", "rate").forEach { compose.onNodeWithTag("drawer:$it").performClick() }
        assertEquals(listOf("add", "manage", "settings", "about", "privacy", "rate"), events)
    }

    @Test fun switchingDisablesAllOutgoingActions() {
        var events = 0
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("switching"), { events++ }, { events++ }, { events++ }, { events++ }, { events++ }, { events++ }, { events++ }) } }
        compose.onNodeWithTag("drawer:switching").assertExists()
        compose.onNodeWithTag(accountTag(inactive.id)).assertIsNotEnabled()
        listOf("add", "manage", "settings", "about", "privacy", "rate").forEach { compose.onNodeWithTag("drawer:$it").assertIsNotEnabled().performClick() }
        assertEquals(0, events)
    }

    @Test fun loadAndSwitchFailuresHaveIndependentRetryActions() {
        var loads = 0
        var switches = 0
        val state = mutableStateOf(drawerFixture("error"))
        compose.setContent { TeamCityTheme { DrawerScreen(state.value, {}, {}, {}, {}, {}, {}, {}, onRetry = { loads++ }, onRetrySelection = { switches++ }) } }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, loads)
        compose.runOnIdle { state.value = drawerFixture("switch_error") }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, switches)
    }

    @Test fun missingAccountMessageCanBeDismissed() {
        var dismissals = 0
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("missing"), {}, {}, {}, {}, {}, {}, {}, onDismissMissing = { dismissals++ }) } }
        compose.onNodeWithText("This account is no longer available.").assertIsDisplayed()
        compose.onNodeWithText("Dismiss").performClick()
        assertEquals(1, dismissals)
    }

    @Test fun manyWrappedAccountsCanScrollToAllFooterActions() {
        var events = 0
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("long"), {}, {}, {}, {}, {}, { events++ }, { events++ }) } }
        compose.onNodeWithTag("drawer:list").performScrollToKey("footer")
        compose.onNodeWithTag("drawer:privacy").assertIsDisplayed().performClick()
        compose.onNodeWithTag("drawer:rate").assertIsDisplayed().performClick()
        assertEquals(2, events)
    }

    @Test fun accountUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                DrawerScreen(drawerFixture("mixed"), {}, {}, {}, {}, {}, {}, {})
            }
        }
        assertThemeText(active.id.userName, 22, Color.Red)
        assertThemeText(active.id.serverUrl, 18, Color.Blue)
    }

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }
}
