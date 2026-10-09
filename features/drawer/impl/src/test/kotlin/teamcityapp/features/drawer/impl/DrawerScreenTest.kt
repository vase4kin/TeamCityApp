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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
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
        currentAccount().assertIsDisplayed()
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
        listOf("add", "manage", "settings", "about", "privacy", "rate").forEach { compose.onNodeWithTag("drawer:$it").performScrollTo().performClick() }
        assertEquals(listOf("add", "manage", "settings", "about", "privacy", "rate"), events)
    }

    @Test fun switchingDisablesAllOutgoingActions() {
        var events = 0
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("switching"), { events++ }, { events++ }, { events++ }, { events++ }, { events++ }, { events++ }, { events++ }) } }
        compose.onNodeWithTag("drawer:switching").assertExists()
        compose.onNodeWithTag(accountTag(inactive.id)).assertIsNotEnabled()
        listOf("add", "manage", "settings", "about", "privacy", "rate").forEach { compose.onNodeWithTag("drawer:$it").performScrollTo().assertIsNotEnabled().performClick() }
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
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue, onPrimaryContainer = Color.Green), typography = Typography(titleMedium = TextStyle(fontSize = 22.sp), bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                DrawerScreen(drawerFixture("mixed"), {}, {}, {}, {}, {}, {}, {})
            }
        }
        assertThemeText(active.id.userName, 22, Color.Green)
        assertThemeText(active.id.serverUrl, 18, Color.Green)
    }

    @Test fun inactiveAccountAndMenuEdgesBelongToTheirActions() {
        val events = mutableListOf<String>()
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("mixed"), { events += "account" }, { events += "add" }, { events += "manage" }, { events += "settings" }, { events += "about" }, { events += "privacy" }, { events += "rate" }) } }
        val list = compose.onNodeWithTag("drawer:list").fetchSemanticsNode().boundsInRoot
        listOf(accountTag(inactive.id), "drawer:add", "drawer:settings", "drawer:about", "drawer:rate", "drawer:privacy").forEach { tag ->
            val row = compose.onNodeWithTag(tag).performScrollTo()
            row.assertIsDisplayed().assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Button))
            val bounds = row.fetchSemanticsNode().boundsInRoot
            assertEquals(list.left + 16f, bounds.left)
            assertEquals(list.right - 16f, bounds.right)
            row.performTouchInput { click(androidx.compose.ui.geometry.Offset(1f, height / 2f)) }
            row.performTouchInput { click(androidx.compose.ui.geometry.Offset(width - 1f, height / 2f)) }
        }
        assertEquals(listOf("account", "account", "add", "add", "settings", "settings", "about", "about", "rate", "rate", "privacy", "privacy"), events)
    }

    @Test fun currentCardIsInformationalAndGroupedActionsHaveDeliberateSpacing() {
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("one"), {}, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag(accountTag(active.id)).assertHasNoClickAction()
        currentAccount().assertIsDisplayed()
        val card = compose.onNodeWithTag("drawer:active-card").fetchSemanticsNode().boundsInRoot
        val add = compose.onNodeWithTag("drawer:add").fetchSemanticsNode().boundsInRoot
        assertTrue(add.top - card.bottom >= 8f)
        val settings = compose.onNodeWithTag("drawer:settings").fetchSemanticsNode().boundsInRoot
        val about = compose.onNodeWithTag("drawer:about").fetchSemanticsNode().boundsInRoot
        assertEquals(2f, about.top - settings.bottom, 0.5f)
        val rate = compose.onNodeWithTag("drawer:rate").fetchSemanticsNode().boundsInRoot
        val privacy = compose.onNodeWithTag("drawer:privacy").fetchSemanticsNode().boundsInRoot
        assertEquals(2f, rate.top - about.bottom, 0.5f)
        assertEquals(2f, privacy.top - rate.bottom, 0.5f)
        compose.onNodeWithTag("drawer:manage").assertHeightIsAtLeast(teamcityapp.libraries.theme.TeamCityDimensions.minimumTouchTarget)
    }

    @Test fun multilineCurrentIdentityAndManageRemainReadableAtDoubleFontScale() {
        val previous = org.robolectric.RuntimeEnvironment.getFontScale()
        org.robolectric.RuntimeEnvironment.setFontScale(2f)
        try {
            var manages = 0
            compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("long"), {}, {}, { manages++ }, {}, {}, {}, {}) } }
            compose.onNodeWithTag("drawer:manage").assertIsDisplayed().assertHeightIsAtLeast(teamcityapp.libraries.theme.TeamCityDimensions.minimumTouchTarget).performClick()
            assertEquals(1, manages)
            currentAccount().assertIsDisplayed()
            compose.onNodeWithTag("drawer:current-marker", useUnmergedTree = true).assertIsDisplayed().assertHasNoClickAction()
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText("https://a-very-long-teamcity-server.example/projects/production/1", useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertFalse(layouts.single().hasVisualOverflow)
            compose.onNodeWithTag("drawer:list").performScrollToKey("footer")
            compose.onNodeWithTag("drawer:rate").assertIsDisplayed()
            val privacy = compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val list = compose.onNodeWithTag("drawer:list").fetchSemanticsNode().boundsInRoot
            assertTrue(privacy.top >= list.top && privacy.bottom <= list.bottom)
        } finally {
            org.robolectric.RuntimeEnvironment.setFontScale(previous)
        }
    }

    @Test fun onlyActualCurrentAccountExposesCurrentStateAndSslWarningRemainsExplicit() {
        val state = mutableStateOf(drawerFixture("no_active"))
        compose.setContent { TeamCityTheme { DrawerScreen(state.value, {}, {}, {}, {}, {}, {}, {}) } }
        currentAccount().assertDoesNotExist()
        compose.onNodeWithText("SSL verification disabled").assertExists()
        compose.runOnIdle { state.value = DrawerUiState(DrawerAccountsUiState.Content(listOf(active.copy(isSslDisabled = true)))) }
        currentAccount().assertIsDisplayed()
        compose.onNodeWithText("SSL verification disabled").assertIsDisplayed()
        compose.onNodeWithTag(accountTag(active.id)).assertHasNoClickAction()
    }

    @Test fun compactManageRetainsItsOwnAccessibleEdgeAction() {
        var manages = 0
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("one"), {}, {}, { manages++ }, {}, {}, {}, {}) } }
        val manage = compose.onNodeWithTag("drawer:manage")
        manage.assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Button))
            .assertHeightIsAtLeast(teamcityapp.libraries.theme.TeamCityDimensions.minimumTouchTarget)
        manage.performTouchInput { click(androidx.compose.ui.geometry.Offset(1f, height / 2f)) }
        manage.performTouchInput { click(androidx.compose.ui.geometry.Offset(width - 1f, height / 2f)) }
        assertEquals(2, manages)
    }

    @Test fun markerIsPassiveAndSupportRowsAreFullWidthInRtl() {
        val events = mutableListOf<String>()
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                TeamCityTheme { DrawerScreen(drawerFixture("one"), {}, {}, {}, {}, {}, { events += "privacy" }, { events += "rate" }) }
            }
        }
        currentAccount().assertHasNoClickAction()
        compose.onNodeWithText("Current account").assertDoesNotExist()
        val marker = compose.onNodeWithTag("drawer:current-marker", useUnmergedTree = true)
        marker.assertIsDisplayed().assertHasNoClickAction().assert(SemanticsMatcher("Passive status has no control role") { !it.config.contains(SemanticsProperties.Role) && !it.config.contains(SemanticsProperties.ToggleableState) })
        marker.assert(SemanticsMatcher("Decorative capsule has no spoken text") { !it.config.contains(SemanticsProperties.Text) && !it.config.contains(SemanticsProperties.ContentDescription) })
        assertTrue(marker.fetchSemanticsNode().boundsInRoot.width > 28f)
        val name = compose.onNodeWithText(active.id.userName, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(marker.fetchSemanticsNode().boundsInRoot.right < name.left)
        val list = compose.onNodeWithTag("drawer:list").fetchSemanticsNode().boundsInRoot
        listOf("rate", "privacy").forEach { action ->
            val row = compose.onNodeWithTag("drawer:$action").performScrollTo()
            row.assertHeightIsAtLeast(teamcityapp.libraries.theme.TeamCityDimensions.minimumTouchTarget)
            val bounds = row.fetchSemanticsNode().boundsInRoot
            assertEquals(list.left + 16f, bounds.left)
            assertEquals(list.right - 16f, bounds.right)
            row.performTouchInput { click(androidx.compose.ui.geometry.Offset(1f, height / 2f)) }
            row.performTouchInput { click(androidx.compose.ui.geometry.Offset(width - 1f, height / 2f)) }
        }
        assertEquals(listOf("rate", "rate", "privacy", "privacy"), events)
    }

    @Test fun namedCurrentCapsuleFitsBesideShortNameWithoutDuplicatingAccessibilityText() {
        compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("named"), {}, {}, {}, {}, {}, {}, {}) } }
        currentAccount().assertHasNoClickAction()
        val name = compose.onNodeWithText("alex.morgan", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val marker = compose.onNodeWithTag("drawer:current-marker", useUnmergedTree = true)
        marker.assertIsDisplayed().assertHasNoClickAction()
            .assert(SemanticsMatcher("Capsule is decorative") { !it.config.contains(SemanticsProperties.Text) && !it.config.contains(SemanticsProperties.Role) })
        val capsule = marker.fetchSemanticsNode().boundsInRoot
        assertTrue(capsule.left > name.right)
        assertEquals(name.top, capsule.top, 0.5f)
    }

    @Test fun longNamedCapsuleWrapsWholeBelowNameAtDoubleFontScale() {
        val previous = org.robolectric.RuntimeEnvironment.getFontScale()
        org.robolectric.RuntimeEnvironment.setFontScale(2f)
        try {
            compose.setContent { TeamCityTheme { DrawerScreen(drawerFixture("named_long"), {}, {}, {}, {}, {}, {}, {}) } }
            currentAccount().assertIsDisplayed().assertHasNoClickAction()
            val name = compose.onNodeWithText("alexander.morgan.platform", useUnmergedTree = true)
            val nameBounds = name.fetchSemanticsNode().boundsInRoot
            val capsule = compose.onNodeWithTag("drawer:current-marker", useUnmergedTree = true).assertIsDisplayed().assertHasNoClickAction().fetchSemanticsNode().boundsInRoot
            assertTrue(capsule.top >= nameBounds.bottom)
            val url = compose.onNodeWithText(active.id.serverUrl, useUnmergedTree = true)
            assertTrue(url.fetchSemanticsNode().boundsInRoot.top >= capsule.bottom)
            assertEquals(nameBounds.left, url.fetchSemanticsNode().boundsInRoot.left, 0.5f)
            listOf(name, url).forEach { node ->
                val results = mutableListOf<TextLayoutResult>()
                node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                assertFalse(results.single().hasVisualOverflow)
            }
            val privacy = compose.onNodeWithTag("drawer:privacy").performScrollTo().assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val list = compose.onNodeWithTag("drawer:list").fetchSemanticsNode().boundsInRoot
            assertTrue(privacy.top >= list.top && privacy.bottom <= list.bottom)
        } finally {
            org.robolectric.RuntimeEnvironment.setFontScale(previous)
        }
    }

    private fun currentAccount() = compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Current account"))

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }
}
