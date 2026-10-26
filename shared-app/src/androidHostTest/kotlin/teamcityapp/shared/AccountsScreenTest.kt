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
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.drawer.api.*
import teamcityapp.features.drawer.impl.*
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AccountsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val alice = DrawerAccount(DrawerAccountId("https://teamcity.example", "alice", "opaque-alice"), true, false)
    private val bob = DrawerAccount(DrawerAccountId("https://teamcity.example", "bob", "opaque-bob"), false, false)
    private val state = DrawerUiState(DrawerAccountsUiState.Content(listOf(alice, bob)))

    @Test fun nativeSelectionRetainsTheExactPublicAccountKey() {
        val selected = mutableListOf<DrawerAccountId>()
        compose.setContent { TeamCityTheme { SharedAccountScreen(state, selected::add, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag(accountTag(bob.id)).performScrollTo().performClick()
        assertEquals(listOf(bob.id), selected)
    }

    @Test fun nativeAccountHasDistinctAddAndManageActions() {
        var add = 0
        var manage = 0
        compose.setContent { TeamCityTheme { SharedAccountScreen(state, {}, { add++ }, { manage++ }, {}, {}, {}, {}) } }
        compose.onNodeWithTag("drawer:add").performScrollTo().performClick()
        compose.onNodeWithTag("drawer:manage").performScrollTo().performClick()
        assertEquals(1, add)
        assertEquals(1, manage)
        compose.onNodeWithTag("accounts:sign-out").assertDoesNotExist()
        compose.onNodeWithTag("accounts:row:${alice.id.identityKey}:remove").assertDoesNotExist()
    }

    @Test fun busyAccountDisablesManagementActions() {
        compose.setContent { TeamCityTheme { SharedAccountScreen(state.copy(selection = AccountSwitchUiState.SigningOut), {}, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("accounts:sign-out").assertDoesNotExist()
        compose.onNodeWithTag("drawer:list").performScrollToNode(hasTestTag("drawer:add"))
        compose.onNodeWithTag("drawer:add").assertIsNotEnabled()
    }

    @Test fun emptySignedOutAccountsKeepAddAndManageWithoutSignOut() {
        compose.setContent { TeamCityTheme { SharedAccountScreen(DrawerUiState(DrawerAccountsUiState.Empty), {}, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("drawer:add").performScrollTo().assertIsEnabled()
        compose.onNodeWithTag("drawer:manage").performScrollTo().assertIsEnabled()
        compose.onNodeWithTag("accounts:sign-out").assertDoesNotExist()
    }
}
