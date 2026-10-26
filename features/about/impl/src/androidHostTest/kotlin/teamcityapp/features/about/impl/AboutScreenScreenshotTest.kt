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
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
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

/** Every About state, including the visible optional server failure. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AboutScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
            "loading" -> AboutUiState.Loading

            "available" -> AboutUiState.Content(
                ServerDetailsUiState.Available(ServerDetailsUiModel("2026.1", "https://teamcity.example"))
            )

            "unavailable" -> AboutUiState.Content(ServerDetailsUiState.Unavailable)

            else -> error("Unknown screenshot state: $stateName")
        }
        // Pin the progress indicator's animation frame rather than sampling wall time.
        compose.mainClock.autoAdvance = state != AboutUiState.Loading
        // Keep release version bumps independent of the visual fixture.
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) { AboutScreen(state, {}, {}, {}, appVersion = "1.52.8") }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val context = RuntimeEnvironment.getApplication()
        if (state == AboutUiState.Loading) {
            compose.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        } else {
            compose.onNodeWithText(context.getString(R.string.about_app_text_app)).assertExists()
            compose.onNodeWithText("1.52.8").assertIsDisplayed()
            if (stateName == "available") {
                compose.onNodeWithText("https://teamcity.example").assertIsDisplayed()
            } else {
                compose.onNodeWithTag("about:server-unavailable").assertExists()
            }
        }
        val name = "about_${stateName}_${variant.name}"
        compose.onRoot().captureRoboImage("$name.png")
        if (state != AboutUiState.Loading && variant.width < 600) {
            // A top-of-screen golden alone would miss the lower actions in the lazy grid.
            val privacy = context.getString(teamcityapp.libraries.resources.R.string.about_app_text_privacy)
            compose.onNode(hasScrollToIndexAction()).performTouchInput { swipeUp() }
            compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(privacy))
            compose.onNodeWithText(privacy).assertIsDisplayed()
            compose.onRoot().captureRoboImage("${name}_bottom.png")
        }
        if (stateName == "available" && variant.name == "phone_light") {
            val back = context.getString(teamcityapp.libraries.theme.R.string.action_back)
            compose.onNodeWithContentDescription(back).performTouchInput { longClick() }
            compose.waitForIdle()
            compose.onNodeWithText(back).assertIsDisplayed()
            // Tooltips use a popup window, so capture all windows rather than only the screen root.
            captureScreenRoboImage("about_back_tooltip.png")
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
            listOf("loading", "available", "unavailable").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
