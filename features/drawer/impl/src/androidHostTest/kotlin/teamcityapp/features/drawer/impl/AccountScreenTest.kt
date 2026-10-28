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
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AccountScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun accountHeaderReturnsToHomeAndSwitchingDisablesBack() {
        val state = mutableStateOf(drawerFixture("mixed"))
        var backs = 0
        compose.setContent { TeamCityTheme { AccountScreen(state.value, {}, {}, {}, {}, {}, {}, {}, onNavigateUp = { backs++ }) } }
        compose.onNodeWithText("Account").assertIsDisplayed()
        compose.onNodeWithTag("account:back").performClick()
        assertEquals(1, backs)
        compose.runOnIdle { state.value = state.value.copy(selection = AccountSwitchUiState.Switching(inactive.id)) }
        compose.onNodeWithTag("account:back").assertIsNotEnabled().performClick()
        assertEquals(1, backs)
    }

    @Test fun accountRetainsLoadAndSwitchFailureActions() {
        val state = mutableStateOf(drawerFixture("error"))
        var retries = 0
        var selectionRetries = 0
        compose.setContent { TeamCityTheme { AccountScreen(state.value, {}, {}, {}, {}, {}, {}, {}, onRetry = { retries++ }, onRetrySelection = { selectionRetries++ }) } }
        compose.onNodeWithTag("drawer:load_error").onChildren().filterToOne(hasClickAction()).performClick()
        assertEquals(1, retries)
        compose.runOnIdle { state.value = drawerFixture("switch_error") }
        compose.onNodeWithTag("drawer:switch_error").performScrollTo().onChildren().filterToOne(hasClickAction()).performClick()
        assertEquals(1, selectionRetries)
    }
}
