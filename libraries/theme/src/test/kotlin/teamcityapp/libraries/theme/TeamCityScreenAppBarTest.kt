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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TeamCityScreenAppBarTest(private val dark: Boolean) {
    @get:Rule val compose = createComposeRule()
    private var flat = Color.Unspecified
    private var raised = Color.Unspecified

    @Test fun programmaticScrollingAndShorterContentUpdateThePinnedHeader() {
        val count = mutableStateOf(40)
        compose.setContent {
            TeamCityTheme(darkTheme = dark) {
                flat = MaterialTheme.colorScheme.surface
                raised = MaterialTheme.colorScheme.surfaceContainer
                TeamCityScreen("Screen", {}) { modifier ->
                    val listState = rememberTeamCityLazyListState()
                    LazyColumn(modifier.testTag("list"), state = listState) {
                        items(count.value) { Text("Row $it", Modifier.fillMaxWidth().padding(24.dp)) }
                    }
                }
            }
        }
        val bounds = compose.onNodeWithTag("screen:app-bar").fetchSemanticsNode().boundsInRoot
        assertBar(flat)
        compose.onNodeWithTag("list").performScrollToIndex(20)
        assertBar(raised)
        compose.onNodeWithTag("list").performScrollToIndex(19)
        assertBar(raised)
        compose.onNodeWithTag("list").performScrollToIndex(0)
        assertBar(flat)
        compose.onNodeWithTag("list").performScrollToIndex(20)
        assertBar(raised)
        compose.runOnIdle { count.value = 1 }
        assertBar(flat)
        compose.onNodeWithTag("list").performTouchInput { swipeUp() }
        assertBar(flat)
        assertEquals(bounds, compose.onNodeWithTag("screen:app-bar").fetchSemanticsNode().boundsInRoot)
    }

    @Test fun retainedPanesKeepIndependentListAndHeaderPositions() {
        val selected = mutableStateOf("first")
        compose.setContent {
            TeamCityTheme(darkTheme = dark) {
                flat = MaterialTheme.colorScheme.surface
                raised = MaterialTheme.colorScheme.surfaceContainer
                val holder = rememberSaveableStateHolder()
                holder.SaveableStateProvider(selected.value) {
                    TeamCityScreen(selected.value, {}) { modifier ->
                        val listState = rememberTeamCityLazyListState()
                        LazyColumn(modifier.testTag("list"), state = listState) {
                            items(40) { Text("Row $it", Modifier.fillMaxWidth().padding(24.dp)) }
                        }
                    }
                }
            }
        }
        assertBar(flat)
        compose.onNodeWithTag("list").performScrollToIndex(20)
        assertBar(raised)
        compose.runOnIdle { selected.value = "second" }
        assertBar(flat)
        compose.runOnIdle { selected.value = "first" }
        assertBar(raised)
        compose.onNodeWithTag("list").performScrollToIndex(0)
        assertBar(flat)
    }

    private fun assertBar(expected: Color) {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        val bar = compose.onNodeWithTag("screen:app-bar").assertIsDisplayed()
        val pixels = bar.captureToImage().toPixelMap()
        assertEquals(expected, pixels[pixels.width - 4, pixels.height / 2])
    }

    companion object {
        @JvmStatic
        @Parameters(name = "dark={0}")
        fun cases(): List<Array<Any>> = listOf(false, true).map { arrayOf(it) }
    }
}
