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

package teamcityapp.features.login.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.semantics.SemanticsProperties
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
class LoginScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun guestModeHidesCredentialsAndPreservesThemWhenSwitchingBack() {
        val state = mutableStateOf(LoginUiState(AuthenticationFormState(userName = "Alice", password = "secret"), demoLoading = false))
        compose.setContent { TeamCityTheme { LoginScreen(state.value, { state.value = state.value.copy(form = it) }, {}, {}, {}) } }
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:username").assertDoesNotExist()
        compose.onNodeWithTag("auth:password").assertDoesNotExist()
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:username").assertTextEquals("Username", "Alice")
        assertEquals("secret", state.value.form.password)
    }

    @Test fun accountFieldsKeepAutofillDisabled() {
        compose.setContent { TeamCityTheme { LoginScreen(LoginUiState(demoLoading = false), {}, {}, {}, {}) } }
        listOf("auth:url", "auth:username", "auth:password").forEach { tag ->
            compose.onNodeWithTag(tag).assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDataType, ContentDataType.None))
        }
    }

    @Test fun httpConsentAndSslConsentHaveSeparateCallbacks() {
        var http = 0
        var ssl = 0
        compose.setContent { TeamCityTheme { LoginScreen(LoginUiState(httpConfirmation = true), {}, {}, {}, {}, onConfirm = { ssl++ }, onConfirmHttp = { http++ }) } }
        compose.onNodeWithText("OK").performClick()
        assertEquals(1, http)
        assertEquals(0, ssl)
    }
}
