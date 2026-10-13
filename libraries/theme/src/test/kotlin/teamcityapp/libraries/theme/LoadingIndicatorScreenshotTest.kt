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
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalTestApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoadingIndicatorScreenshotTest(private val stateName: String, private val variant: Variant) {
    data class Variant(val name: String, val width: Int, val height: Int, val dark: Boolean, val fontScale: Float) {
        override fun toString() = name
    }
    private val compose = createComposeRule()
    private val device = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val qualifiers = RuntimeEnvironment.getQualifiers()
                val fontScale = RuntimeEnvironment.getFontScale()
                RuntimeEnvironment.setQualifiers("en-rUS-w${variant.width}dp-h${variant.height}dp-${if (variant.dark) "night" else "notnight"}-mdpi")
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
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                Surface(Modifier.fillMaxSize()) {
                    when (stateName) {
                        "button" -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            TeamCityLoadingButton({}, "Sign in", "Signing in…", false)
                            TeamCityLoadingButton({}, "Sign in", "Signing in…", true)
                            TeamCityLoadingExtendedFloatingActionButton({}, "Restart", "Restarting…", true, icon = {})
                        }

                        "content" -> LoadingContent(Modifier.fillMaxSize())

                        "linear" -> Column(Modifier.padding(16.dp)) {
                            Text("Saving changes")
                            TeamCityLinearLoadingIndicator(Modifier.padding(top = 16.dp))
                        }

                        "branch" -> BranchField(null, false, "", {}, "Build branch", "Loading branches…", "No branches", "Default branch")

                        else -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            TeamCityLoadingIndicator(Modifier.size(TeamCityDimensions.compactProgressIndicatorSize))
                            TeamCityLoadingIndicator(Modifier.size(TeamCityDimensions.progressIndicatorSize))
                            TeamCityLoadingIndicator()
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).onFirst().assertIsDisplayed()
        compose.onRoot().captureRoboImage("loading_${stateName}_${variant.name}.png")
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
            listOf("content", "inline", "linear", "branch", "button").flatMap { state -> variants.map { arrayOf<Any>(state, it) } }
        }
    }
}
