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

package teamcityapp.features.build_details.impl

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
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
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.rememberTeamCityLazyListState

/** Reproduces a feature's own content scaffold inside the parent header. */
@Composable
internal fun AppBarScrollableTestContent() {
    val listState = rememberTeamCityLazyListState()
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding).testTag("details:scroll")) {
            items(40) { index -> Text("Build content row ${index + 1}", Modifier.fillMaxWidth().padding(24.dp)) }
        }
    }
}

@OptIn(ExperimentalTestApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildDetailsAppBarTest(private val section: BuildDetailsSection?, private val dark: Boolean) {
    @get:Rule val compose = createComposeRule()

    @Test fun nestedContentLiftsPinnedHeaderAndRestoresItsAppearance() {
        val restoration = StateRestorationTester(compose)
        var flat = Color.Unspecified
        var raised = Color.Unspecified
        restoration.setContent {
            TeamCityTheme(darkTheme = dark) {
                flat = MaterialTheme.colorScheme.surface
                raised = MaterialTheme.colorScheme.surfaceContainer
                if (section == null) {
                    BuildDetailsScreen(BuildDetailsUiState(BuildLaunchData("42", "/builds/42", number = "42")), {}, {}, {}, {}, remember { SnackbarHostState() }) { AppBarScrollableTestContent() }
                } else {
                    BuildDetailsSectionScreen(section, "42", "Build Android", {}) { AppBarScrollableTestContent() }
                }
            }
        }
        val bar = compose.onNodeWithTag("screen:app-bar")
        val bounds = bar.fetchSemanticsNode().boundsInRoot
        fun assertBar(expected: Color) {
            compose.mainClock.advanceTimeBy(1_000)
            compose.waitForIdle()
            bar.assertIsDisplayed()
            assertEquals(bounds, bar.fetchSemanticsNode().boundsInRoot)
            val pixels = bar.captureToImage().toPixelMap()
            assertEquals(expected, pixels[pixels.width - 4, pixels.height / 2])
        }
        assertBar(flat)
        val list = compose.onNodeWithTag("details:scroll")
        list.performTouchInput { swipeUp(startY = height * .65f, endY = height * .35f, durationMillis = 1_000) }
        assertBar(raised)
        restoration.emulateSavedInstanceStateRestore()
        assertBar(raised)
        repeat(3) { list.performTouchInput { swipeDown(startY = height * .35f, endY = height * .65f, durationMillis = 1_000) } }
        assertBar(flat)
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}_dark={1}")
        fun cases(): List<Array<Any?>> = (listOf(null) + listOf(BuildDetailsSection.Changes, BuildDetailsSection.Tests, BuildDetailsSection.Parameters, BuildDetailsSection.Artifacts, BuildDetailsSection.Snapshots)).flatMap { section -> listOf(false, true).map { arrayOf(section, it) } }
    }
}
