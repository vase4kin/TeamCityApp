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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
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
class LoadingButtonTest {
    @get:Rule val compose = createComposeRule()

    @Test fun floatingActionKeepsWidthAndBlocksRepeatedClicksWhileLoading() {
        val loading = mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            TeamCityTheme {
                TeamCityLoadingExtendedFloatingActionButton({
                    clicks++
                    loading.value = true
                }, "Restart", "Restarting…", loading.value, Modifier.testTag("action"), icon = {})
            }
        }
        val width = compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot.width
        compose.onNodeWithTag("action").performClick()
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onNodeWithTag("action").assertIsNotEnabled()
        compose.onNodeWithText("Restarting…", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag("action").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Restarting…"))
        assertEquals(width, compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot.width, .5f)
        compose.onNodeWithTag("action").performClick()
        assertEquals(1, clicks)
    }

    @Test fun regularButtonShowsLabelAndIndicatorAndRejectsBusyTaps() {
        var clicks = 0
        compose.setContent { TeamCityTheme { TeamCityLoadingButton({ clicks++ }, "Sign in", "Signing in…", true, Modifier.testTag("action")) } }
        compose.onNodeWithTag("action").assertIsNotEnabled().performClick()
        compose.onNodeWithText("Signing in…").assertIsDisplayed()
        compose.onNode(hasProgressBarRangeInfo(androidx.compose.ui.semantics.ProgressBarRangeInfo.Indeterminate)).assertExists()
        assertEquals(0, clicks)
    }
}
