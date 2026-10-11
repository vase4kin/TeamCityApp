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

package teamcityapp.features.build_history.impl

import android.app.Application
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

/** Loading/list/append, favorite/tip failures, all contextual tips, queued Show and snackbars. */
@OptIn(ExperimentalTestApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildHistoryScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val rows = listOf(
            historyBuild("queued", "queued").copy(number = null, waitReason = "Waiting for an agent with the required Android SDK and sufficient disk space", branchName = "feature/compose-build-history"),
            historyBuild("running", "running").copy(personal = true, pinned = true, statusText = "Running Android instrumented tests", branchName = "feature/lists"),
            historyBuild("42").copy(status = "FAILURE", statusText = "Compilation failed in the app integration module", branchName = "main"),
            historyBuild("43").copy(startDate = "20261009T102030Z", statusText = "All 214 tests passed")
        )
        val state: ListUiState<BuildLaunchData> = when (stateName) {
            "loading" -> ListUiState.Loading
            "error" -> ListUiState.Error
            "empty" -> ListUiState.Empty()
            "empty_refreshing" -> ListUiState.Empty(isRefreshing = true)
            "empty_refresh_failed" -> ListUiState.Empty(refreshFailed = true)
            "refreshing" -> ListUiState.Content(rows, isRefreshing = true)
            "refresh_failed" -> ListUiState.Content(rows, refreshFailed = true)
            else -> ListUiState.Content(rows)
        }
        val favorite = when (stateName) {
            "favorite_loading" -> FavoriteState.Loading
            "favorite_unavailable" -> FavoriteState.Unavailable
            "favorite_updating" -> FavoriteState.Available(false, updating = true)
            "favorite_update_failed" -> FavoriteState.Available(false, updateFailed = true)
            "favorite_selected", "notice_favorite_added" -> FavoriteState.Available(true)
            else -> FavoriteState.Available(false)
        }
        val onboarding = when (stateName) {
            "onboarding_loading" -> OnboardingState.Loading
            "onboarding_unavailable" -> OnboardingState.Unavailable
            "prompt_run" -> OnboardingState.Available(BuildHistoryPrompt.Run)
            "prompt_filter" -> OnboardingState.Available(BuildHistoryPrompt.Filter)
            "prompt_favorite" -> OnboardingState.Available(BuildHistoryPrompt.Favorite)
            "prompt_saving" -> OnboardingState.Available(BuildHistoryPrompt.Filter, saving = true)
            "prompt_save_failed" -> OnboardingState.Available(BuildHistoryPrompt.Filter, saveFailed = true)
            else -> OnboardingState.Available()
        }
        val opening = when (stateName) {
            "queued_loading" -> QueuedBuildState.Loading
            "queued_failed" -> QueuedBuildState.Failed
            else -> QueuedBuildState.Idle
        }
        val append = when (stateName) {
            "append_loading" -> HistoryAppendState.Loading
            "append_failed" -> HistoryAppendState.Error
            else -> HistoryAppendState.Idle
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val snackbar = remember { SnackbarHostState() }
            LaunchedEffect(stateName) {
                when (stateName) {
                    "notice_queued" -> snackbar.showSnackbar("Build is added to build queue", "Show", true, SnackbarDuration.Indefinite)
                    "notice_filters" -> snackbar.showSnackbar("Build filters have been applied", "Reset filters", true, SnackbarDuration.Indefinite)
                    "notice_favorite_added" -> snackbar.showSnackbar("The configuration has been added to favorites", "Open favorites", true, SnackbarDuration.Indefinite)
                    "notice_favorite_removed" -> snackbar.showSnackbar("The configuration has been removed from favorites", withDismissAction = true, duration = SnackbarDuration.Indefinite)
                }
            }
            TeamCityTheme(darkTheme = variant.dark) {
                HistoryTestScreen(state, BuildHistoryControls(favorite, onboarding, opening), rows, append, snackbar = snackbar)
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val roots = compose.onAllNodes(isRoot())
        roots[roots.fetchSemanticsNodes().lastIndex].captureRoboImage("history_${stateName}_${variant.name}.png")
        if (state is ListUiState.Content && variant.fontScale > 1f && opening == QueuedBuildState.Idle && (onboarding !is OnboardingState.Available || onboarding.prompt == null)) {
            compose.mainClock.autoAdvance = true
            if (append == HistoryAppendState.Idle) {
                compose.onNodeWithTag("history:list").performScrollToNode(hasTestTag("history:build:43"))
            } else {
                compose.onNodeWithTag("history:list").performScrollToIndex(rows.size)
            }
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
            compose.onNodeWithTag("history:build:43").assertIsDisplayed()
            compose.onRoot().captureRoboImage("history_${stateName}_${variant.name}_bottom.png")
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
            listOf(
                "loading", "error", "empty", "empty_refreshing", "empty_refresh_failed", "content", "refreshing", "refresh_failed", "append_loading", "append_failed",
                "favorite_loading", "favorite_unavailable", "favorite_updating", "favorite_update_failed", "favorite_selected", "onboarding_loading", "onboarding_unavailable",
                "prompt_run", "prompt_filter", "prompt_favorite", "prompt_saving", "prompt_save_failed", "queued_loading", "queued_failed",
                "notice_queued", "notice_filters", "notice_favorite_added", "notice_favorite_removed"
            ).flatMap { name ->
                variants.map { arrayOf<Any>(name, it) }
            }
        }
    }
}
