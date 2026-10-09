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

package teamcityapp.features.test_details.impl

import android.app.Application
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.theme.TeamCityTheme

/** Every Test Details state, in both themes, sizes, and enlarged text. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TestDetailsScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
    data class Variant(val name: String, val width: Int, val height: Int, val dark: Boolean, val fontScale: Float) {
        override fun toString() = name
    }

    private val compose = createComposeRule()
    private val device = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val qualifiers = RuntimeEnvironment.getQualifiers()
                val fontScale = RuntimeEnvironment.getFontScale()
                RuntimeEnvironment.setQualifiers(
                    "en-rUS-w${variant.width}dp-h${variant.height}dp-${if (variant.dark) "night" else "notnight"}-mdpi"
                )
                RuntimeEnvironment.setFontScale(variant.fontScale)
                try {
                    base.evaluate()
                } finally {
                    RuntimeEnvironment.setQualifiers(qualifiers)
                    RuntimeEnvironment.setFontScale(fontScale)
                }
            }
        }
    }

    @get:Rule val rules: RuleChain = RuleChain.outerRule(device).around(compose)

    @Test fun rendersState() {
        val state = when (stateName) {
            "loading" -> TestDetailsUiState.Loading
            "empty" -> TestDetailsUiState.Empty
            "error" -> TestDetailsUiState.Error
            "invalid_input" -> TestDetailsUiState.InvalidInput
            "content" -> TestDetailsUiState.Content("Test details <tag> & literal text")
            "long_content" -> TestDetailsUiState.Content((0..80).joinToString(" ") { "Stack frame $it: AssertionError at example.Test.test(Test.kt:$it)." })
            else -> error("Unknown screenshot state: $stateName")
        }
        compose.mainClock.autoAdvance = state != TestDetailsUiState.Loading
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { TestDetailsScreen(state, {}, {}) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        when (state) {
            TestDetailsUiState.Loading -> compose.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
            TestDetailsUiState.Empty -> compose.onNodeWithText("No test details").assertIsDisplayed()
            TestDetailsUiState.Error -> compose.onNodeWithText("Try again").assertIsDisplayed()
            is TestDetailsUiState.Content -> compose.onNodeWithTag("test_details:text").assertIsDisplayed()
            TestDetailsUiState.InvalidInput -> compose.onNodeWithTag("test_details:text").assertDoesNotExist()
        }
        val name = "test_details_${stateName}_${variant.name}"
        compose.onRoot().captureRoboImage("$name.png")
        if (stateName == "long_content") {
            compose.onNodeWithTag("test_details:text").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 100000f) }
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("${name}_bottom.png")
        }
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(
                Variant("phone_$theme", 360, 800, dark, 1f),
                Variant("tablet_$theme", 1000, 700, dark, 1f),
                Variant("large_font_$theme", 360, 800, dark, 1.5f)
            )
            listOf("loading", "empty", "error", "invalid_input", "content", "long_content").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
