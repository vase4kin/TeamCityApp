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

package teamcityapp.shared

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.libraries.accounts.AccountIdentity
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AccountsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val alice = AccountIdentity("https://teamcity.example", "alice", false)
    private val bob = alice.copy(userName = "bob")
    private val state = AccountsUiState(ListUiState.Content(listOf(alice, bob)), activeKey = alice.key)

    @Test fun accountSelectionDispatchesTheExactUserOnTheSameServer() {
        val selected = mutableListOf<String>()
        compose.setContent { TeamCityTheme { SharedAccountsScreen(state, selected::add, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("accounts:activate:${bob.key}").performScrollTo().performClick()
        assertEquals(listOf(bob.key), selected)
    }

    @Test fun removalRequiresConfirmationAndCancelDoesNotDelete() {
        val removed = mutableListOf<String>()
        compose.setContent { TeamCityTheme { SharedAccountsScreen(state, {}, removed::add, {}, {}, {}, {}) } }
        compose.onNodeWithTag("accounts:remove:${alice.key}").performClick()
        compose.onNodeWithText("Remove saved account?").assertIsDisplayed()
        assertTrue(removed.isEmpty())
        compose.onNodeWithText("Cancel").performClick()
        assertTrue(removed.isEmpty())
        compose.onNodeWithTag("accounts:remove:${alice.key}").performClick()
        compose.onNodeWithTag("accounts:confirm-remove").performClick()
        assertEquals(listOf(alice.key), removed)
    }

    @Test fun busyOperationsDisableAccountChanges() {
        compose.setContent { TeamCityTheme { SharedAccountsScreen(state.copy(busy = true), {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("accounts:activate:${alice.key}").assertIsNotEnabled()
        compose.onNodeWithTag("accounts:remove:${alice.key}").assertIsNotEnabled()
        compose.onNodeWithTag("accounts:add").assertIsNotEnabled()
        compose.onNodeWithTag("accounts:sign-out").assertIsNotEnabled()
    }

    @Test fun anOptionalWriteFailureKeepsRowsAndDispatchesRetry() {
        var retries = 0
        compose.setContent { TeamCityTheme { SharedAccountsScreen(state.copy(mutationFailed = true), {}, {}, {}, { retries++ }, {}, {}) } }
        compose.onNodeWithText("alice").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }

    @Test fun emptyAccountsKeepAddAvailableAndHideSignOut() {
        var added = 0
        compose.setContent { TeamCityTheme { SharedAccountsScreen(AccountsUiState(ListUiState.Empty()), {}, {}, {}, {}, { added++ }, {}) } }
        compose.onNodeWithText("No saved accounts").assertIsDisplayed()
        compose.onNodeWithTag("accounts:sign-out").assertDoesNotExist()
        compose.onNodeWithTag("accounts:add").performClick()
        assertEquals(1, added)
    }
}
