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

package teamcityapp.features.splash.impl

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.splash.api.SplashDestination
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SplashScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loadingPreservesCentered160DpAccessibleLogo() {
        compose.setContent { TeamCityTheme { SplashScreen(SplashUiState.Loading) } }
        compose.onNodeWithTag("splash:logo").assertWidthIsEqualTo(160.dp).assertHeightIsEqualTo(160.dp).assertIsDisplayed().assertContentDescriptionEquals("TeamCityApp")
        val root = compose.onNodeWithTag("splash:screen").fetchSemanticsNode().boundsInRoot
        val logo = compose.onNodeWithTag("splash:logo").fetchSemanticsNode().boundsInRoot
        assertEquals(root.center.x, logo.center.x, 0.1f)
        val group = compose.onNodeWithTag("splash:loading").fetchSemanticsNode().boundsInRoot
        assertEquals(root.center.y, group.center.y, 0.1f)
        compose.onNode(hasProgressBarRangeInfo(androidx.compose.ui.semantics.ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        compose.onNodeWithText("Try again").assertDoesNotExist()
    }

    @Test fun errorOffersAccessibleRetryAction() {
        var retries = 0
        compose.setContent { TeamCityTheme { SplashScreen(SplashUiState.Error, onRetry = { retries++ }) } }
        compose.onNodeWithTag("splash:error").assertIsDisplayed()
        compose.onNodeWithText("Try again").assertHasClickAction().performClick()
        assertEquals(1, retries)
    }

    @Test fun readyKeepsBrandingUntilUiPerformsNavigation() {
        compose.setContent { TeamCityTheme { SplashScreen(SplashUiState.Ready(SplashDestination.Home)) } }
        compose.onNodeWithTag("splash:ready:Home").assertExists()
        compose.onNodeWithContentDescription("TeamCityApp").assertIsDisplayed()
    }

    @Test fun navigatedKeepsBrandingWhileActivityFinishes() {
        compose.setContent { TeamCityTheme { SplashScreen(SplashUiState.Navigated) } }
        compose.onNodeWithTag("splash:navigated").assertExists()
        compose.onNodeWithContentDescription("TeamCityApp").assertIsDisplayed()
    }

    @Test fun errorUsesProvidedSurfaceAndTypography() {
        compose.setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(surface = Color.Green, onSurface = Color.Red),
                typography = Typography(bodyLarge = TextStyle(fontSize = 22.sp), bodyMedium = TextStyle(fontSize = 18.sp), headlineSmall = TextStyle(fontSize = 28.sp))
            ) {
                SplashScreen(SplashUiState.Error)
            }
        }
        assertThemeText("Couldn’t load content", 28, Color.Red)
        val pixels = compose.onNodeWithTag("splash:screen").captureToImage().toPixelMap()
        assertEquals(Color.Green, pixels[0, 0])
    }

    private fun assertThemeText(text: String, fontSize: Int, color: Color) {
        val results = mutableListOf<TextLayoutResult>()
        compose.onAllNodesWithText(text, useUnmergedTree = true)[0]
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(fontSize.sp, results.single().layoutInput.style.fontSize)
        assertEquals(color, results.single().layoutInput.style.color)
    }
}
