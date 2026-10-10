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

package teamcityapp.features.build_log.impl

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
import teamcityapp.features.build_log.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildLogScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sslAccountOpensItsExactUrlInTheBrowserWithoutLoadingWebContent() {
        var browser: String? = null
        var webCreated = 0
        val url = "https://server/viewLog.html?buildId=123"
        compose.setContent { TeamCityTheme { BuildLogScreen(BuildLogUiState.Session(BuildLogSession(url, sslDisabled = true)), {}, {}, { browser = it }, webContent = { _, _ -> webCreated++ }) } }
        compose.onNodeWithTag("build-log:browser").performClick()
        assertEquals(url, browser)
        assertEquals(0, webCreated)
    }

    @Test fun authenticationConsentPrecedesCreatingWebContent() {
        var authenticated = 0
        var webCreated = 0
        compose.setContent { TeamCityTheme { BuildLogScreen(BuildLogUiState.Session(BuildLogSession("https://server", needsAuthentication = true)), {}, { authenticated++ }, {}, webContent = { _, _ -> webCreated++ }) } }
        compose.onNodeWithTag("build-log:authenticate").performClick()
        assertEquals(1, authenticated)
        assertEquals(0, webCreated)
    }

    @Test fun authenticationFailureKeepsSignInActionSeparateFromPageRetry() {
        var authenticated = 0
        var retried = 0
        var webCreated = 0
        val state = BuildLogUiState.Session(BuildLogSession("https://server", needsAuthentication = true), authenticationFailed = true)
        compose.setContent { TeamCityTheme { BuildLogScreen(state, { retried++ }, { authenticated++ }, {}, webContent = { _, _ -> webCreated++ }) } }
        compose.onNodeWithText("Couldn't sign in to the build log. Sign in again to continue.").assertIsDisplayed()
        compose.onNodeWithText("Sign in").performClick()
        assertEquals(1, authenticated)
        assertEquals(0, retried)
        assertEquals(0, webCreated)
    }
}
