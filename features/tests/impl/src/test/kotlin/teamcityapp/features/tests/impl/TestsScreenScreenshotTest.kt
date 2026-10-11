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

package teamcityapp.features.tests.impl

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
import teamcityapp.features.tests.api.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TestsScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val filter = when (stateName) {
            "empty_passed", "content_passed" -> TestsFilter.Passed
            "empty_ignored", "content_ignored" -> TestsFilter.Ignored
            else -> TestsFilter.Failed
        }
        val status = when (stateName) {
            "content_passed" -> TestStatus.Passed
            "content_ignored" -> TestStatus.Ignored
            "content_error" -> TestStatus.Error
            else -> TestStatus.Failed
        }
        val rows = when (stateName) {
            "long_content" -> (1..30).map { testOccurrence("$it").copy(name = "BuildQueueTest.testCase$it") }
            "mixed_sections" -> TestStatus.entries.mapIndexed { index, current -> testOccurrence("$index", current).copy(name = "BuildQueueTest.testCase$index") }
            else -> listOf(testOccurrence(), testOccurrence("2", status).copy(name = "BuildQueueTest.whenTheServerReturnsAnUnexpectedStatusThenTheClientKeepsPreviousResultsAndAllowsRetry"), testOccurrence("3", status)).map { it.copy(status = status) }
        }
        val state: ListUiState<teamcityapp.features.tests.api.TestOccurrence> = when (stateName) {
            "loading" -> ListUiState.Loading
            "error" -> ListUiState.Error
            "empty_failed", "empty_passed", "empty_ignored", "zero_counts", "default_failed_only_passed" -> ListUiState.Empty()
            "empty_refreshing" -> ListUiState.Empty(isRefreshing = true)
            "empty_refresh_failed" -> ListUiState.Empty(refreshFailed = true)
            "refreshing" -> ListUiState.Content(rows, isRefreshing = true)
            "refresh_failed" -> ListUiState.Content(rows, refreshFailed = true)
            else -> ListUiState.Content(rows)
        }
        val counts = when (stateName) {
            "zero_counts" -> TestsCounts(0, 0, 0)
            "default_failed_only_passed" -> TestsCounts(10, 0, 0)
            "mixed_sections" -> TestsCounts(1, 1, 2)
            "long_content" -> TestsCounts(0, 30, 0)
            else -> TestsCounts(12, 3, 3)
        }
        val count = if (stateName == "count_unavailable") TestsCountState.Unavailable else TestsCountState.Available(counts.passed + counts.failed + counts.ignored)
        val append = when (stateName) {
            "append_loading" -> TestsAppendState.Loading
            "append_failed" -> TestsAppendState.Error
            else -> TestsAppendState.Idle
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                TestsScreen(state, filter, counts, count, rows.size, { rows[it] }, { rows[it] }, append, {}, {}, {}, {}, {})
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (stateName == "count_unavailable") compose.onNodeWithText("Retry count").assertIsDisplayed()
        compose.onRoot().captureRoboImage("tests_${stateName}_${variant.name}.png")
        if (stateName == "long_content") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("tests:list").performScrollToNode(hasTestTag("tests:test:30"))
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(100)
            compose.onNodeWithTag("tests:test:30").assertIsDisplayed()
            compose.onRoot().captureRoboImage("tests_${stateName}_${variant.name}_bottom.png")
        }
        if (append == TestsAppendState.Error && variant.fontScale > 1f) {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("tests:list").performScrollToNode(hasText("Retry"))
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(100)
            compose.onNodeWithText("Retry").assertIsDisplayed()
            compose.onRoot().captureRoboImage("tests_${stateName}_${variant.name}_bottom.png")
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
            listOf("loading", "error", "empty_failed", "empty_passed", "empty_ignored", "empty_refreshing", "empty_refresh_failed", "content_failed", "content_passed", "content_ignored", "content_error", "mixed_sections", "refreshing", "refresh_failed", "append_loading", "append_failed", "count_unavailable", "long_content", "zero_counts", "default_failed_only_passed").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
