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
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildDetailsScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val snapshot = BuildLaunchData("42", "/builds/42", number = "42", state = "finished", webUrl = "https://server/build/42", buildTypeId = "android", configuration = BuildConfigurationData("android", "Build Android application", "project", "Android"), changes = BuildCollectionLink("/changes"), tests = BuildTests("/tests", 100, 20, 3, 123), artifacts = BuildCollectionLink("/artifacts"), snapshotDependencies = BuildCollectionLink(null))
        val counts = mapOf(BuildDetailsTab.Changes to 12, BuildDetailsTab.Tests to 123)
        val state = when {
            stateName.startsWith("prompt_") -> {
                val kind = BuildDetailsPrompt.valueOf(stateName.removePrefix("prompt_"))
                val description = when (kind) {
                    BuildDetailsPrompt.Stop -> R.string.text_onboarding_stop_build
                    BuildDetailsPrompt.Remove -> R.string.text_onboarding_remove_build_from_queue
                    BuildDetailsPrompt.Restart -> R.string.text_onboarding_restart_build
                }
                BuildDetailsUiState(snapshot, counts = counts, prompt = BuildDetailsPromptUi(kind, description = description))
            }

            stateName == "counts_loading" -> BuildDetailsUiState(snapshot)

            stateName == "counts_partial_failed" -> BuildDetailsUiState(snapshot, counts = mapOf(BuildDetailsTab.Changes to 12), countFailures = setOf(BuildDetailsTab.Tests))

            stateName == "counts_all_failed" -> BuildDetailsUiState(snapshot, countFailures = setOf(BuildDetailsTab.Changes, BuildDetailsTab.Tests))

            stateName == "tips_failed" -> BuildDetailsUiState(snapshot, counts = counts, onboardingFailed = true)

            stateName.startsWith("tab_") -> BuildDetailsUiState(snapshot, selected = BuildDetailsTab.valueOf(stateName.removePrefix("tab_")), counts = counts)

            stateName.startsWith("dialog_") -> {
                val kind = when {
                    stateName.contains("Stop") -> ConfirmationKind.Stop
                    stateName.contains("Remove") -> ConfirmationKind.Remove
                    else -> ConfirmationKind.Restart
                }
                BuildDetailsUiState(snapshot, counts = counts, confirmation = BuildConfirmation(snapshot, kind, own = !stateName.endsWith("other")))
            }

            stateName.startsWith("progress_") -> BuildDetailsUiState(snapshot, counts = counts, operation = BuildDetailsOperation.valueOf(stateName.removePrefix("progress_")))

            else -> BuildDetailsUiState(snapshot, counts = counts)
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                BuildDetailsScreen(state, variant.width >= 600, {}, {}, {}, {}, {}, remember { SnackbarHostState() }) {
                    Surface(Modifier.fillMaxSize()) {
                        Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
                            Text("Build #42", style = MaterialTheme.typography.headlineSmall)
                            Text("${state.selected.name} content", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("details_${stateName}_${variant.name}.png")
    }
    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(Variant("phone_$theme", 360, 800, dark, 1f), Variant("tablet_$theme", 1000, 700, dark, 1f), Variant("large_font_$theme", 360, 800, dark, 1.5f))
            (BuildDetailsTab.entries.map { "tab_${it.name}" } + listOf("dialog_Stop_own", "dialog_Stop_other", "dialog_Remove_own", "dialog_Remove_other", "dialog_Restart") + BuildDetailsOperation.entries.map { "progress_${it.name}" } + BuildDetailsPrompt.entries.map { "prompt_${it.name}" } + listOf("counts_loading", "counts_partial_failed", "counts_all_failed", "tips_failed")).flatMap { state -> variants.map { arrayOf<Any>(state, it) } }
        }
    }
}
