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

package teamcityapp.features.about.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AboutScreenVersionTest {
    @get:Rule val compose = createComposeRule()
    private val content = AboutUiState.Content(ServerDetailsUiState.Unavailable)

    @Test fun rendersTheCurrentApplicationVersion() {
        compose.setContent {
            TeamCityTheme { SharedAboutScreen(content, {}, {}, {}, appVersion = ABOUT_APP_VERSION) }
        }
        compose.onNodeWithText(ABOUT_APP_VERSION).assertIsDisplayed()
    }

    @Test fun rendersAnExplicitVersionForDeterministicFixtures() {
        compose.setContent {
            TeamCityTheme { SharedAboutScreen(content, {}, {}, {}, appVersion = "9.87.65") }
        }
        compose.onNodeWithText("9.87.65").assertIsDisplayed()
        compose.onNodeWithContentDescription("Application version 9.87.65").assertIsDisplayed()
        compose.onNodeWithText(ABOUT_APP_VERSION).assertDoesNotExist()
    }

    @Test fun androidLibrariesActionRemainsAvailable() {
        val actions = mutableListOf<AboutAction>()
        compose.setContent { TeamCityTheme { SharedAboutScreen(content, {}, actions::add, {}, "1.2.3") } }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Open sources libraries"))
        compose.onNodeWithText("Open sources libraries").performClick()
        assertEquals(listOf(AboutAction.Libraries), actions)
    }

    @Test fun iosConfigurationKeepsSourceCodeWithoutLibraries() {
        compose.setContent { TeamCityTheme { SharedAboutScreen(content, {}, {}, {}, "1.2.3", showOpenSourceLibraries = false) } }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Source code"))
        compose.onNodeWithText("Source code").assertIsDisplayed()
        compose.onNodeWithText("Open sources libraries").assertDoesNotExist()
    }

    @Test fun serverLinkKeepsItsDescriptiveLabelAndAction() {
        val url = "https://teamcity.example"
        var opened: String? = null
        val available = AboutUiState.Content(ServerDetailsUiState.Available(ServerDetailsUiModel("2026.1", url)))
        compose.setContent {
            TeamCityTheme { SharedAboutScreen(available, { opened = it }, {}, {}, appVersion = ABOUT_APP_VERSION) }
        }
        compose.onNodeWithText(url).assertIsDisplayed()
        compose.onNodeWithContentDescription("Server url, $url").performClick()
        assertEquals(url, opened)
    }
}
