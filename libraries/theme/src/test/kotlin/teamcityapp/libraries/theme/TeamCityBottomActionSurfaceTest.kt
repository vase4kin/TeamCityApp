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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class, ExperimentalLayoutApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TeamCityBottomActionSurfaceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun scrollRemainderChangesToneAtBottomAndBackWithoutMovingActions() = scrollTransitions(false)

    @Test fun scrollRemainderChangesToneInDarkTheme() = scrollTransitions(true)

    private fun scrollTransitions(dark: Boolean) {
        lateinit var scroll: ScrollState
        val contentHeight = mutableStateOf(80)
        val viewportHeight = mutableStateOf(240)
        var actions = 0
        compose.setContent {
            TeamCityTheme(dark) {
                scroll = rememberScrollState()
                Column(Modifier.width(320.dp)) {
                    Column(Modifier.height(viewportHeight.value.dp).fillMaxWidth().verticalScroll(scroll)) {
                        Spacer(Modifier.height(contentHeight.value.dp))
                    }
                    TeamCityBottomActionSurface(scroll.canScrollForward, Modifier.testTag("bar")) {
                        Button({ actions++ }, Modifier.fillMaxWidth().padding(16.dp).height(56.dp).testTag("action")) { Text("Action") }
                    }
                }
            }
        }
        assertTone(dark, false)
        val initial = compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { contentHeight.value = 500 }
        assertTone(dark, true)
        assertEquals(initial, compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot)
        compose.runOnIdle { runBlocking { scroll.scrollTo(scroll.maxValue) } }
        assertTone(dark, false)
        compose.runOnIdle { runBlocking { scroll.scrollTo(scroll.maxValue - 20) } }
        assertTone(dark, true)
        // Shrinking content while scrolled clamps the offset and removes separation.
        compose.runOnIdle { contentHeight.value = 64 }
        assertTone(dark, false)
        compose.runOnIdle { contentHeight.value = 500 }
        assertTone(dark, true)
        compose.runOnIdle { viewportHeight.value = 600 }
        assertTone(dark, false)
        compose.runOnIdle { viewportHeight.value = 120 }
        assertTone(dark, true)
        compose.onNodeWithTag("action").performClick()
        assertEquals(1, actions)
    }

    @Test fun emptyAndLoadingSizedContentAreFlatUntilTheyOverflow() {
        val height = mutableStateOf(0)
        compose.setContent {
            TeamCityTheme {
                val scroll = rememberScrollState()
                Column {
                    Column(Modifier.height(120.dp).verticalScroll(scroll)) { Spacer(Modifier.height(height.value.dp)) }
                    TeamCityBottomActionSurface(scroll.canScrollForward, Modifier.height(80.dp).testTag("bar")) { }
                }
            }
        }
        assertTone(false, false)
        compose.runOnIdle { height.value = 64 }
        assertTone(false, false)
        compose.runOnIdle { height.value = 300 }
        assertTone(false, true)
    }

    @Test fun lazyListProducerUsesTheSameRemainingContentContract() {
        lateinit var list: androidx.compose.foundation.lazy.LazyListState
        val count = mutableStateOf(1)
        compose.setContent {
            TeamCityTheme {
                list = rememberLazyListState()
                Column {
                    LazyColumn(Modifier.height(120.dp), state = list) { items(count.value) { Text("Item $it", Modifier.height(48.dp)) } }
                    TeamCityBottomActionSurface(list.canScrollForward, Modifier.height(80.dp).testTag("bar")) { }
                }
            }
        }
        assertTone(false, false)
        compose.runOnIdle { count.value = 10 }
        assertTone(false, true)
        compose.runOnIdle { runBlocking { list.scrollToItem(9) } }
        assertTone(false, false)
        compose.runOnIdle { runBlocking { list.scrollToItem(0) } }
        assertTone(false, true)
    }

    @Test fun navigationInsetsShareToneAndKeepActionsSafeInLightTheme() = navigationInsets(false)

    @Test fun navigationInsetsShareToneAndKeepActionsSafeInDarkTheme() = navigationInsets(true)

    private fun navigationInsets(dark: Boolean) {
        val raised = mutableStateOf(false)
        val ime = MutableWindowInsets(WindowInsets(0))
        compose.setContent {
            TeamCityTheme(dark) {
                Column(Modifier.width(320.dp).height(360.dp).windowInsetsPadding(ime)) {
                    Spacer(Modifier.weight(1f).fillMaxWidth().testTag("viewport"))
                    TeamCityBottomActionSurface(
                        raised.value,
                        Modifier.testTag("bar"),
                        windowInsets = WindowInsets(left = 20, right = 30, bottom = 24),
                        contentMaxWidth = 240.dp
                    ) {
                        Box(Modifier.height(80.dp).fillMaxWidth().testTag("action"))
                    }
                }
            }
        }
        assertTone(dark, false)
        val initial = compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot
        assertEquals(35f, initial.left, 0f)
        assertEquals(240f, initial.width, 0f)
        assertEquals(336f, initial.bottom, 0f)
        assertEquals(104f, compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot.height, 0f)
        assertEquals(initial.top, compose.onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot.bottom, 0f)
        compose.runOnIdle { raised.value = true }
        assertTone(dark, true)
        assertEquals(initial, compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot)
        val pixels = compose.onNodeWithTag("bar").captureToImage().toPixelMap()
        val expected = (if (dark) DarkColorScheme else LightColorScheme).surfaceContainer
        assertEquals(expected, pixels[8, pixels.height - 8])
        assertEquals(expected, pixels[pixels.width - 8, 8])

        // A docked IME consumes the navigation inset: no extra nav gap or IME-sized surface.
        compose.runOnIdle { ime.insets = WindowInsets(bottom = 120) }
        compose.waitForIdle()
        val keyboardAction = compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot
        assertEquals(240f, keyboardAction.bottom, 0f)
        assertEquals(80f, compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot.height, 0f)
        assertEquals(keyboardAction.top, compose.onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot.bottom, 0f)
        compose.runOnIdle {
            ime.insets = WindowInsets(0)
            raised.value = false
        }
        assertTone(dark, false)
        assertEquals(initial, compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot)
    }

    private fun assertTone(dark: Boolean, raised: Boolean) {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        val colors = if (dark) DarkColorScheme else LightColorScheme
        val expected = if (raised) colors.surfaceContainer else colors.surface
        val pixels = compose.onNodeWithTag("bar").captureToImage().toPixelMap()
        assertEquals(expected, pixels[8, 8])
    }
}
