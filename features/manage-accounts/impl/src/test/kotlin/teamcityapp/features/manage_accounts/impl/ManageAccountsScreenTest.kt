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

package teamcityapp.features.manage_accounts.impl

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ManageAccountsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val normal = ManagedAccount(ManagedAccountId("https://server", "Alice"), false, false)
    private val ssl = ManagedAccount(ManagedAccountId("https://server", "Bob"), true, true)
    private val state = ManageAccountsUiState(AccountListUiState.Content(listOf(normal, ssl)))

    @Test fun accountIdentityIsNonDestructiveAndRemoveIsExplicit() {
        val removed = mutableListOf<ManagedAccountId>()
        var warnings = 0
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state, { removed += it }, { warnings++ }, {}, {}) } }
        compose.onNodeWithTag(accountTag(normal.id)).assertHasNoClickAction()
        compose.onNodeWithTag("${accountTag(normal.id)}:remove").performClick()
        compose.onNodeWithTag("${accountTag(ssl.id)}:ssl").assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(listOf(normal.id), removed)
        assertEquals(1, warnings)
    }

    @Test fun emptyStateStillAllowsAddingAnAccountAndBack() {
        var added = 0
        var closed = 0
        compose.setContent { TeamCityTheme { ManageAccountsScreen(ManageAccountsUiState(AccountListUiState.Empty), {}, {}, { added++ }, { closed++ }) } }
        compose.onNodeWithTag("accounts:empty").assertExists()
        compose.onNodeWithTag("accounts:add").assertHeightIsEqualTo(56.dp).performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, added)
        assertEquals(1, closed)
    }

    @Test fun removalDisablesAccountActionsAndShowsProgress() {
        var events = 0
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state.copy(removal = AccountRemovalUiState.Removing(normal.id)), { events++ }, { events++ }, { events++ }, {}) } }
        compose.onNodeWithTag("accounts:removing").assertExists()
        listOf(normal, ssl).forEach { account ->
            compose.onNodeWithTag("${accountTag(account.id)}:remove").assertIsNotEnabled().performClick()
        }
        compose.onNodeWithTag("${accountTag(ssl.id)}:ssl").assertIsNotEnabled().performClick()
        compose.onNodeWithTag("accounts:add").performClick()
        assertEquals(0, events)
    }

    @Test fun loadFailureOffersRetryAndCleanupFailureOffersItsOwnRetry() {
        var load = 0
        var removal = 0
        val ui = mutableStateOf(ManageAccountsUiState(AccountListUiState.Error))
        compose.setContent { TeamCityTheme { ManageAccountsScreen(ui.value, {}, {}, {}, {}, { load++ }, { removal++ }) } }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, load)
        compose.runOnIdle { ui.value = ManageAccountsUiState(AccountListUiState.Empty, AccountRemovalUiState.Error(normal.id)) }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, removal)
    }

    @Test fun cancelDoesNotRemoveAndConfirmationUsesTheSelectedIdentity() {
        var cancelled = 0
        val removed = mutableListOf<ManagedAccountId>()
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state, {}, {}, {}, {}, dialog = ManageAccountsDialog.ConfirmRemoval(ssl.id), onDismissDialog = { cancelled++ }, onConfirmRemoval = { removed += it }) } }
        compose.onNodeWithText("Cancel").performClick()
        assertTrue(removed.isEmpty())
        assertEquals(1, cancelled)
        compose.onNodeWithText("Remove").performClick()
        assertEquals(listOf(ssl.id), removed)
    }

    @Test fun sslWarningUsesOriginalTextAndOkDismissesIt() {
        var dismissed = 0
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state, {}, {}, {}, {}, dialog = ManageAccountsDialog.SslWarning, onDismissDialog = { dismissed++ }) } }
        compose.onNodeWithText("Warning").assertIsDisplayed()
        compose.onNodeWithText("man-in-the-middle attacks", substring = true).assertIsDisplayed()
        compose.onNodeWithText("OK").performClick()
        assertEquals(1, dismissed)
    }

    @Test fun selectedAccountDialogSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        val removed = mutableListOf<ManagedAccountId>()
        restoration.setContent {
            var dialog by rememberSaveable(stateSaver = accountDialogSaver) { mutableStateOf<ManageAccountsDialog>(ManageAccountsDialog.None) }
            TeamCityTheme { ManageAccountsScreen(state, { dialog = ManageAccountsDialog.ConfirmRemoval(it) }, {}, {}, {}, dialog = dialog, onConfirmRemoval = { removed += it }) }
        }
        compose.onNodeWithTag("${accountTag(ssl.id)}:remove").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("accounts:dialog").assertIsDisplayed()
        compose.onNodeWithText("Remove").performClick()
        assertEquals(listOf(ssl.id), removed)
    }

    @Test fun accountUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(titleMedium = TextStyle(fontSize = 22.sp), bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                ManageAccountsScreen(state, {}, {}, {}, {})
            }
        }
        assertThemeText("Alice", 22, Color.Red)
        assertThemeText("https://server", 18, Color.Blue)
    }

    @Test fun warningDialogUsesProvidedTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(titleMedium = TextStyle(fontSize = 22.sp), bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                ManageAccountsScreen(state, {}, {}, {}, {}, dialog = ManageAccountsDialog.SslWarning)
            }
        }
        assertThemeText("Warning", 28, Color.Red)
    }

    @Test fun removalErrorReservesSpaceForLastAccountActionsAtDoubleText() {
        var warnings = 0
        var removed: ManagedAccountId? = null
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(360.dp, 600.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                    TeamCityTheme { ManageAccountsScreen(state.copy(removal = AccountRemovalUiState.Error(normal.id)), { removed = it }, { warnings++ }, {}, {}) }
                }
            }
        }
        val feedback = compose.onNodeWithTag("accounts:remove_error")
        fun assertAboveFeedback(tag: String) {
            val action = compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
            assertTrue(action.fetchSemanticsNode().boundsInRoot.bottom <= feedback.fetchSemanticsNode().boundsInRoot.top)
            action.performClick()
        }
        assertAboveFeedback("${accountTag(ssl.id)}:ssl")
        assertEquals(1, warnings)
        assertAboveFeedback("${accountTag(ssl.id)}:remove")
        assertEquals(ssl.id, removed)
        compose.onNodeWithText("Try again").assertIsDisplayed()
        compose.onNodeWithTag("accounts:add").assertIsDisplayed()
    }

    @Test fun shortLoadFailureKeepsScrolledRetryAboveTheAddAction() {
        var retried = 0
        var created = 0
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(360.dp, 400.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                    TeamCityTheme { ManageAccountsScreen(ManageAccountsUiState(AccountListUiState.Error), {}, {}, { created++ }, {}, onRetry = { retried++ }) }
                }
            }
        }
        val retry = compose.onNodeWithText("Try again").performScrollTo().assertIsDisplayed()
        val add = compose.onNodeWithTag("accounts:add").assertIsDisplayed()
        assertTrue(retry.fetchSemanticsNode().boundsInRoot.bottom <= add.fetchSemanticsNode().boundsInRoot.top)
        retry.performClick()
        add.performClick()
        assertEquals(1, retried)
        assertEquals(1, created)
    }

    @Test fun shortAccountScreenBoundsFeedbackAndKeepsBothRecoveryAndRowsReachable() {
        var retried = 0
        var removed: ManagedAccountId? = null
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(360.dp, 400.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                    TeamCityTheme { ManageAccountsScreen(state.copy(removal = AccountRemovalUiState.Error(normal.id)), { removed = it }, {}, {}, {}, onRetryRemoval = { retried++ }) }
                }
            }
        }
        val body = compose.onNodeWithTag("accounts:body").fetchSemanticsNode().boundsInRoot
        val feedback = compose.onNodeWithTag("accounts:remove_error").fetchSemanticsNode().boundsInRoot
        assertTrue(feedback.height <= body.height / 3f + 1f)
        compose.onNodeWithText("Try again").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, retried)
        compose.onNodeWithTag("${accountTag(ssl.id)}:remove").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(ssl.id, removed)
        compose.onNodeWithTag("accounts:add").assertIsDisplayed()
    }

    @Test fun cornerRemoveHasContextualLabelAndOneCallbackPerTargetEdge() {
        val removed = mutableListOf<ManagedAccountId>()
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state, { removed += it }, {}, {}, {}) } }
        listOf(normal, ssl).forEach { account ->
            compose.onNodeWithTag(accountTag(account.id)).assertHasNoClickAction().performTouchInput { click(center) }
            val remove = compose.onNodeWithTag("${accountTag(account.id)}:remove")
            remove.assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
                .assertContentDescriptionEquals("Remove ${account.id.userName} account at ${account.id.serverUrl}")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            remove.performTouchInput { click(Offset(1f, height / 2f)) }
            remove.performTouchInput { click(Offset(width - 1f, height / 2f)) }
        }
        assertEquals(listOf(normal.id, normal.id, ssl.id, ssl.id), removed)
    }

    @Test fun currentCapsuleIsPassiveAndRemoveTooltipDescribesItsExactAccount() {
        val removed = mutableListOf<ManagedAccountId>()
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state, { removed += it }, {}, {}, {}) } }
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Current account")).assertHasNoClickAction()
        compose.onNodeWithTag("${accountTag(ssl.id)}:current", useUnmergedTree = true).assertHasNoClickAction()
            .assert(SemanticsMatcher("Passive decorative capsule") { !it.config.contains(SemanticsProperties.Text) && !it.config.contains(SemanticsProperties.Role) })
        compose.onNodeWithText("Active account").assertDoesNotExist()
        compose.onNodeWithTag("${accountTag(normal.id)}:remove").performTouchInput { longClick() }
        compose.onNodeWithText("Remove account").assertIsDisplayed()
        assertTrue(removed.isEmpty())
    }

    @Test fun longNamedIdentityKeepsRemoveAndFullWidthUrlReadableAtDoubleTextAndRtl() {
        val long = ManagedAccount(ManagedAccountId("https://a-very-long-teamcity-server.example/projects/production", "alexander.morgan.platform"), true, false)
        var removals = 0
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(360.dp, 800.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TeamCityTheme { ManageAccountsScreen(ManageAccountsUiState(AccountListUiState.Content(listOf(long))), { removals++ }, {}, {}, {}) }
                    }
                }
            }
        }
        compose.onNodeWithTag(accountTag(long.id)).assertHasNoClickAction()
        val icon = compose.onNodeWithTag("${accountTag(long.id)}:remove").assertIsDisplayed().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        val name = compose.onNodeWithText(long.id.userName, useUnmergedTree = true)
        val url = compose.onNodeWithTag("${accountTag(long.id)}:url", useUnmergedTree = true)
        val nameBounds = name.fetchSemanticsNode().boundsInRoot
        val urlBounds = url.fetchSemanticsNode().boundsInRoot
        assertTrue(urlBounds.width > nameBounds.width)
        assertTrue(icon.fetchSemanticsNode().boundsInRoot.right <= nameBounds.left)
        listOf(name, url).forEach { node ->
            val results = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            assertFalse(results.single().hasVisualOverflow)
        }
        compose.onNodeWithTag("${accountTag(long.id)}:current", useUnmergedTree = true).assertIsDisplayed().assertHasNoClickAction()
        icon.performClick()
        assertEquals(1, removals)
    }

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }
}
