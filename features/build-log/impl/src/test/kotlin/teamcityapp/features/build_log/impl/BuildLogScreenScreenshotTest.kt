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

package teamcityapp.features.build_log.impl

import android.app.Application
import androidx.compose.foundation.layout.padding
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
import teamcityapp.features.build_log.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildLogScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
            "loading" -> BuildLogUiState.Loading

            "error" -> BuildLogUiState.Error

            else -> BuildLogUiState.Session(
                BuildLogSession("https://teamcity.example/viewLog.html?buildId=123", sslDisabled = stateName == "ssl", needsAuthentication = stateName in listOf("authentication", "acknowledging", "authentication_error")),
                page = when (stateName) {
                    "content" -> BuildLogPage.Content
                    "page_error" -> BuildLogPage.Error
                    else -> BuildLogPage.Loading
                },
                acknowledging = stateName == "acknowledging",
                authenticationFailed = stateName == "authentication_error"
            )
        }
        compose.mainClock.autoAdvance = false
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { androidx.compose.material3.Surface { BuildLogScreen(state, {}, {}, {}, webContent = { _, modifier -> androidx.compose.material3.Text("Build started\nCheckout sources\nCompilation successful\nBuild finished", modifier.padding(16.dp)) }) } } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val tag: String? = null
        val node = if (tag == null) compose.onRoot() else compose.onNodeWithTag(tag)
        node.captureRoboImage("build_log_${stateName}_${variant.name}.png")
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
            listOf("loading", "error", "ssl", "authentication", "acknowledging", "authentication_error", "page_loading", "page_error", "content").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
