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

package teamcityapp.libraries.theme

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ErrorContentTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fullScreenErrorUsesSharedCopyAndDispatchesRetry() {
        var retries = 0
        compose.setContent { TeamCityTheme { ErrorContent(Modifier.fillMaxSize(), { retries++ }) } }
        compose.onNodeWithText("Couldn’t load content").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun authenticationRecoveryRemainsReachableInShortWindowAtLargeText() {
        var signIns = 0
        compose.setContent {
            TeamCityTheme {
                CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                    ErrorContent(
                        Modifier.width(300.dp).height(280.dp),
                        { signIns++ },
                        title = "Sign in to view the build log",
                        message = "Your TeamCity session needs authentication before this build log can be displayed.",
                        actionLabel = "Sign in"
                    )
                }
            }
        }
        compose.onNodeWithText("Try again").assertDoesNotExist()
        compose.onNodeWithText("Sign in").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, signIns)
    }

    @Test fun inlineRecoveryWrapsAndRespectsPendingAction() {
        var retries = 0
        val enabled = mutableStateOf(false)
        compose.setContent {
            TeamCityTheme {
                CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                    Surface(Modifier.width(300.dp)) {
                        ErrorNotice("Couldn’t refresh. Previously loaded data is still available.", { retries++ }, enabled = enabled.value)
                    }
                }
            }
        }
        compose.onNodeWithText("Couldn’t refresh. Previously loaded data is still available.").assertIsDisplayed()
        compose.onNodeWithText("Try again").assertIsDisplayed().assertIsNotEnabled()
        compose.runOnIdle { enabled.value = true }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun noticeWithoutRecoveryDoesNotInventAnAction() {
        compose.setContent { TeamCityTheme { ErrorNotice("This optional section is unavailable") } }
        compose.onNodeWithText("This optional section is unavailable").assertIsDisplayed()
        compose.onNodeWithText("Try again").assertDoesNotExist()
    }
}
