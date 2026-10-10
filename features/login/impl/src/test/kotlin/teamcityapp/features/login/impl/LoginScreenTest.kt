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
import androidx.compose.ui.unit.dp
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

    @Config(qualifiers = "en-rUS-w1000dp-h1000dp-notnight-mdpi")
    @Test
    fun tabletSwitchRowsStayWithin560dpFormPane() {
        compose.setContent { TeamCityTheme { LoginScreen(LoginUiState(), {}, {}, {}, {}) } }
        compose.onNodeWithTag("login:form").assertWidthIsEqualTo(560.dp)
        listOf("auth:guest", "auth:ssl").forEach { compose.onNodeWithTag(it).assertWidthIsEqualTo(560.dp) }
    }

    @Test fun switchRowsSpanTheBoundedFormPane() {
        compose.setContent { TeamCityTheme { LoginScreen(LoginUiState(), { }, { }, { }, { }) } }
        val pane = compose.onNodeWithTag("login:form").fetchSemanticsNode().boundsInRoot
        listOf("auth:guest", "auth:ssl").forEach { tag ->
            val row = compose.onNodeWithTag(tag)
            row.assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Switch))
            val bounds = row.fetchSemanticsNode().boundsInRoot
            assertEquals(pane.left, bounds.left)
            assertEquals(pane.right, bounds.right)
        }
    }

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

    @Test fun usernameValidationBelongsToUsernameField() {
        compose.setContent { TeamCityTheme { LoginScreen(LoginUiState(form = AuthenticationFormState(error = AuthenticationError.EmptyUserName), demoLoading = false), {}, {}, {}, {}) } }
        compose.onNodeWithTag("auth:username").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        compose.onNodeWithTag("auth:url").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
        compose.onNodeWithTag("auth:password").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
        compose.onNodeWithTag("auth:error", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun shortWindowWithDoubleTextKeepsSubmitReachableAndImeSubmitsOnce() {
        var submits = 0
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(androidx.compose.ui.unit.DpSize(360.dp, 400.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                    TeamCityTheme { LoginScreen(LoginUiState(demoLoading = false), {}, { submits++ }, {}, {}) }
                }
            }
        }
        compose.onNodeWithTag("auth:password").performScrollTo().performImeAction()
        assertEquals(1, submits)
        compose.onNodeWithTag("login:submit").performScrollTo().assertIsDisplayed()
    }
}
