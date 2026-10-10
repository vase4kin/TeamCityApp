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

package teamcityapp.features.run_build.impl

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
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RunBuildScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val request = BuildRequest("bt1", branch = "main", agent = if (stateName == "content") BuildAgent("1", "Linux agent") else null, personal = stateName == "content", queueAtTop = stateName == "content", parameters = if (stateName in listOf("parameters", "expanded", "scrolled")) listOf(BuildParameter("env.name", "staging"), BuildParameter("system.verbose", "true")) else emptyList())
        val state = RunBuildUiState(
            branches = when (stateName) {
                "loading" -> null
                "empty", "branches_error" -> emptyList()
                "single_branch" -> listOf("main")
                else -> listOf("main", "develop")
            },
            branchesFailed = stateName == "branches_error",
            agents = when (stateName) {
                "loading" -> null
                "empty", "agents_error" -> emptyList()
                else -> listOf(BuildAgent("1", "Linux agent"), BuildAgent("2", "Windows agent"))
            },
            agentsFailed = stateName == "agents_error",
            request = request,
            queuing = stateName == "queuing",
            queueError = when (stateName) {
                "queue_error" -> QueueBuildResult.Error
                "forbidden" -> QueueBuildResult.Forbidden
                else -> null
            }
        )
        compose.mainClock.autoAdvance = false
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { RunBuildScreen(state, {}, {}, {}, {}, {}, {}, agentDialog = stateName == "agent_dialog", parameterDialog = if (stateName.startsWith("parameter_")) ParameterDialogState(invalid = stateName == "parameter_error") else null) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (stateName in listOf("expanded", "scrolled")) {
            compose.onNodeWithTag("run-build:options").performClick()
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
        }
        if (stateName == "scrolled") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("run-build:scroll").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10_000f) }
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
        }
        compose.onNodeWithContentDescription(RuntimeEnvironment.getApplication().getString(teamcityapp.libraries.theme.R.string.action_close)).assertIsDisplayed()
        val tag = when (stateName) {
            "queuing" -> "run-build:progress"
            "agent_dialog" -> "run-build:agent-dialog"
            "parameter_dialog", "parameter_error" -> "run-build:parameter-dialog"
            else -> null
        }
        if (tag == null) compose.onRoot().captureRoboImage("run_build_${stateName}_${variant.name}.png") else captureScreenRoboImage("run_build_${stateName}_${variant.name}.png")
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(
                Variant("phone_$theme", 360, 800, dark, 1f),
                Variant("tablet_$theme", 1000, 700, dark, 1f),
                Variant("large_font_$theme", 360, 800, dark, 1.5f),
                Variant("double_font_$theme", 360, 800, dark, 2f)
            )
            listOf("loading", "empty", "branches_error", "agents_error", "content", "single_branch", "parameters", "expanded", "queue_error", "forbidden", "queuing", "agent_dialog", "parameter_dialog", "parameter_error", "scrolled").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            } + listOf("expanded", "scrolled").map { state -> arrayOf<Any>(state, Variant("landscape_$theme", 720, 360, dark, 1f)) }
        }
    }
}
