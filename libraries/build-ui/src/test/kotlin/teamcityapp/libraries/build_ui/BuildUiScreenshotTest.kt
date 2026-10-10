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

package teamcityapp.libraries.build_ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildUiScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val base = BuildLaunchData("1", "/builds/1", number = "42", state = "finished", status = "SUCCESS", statusText = "Success", branchName = "main", buildTypeId = "Android_Debug", configuration = BuildConfigurationData("Android_Debug", "Android Debug", "Mobile", "Mobile"))
        val row = when (stateName) {
            "failure" -> base.copy(status = "FAILURE", statusText = "Tests failed")
            "error" -> base.copy(status = "ERROR", statusText = "Failed to start")
            "unknown" -> base.copy(status = "UNKNOWN", statusText = "Unknown status")
            "running" -> base.copy(state = "running", statusText = "Building and running tests")
            "queued" -> base.copy(state = "queued", waitReason = "Waiting for an agent")
            "queued_fallback" -> base.copy(state = "queued", number = null)
            "flags" -> base.copy(personal = true, pinned = true)
            "partial_configuration" -> base.copy(configuration = null)
            "long_labels" -> base.copy(number = "123456789012345678901234567890", statusText = "Building the production mobile application with snapshot dependencies while waiting for compatible agents and deployment environments", branchName = "feature/preserve-the-complete-build-launch-payload-and-configuration-navigation", personal = true, pinned = true, configuration = BuildConfigurationData("long", "Build Android and iOS applications with every deployment environment", "Mobile", "Mobile application development"))
            else -> base
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(Modifier.widthIn(max = 720.dp).fillMaxSize()) {
                        item { TeamCityBuildConfigurationHeader(row, { _, _ -> }) }
                        item { TeamCityBuildRow(row, {}, Modifier.testTag("row")) }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build_ui_${stateName}_${variant.name}.png")
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
            listOf("success", "failure", "error", "unknown", "running", "queued", "queued_fallback", "flags", "partial_configuration", "long_labels").flatMap { state -> variants.map { arrayOf<Any>(state, it) } }
        }
    }
}
