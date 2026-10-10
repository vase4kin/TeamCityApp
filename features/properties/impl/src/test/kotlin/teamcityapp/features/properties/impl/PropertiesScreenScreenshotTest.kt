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

package teamcityapp.features.properties.impl

import android.app.Application
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
import teamcityapp.features.properties.api.Property
import teamcityapp.libraries.theme.TeamCityTheme

/** Deterministic goldens for every parameter-list state and supported layout. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PropertiesScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
            "empty" -> PropertiesUiState.Empty

            "content" -> PropertiesUiState.Content(listOf(Property("sdk", "24"), Property("userName", "Murdock")))

            "empty_value" -> PropertiesUiState.Content(listOf(Property("env.CI", "")))

            "long_value", "expanded_value" -> PropertiesUiState.Content(
                listOf(
                    Property("build.number", "#1842"),
                    Property("teamcity.build.branch", "feature/simpler-properties"),
                    Property("env.JAVA_HOME", "/opt/java/openjdk-17"),
                    Property("env.GRADLE_OPTS", "-Xmx4g -XX:+UseParallelGC -Dfile.encoding=UTF-8\n-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true\n-Dorg.gradle.caching=true -Dorg.gradle.workers.max=4"),
                    Property("env.HTTP_PROXY", "")
                )
            )

            "long_content" -> PropertiesUiState.Content(
                (0..30).map {
                    Property("env.very.long.parameter.name.$it.with.more.text", "A long parameter value that wraps to a second line and is truncated when it exceeds the available row width")
                }
            )

            else -> error("Unknown screenshot state: $stateName")
        }
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { PropertiesScreen(state, {}) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val context = RuntimeEnvironment.getApplication()
        if (state == PropertiesUiState.Empty) {
            compose.onNodeWithText(context.getString(R.string.empty_list_message_parameters)).assertIsDisplayed()
        } else {
            compose.onNodeWithTag("properties:list").assertIsDisplayed()
        }
        if (stateName == "expanded_value") {
            compose.onNodeWithTag("properties:expand:3").performScrollTo().performClick()
            compose.waitForIdle()
        }
        val name = "properties_${stateName}_${variant.name}"
        compose.onRoot().captureRoboImage("$name.png")
        if (stateName == "long_content") {
            compose.onNodeWithTag("properties:list").performScrollToNode(hasTestTag("properties:row:30"))
            compose.onNodeWithTag("properties:row:30").assertIsDisplayed()
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
            listOf("empty", "content", "empty_value", "long_content", "long_value", "expanded_value").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
