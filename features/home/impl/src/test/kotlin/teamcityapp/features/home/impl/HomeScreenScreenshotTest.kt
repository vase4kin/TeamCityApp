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

package teamcityapp.features.home.impl

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import teamcityapp.features.home.api.*
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val badges = HomeTab.entries.filter { it != HomeTab.Projects }.associateWith<HomeTab, HomeBadge> { HomeBadge.Count(if (it == HomeTab.Favorites) 8 else 123) }
        val state = when {
            stateName == "loading" -> HomeUiState()
            stateName == "counts_failed" -> HomeUiState(badges = badges.mapValues { HomeBadge.Failed() })
            stateName == "partial_failure" -> HomeUiState(badges = badges + (HomeTab.Queue to HomeBadge.Failed()))
            stateName == "onboarding_failed" -> HomeUiState(badges = badges, onboardingFailed = true)
            stateName.startsWith("prompt_") -> HomeUiState(badges = badges, prompt = HomePrompt.valueOf(stateName.removePrefix("prompt_")).ui())
            else -> HomeUiState(selected = HomeTab.valueOf(stateName.removePrefix("tab_")), badges = badges)
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                HomeScreen(state, variant.width >= 600, {}, {}, {}, {}, remember { SnackbarHostState() }) {
                    Surface(Modifier.fillMaxSize()) {
                        Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
                            Text("TeamCity", style = MaterialTheme.typography.headlineSmall)
                            Text("Projects, configurations and builds", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("home_${stateName}_${variant.name}.png")
    }
    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(Variant("phone_$theme", 360, 800, dark, 1f), Variant("tablet_$theme", 1000, 700, dark, 1f), Variant("large_font_$theme", 360, 800, dark, 1.5f))
            (listOf("loading", "counts_failed", "partial_failure", "onboarding_failed") + HomeTab.entries.map { "tab_${it.name}" } + HomePrompt.entries.map { "prompt_${it.name}" }).flatMap { state -> variants.map { arrayOf<Any>(state, it) } }
        }
    }
}
