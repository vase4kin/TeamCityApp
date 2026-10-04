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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
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

    @Test fun rowsWithSameServerUseFullIdentityAndWarningHasASeparateAction() {
        val removed = mutableListOf<ManagedAccountId>()
        var warnings = 0
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state, { removed += it }, { warnings++ }, {}, {}) } }
        compose.onNodeWithTag(accountTag(normal.id)).performClick()
        compose.onNodeWithText("Bob", useUnmergedTree = true).performClick()
        compose.onAllNodesWithText("https://server", useUnmergedTree = true)[1].performClick()
        compose.onNodeWithText("Ignore SSL certificate validity is enabled for this account", useUnmergedTree = true).performClick()
        assertEquals(listOf(normal.id, ssl.id, ssl.id), removed)
        assertEquals(1, warnings)
    }

    @Test fun emptyStateStillAllowsAddingAnAccountAndBack() {
        var added = 0
        var closed = 0
        compose.setContent { TeamCityTheme { ManageAccountsScreen(ManageAccountsUiState(AccountListUiState.Empty), {}, {}, { added++ }, { closed++ }) } }
        compose.onNodeWithTag("accounts:empty").assertExists()
        compose.onNodeWithContentDescription("Add account").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, added)
        assertEquals(1, closed)
    }

    @Test fun removalDisablesAccountActionsAndShowsProgress() {
        var events = 0
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state.copy(removal = AccountRemovalUiState.Removing(normal.id)), { events++ }, { events++ }, { events++ }, {}) } }
        compose.onNodeWithTag("accounts:removing").assertExists()
        compose.onNodeWithTag(accountTag(normal.id)).assertIsNotEnabled()
        compose.onNodeWithTag("accounts:add").performClick()
        assertEquals(0, events)
    }

    @Test fun loadFailureOffersRetryAndCleanupFailureOffersItsOwnRetry() {
        var load = 0
        var removal = 0
        val ui = mutableStateOf(ManageAccountsUiState(AccountListUiState.Error))
        compose.setContent { TeamCityTheme { ManageAccountsScreen(ui.value, {}, {}, {}, {}, { load++ }, { removal++ }) } }
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, load)
        compose.runOnIdle { ui.value = ManageAccountsUiState(AccountListUiState.Empty, AccountRemovalUiState.Error(normal.id)) }
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, removal)
    }

    @Test fun cancelDoesNotRemoveAndConfirmationUsesTheSelectedIdentity() {
        var cancelled = 0
        val removed = mutableListOf<ManagedAccountId>()
        compose.setContent { TeamCityTheme { ManageAccountsScreen(state, {}, {}, {}, {}, dialog = ManageAccountsDialog.ConfirmRemoval(ssl.id), onDismissDialog = { cancelled++ }, onConfirmRemoval = { removed += it }) } }
        compose.onNodeWithText("NOPE").performClick()
        assertTrue(removed.isEmpty())
        assertEquals(1, cancelled)
        compose.onNodeWithText("SURE").performClick()
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
        compose.onNodeWithText("Bob", useUnmergedTree = true).performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("accounts:dialog").assertIsDisplayed()
        compose.onNodeWithText("SURE").performClick()
        assertEquals(listOf(ssl.id), removed)
    }

    @Test fun accountUsesProvidedColorsAndTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                ManageAccountsScreen(state, {}, {}, {}, {})
            }
        }
        assertThemeText("Alice", 22, Color.Red)
        assertThemeText("https://server", 18, Color.Blue)
    }

    @Test fun warningDialogUsesProvidedTypography() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(onSurface = Color.Red, onSurfaceVariant = Color.Blue), typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))) {
                ManageAccountsScreen(state, {}, {}, {}, {}, dialog = ManageAccountsDialog.SslWarning)
            }
        }
        assertThemeText("Warning", 28, Color.Red)
    }

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }
}
