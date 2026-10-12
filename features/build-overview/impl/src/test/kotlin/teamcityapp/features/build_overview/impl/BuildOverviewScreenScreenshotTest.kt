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

package teamcityapp.features.build_overview.impl

import android.app.Application
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
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildOverviewScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val build = BuildOverviewFixtures.build(stateName)
        val rows = overviewRows(build)
        val counts = when (stateName) {
            "counts_loading" -> mapOf(BuildDetailsSection.Changes to BuildDetailsCount.Loading, BuildDetailsSection.Tests to BuildDetailsCount.Loading)
            "counts_partial_failed" -> mapOf(BuildDetailsSection.Changes to BuildDetailsCount.Available(1), BuildDetailsSection.Tests to BuildDetailsCount.Unavailable)
            "counts_all_failed" -> mapOf(BuildDetailsSection.Changes to BuildDetailsCount.Unavailable, BuildDetailsSection.Tests to BuildDetailsCount.Unavailable)
            else -> mapOf(BuildDetailsSection.Changes to BuildDetailsCount.Available(1), BuildDetailsSection.Tests to BuildDetailsCount.Available(16))
        }
        val state = when (stateName) {
            "loading" -> BuildOverviewUiState()
            "error" -> BuildOverviewUiState(list = ListUiState.Error)
            "empty" -> BuildOverviewUiState(list = ListUiState.Empty())
            "refreshing" -> buildOverviewState(ListUiState.Content(listOf(build), isRefreshing = true), counts = counts)
            "refresh_failed" -> buildOverviewState(ListUiState.Content(listOf(build), refreshFailed = true), counts = counts)
            else -> buildOverviewState(ListUiState.Content(listOf(build)), counts = counts)
        }
        compose.mainClock.autoAdvance = false
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { BuildOverviewScreen(state, {}, {}, {}) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("overview_${stateName}_${variant.name}.png")
        if (stateName == "long_content" || stateName == "full_optional" || stateName.startsWith("hub_") || stateName.startsWith("counts_") || stateName == "partial_context" || stateName == "configuration_only") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("overview:list").performScrollToNode(hasTestTag("overview:row:${rows.last().field}"))
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(100)
            compose.onNodeWithTag("overview:row:${rows.last().field}").assertIsDisplayed()
            compose.onRoot().captureRoboImage("overview_${stateName}_${variant.name}_bottom.png")
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
            listOf("hub_finished", "hub_running", "hub_queued", "counts_loading", "counts_partial_failed", "counts_all_failed", "partial_context", "configuration_only", "loading", "error", "empty", "finished", "running", "queued", "queued_fallback", "failure", "status_error", "status_unknown", "cancelled", "cancelled_without_user", "user_trigger", "restarted", "deleted_user", "configuration_trigger", "deleted_configuration", "unknown_trigger", "missing_trigger_details", "personal", "minimal", "missing_dates", "full_optional", "refreshing", "refresh_failed", "long_content").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
