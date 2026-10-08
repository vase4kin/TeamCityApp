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

package teamcityapp.features.create_account.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CreateAccountScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun discardDialogCanBeCancelledOrConfirmed() {
        var discarded = 0
        var cancelled = 0
        compose.setContent { TeamCityTheme { CreateAccountScreen(CreateAccountUiState(), {}, {}, {}, {}, dialog = CreateAccountDialog.Discard, onConfirm = { discarded++ }, onDecline = { cancelled++ }) } }
        compose.onNodeWithText("CANCEL").performClick()
        assertEquals(1, cancelled)
        assertEquals(0, discarded)
        compose.onNodeWithText("DISCARD").performClick()
        assertEquals(1, discarded)
    }

    @Test fun savingDisablesFieldsAndToolbarSubmission() {
        var submitted = 0
        compose.setContent { TeamCityTheme { CreateAccountScreen(CreateAccountUiState(AuthenticationFormState(busy = true)), {}, { submitted++ }, {}, {}) } }
        compose.onNodeWithTag("auth:url").assertIsNotEnabled()
        compose.onNodeWithTag("auth:progress").assertExists()
        assertEquals(0, submitted)
    }
}
