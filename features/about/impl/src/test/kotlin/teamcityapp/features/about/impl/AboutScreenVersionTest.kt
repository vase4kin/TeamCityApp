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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

    @Test fun defaultsToTheCurrentApplicationVersion() {
        compose.setContent {
            TeamCityTheme { AboutScreen(content, {}, {}, {}) }
        }
        compose.onNodeWithText(BuildConfig.VERSION).assertIsDisplayed()
    }

    @Test fun rendersAnExplicitVersionForDeterministicFixtures() {
        compose.setContent {
            TeamCityTheme { AboutScreen(content, {}, {}, {}, appVersion = "9.87.65") }
        }
        compose.onNodeWithText("9.87.65").assertIsDisplayed()
        compose.onNodeWithText(BuildConfig.VERSION).assertDoesNotExist()
    }
}
