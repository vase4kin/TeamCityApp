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

package teamcityapp.features.changes.impl

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
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
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

/** All list, append and optional-count states with deterministic hydrated changes. */
@OptIn(ExperimentalTestApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChangesScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
            change(),
            change("43").copy(comment = "Allow a cancelled build to finish cleanup before the next queued build starts", files = List(12) { teamcityapp.features.change_details.api.ChangedFile("File$it.kt", "edited") }),
            change("44").copy(comment = "Update documentation", files = emptyList())
        )
        val state: ListUiState<teamcityapp.features.change_details.api.ChangeDetails> = when (stateName) {
            "loading" -> ListUiState.Loading
            "error" -> ListUiState.Error
            "empty" -> ListUiState.Empty()
            "empty_refreshing" -> ListUiState.Empty(isRefreshing = true)
            "empty_refresh_failed" -> ListUiState.Empty(refreshFailed = true)
            "refreshing" -> ListUiState.Content(rows, isRefreshing = true)
            "refresh_failed" -> ListUiState.Content(rows, refreshFailed = true)
            else -> ListUiState.Content(rows)
        }
        val count = if (stateName == "count_unavailable") ChangesCountState.Unavailable else ChangesCountState.Available(3)
        val append = when (stateName) {
            "append_loading" -> ChangesAppendState.Loading
            "append_failed" -> ChangesAppendState.Error
            else -> ChangesAppendState.Idle
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                ChangesScreen(state, count, rows.size, { rows[it] }, { rows[it].id }, append, {}, {}, {}, {})
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (stateName == "count_unavailable") compose.onNodeWithText("Retry count").assertIsDisplayed()
        compose.onRoot().captureRoboImage("changes_${stateName}_${variant.name}.png")
        if (state is ListUiState.Content && variant.fontScale > 1f) {
            // Enlarged text can place the last row or append action below the viewport.
            compose.onNodeWithTag("changes:list").performScrollToIndex(rows.lastIndex)
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
            compose.onNodeWithTag("changes:change:44").assertIsDisplayed()
            if (append == ChangesAppendState.Error) compose.onNodeWithText("Retry").assertIsDisplayed()
            compose.onRoot().captureRoboImage("changes_${stateName}_${variant.name}_bottom.png")
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
            listOf("loading", "error", "empty", "empty_refreshing", "empty_refresh_failed", "content", "refreshing", "refresh_failed", "append_loading", "append_failed", "count_unavailable").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
