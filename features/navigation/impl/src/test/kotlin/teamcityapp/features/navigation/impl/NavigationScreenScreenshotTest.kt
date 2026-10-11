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

package teamcityapp.features.navigation.impl

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
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val project = ProjectReference("project", "TeamCity project")
        val rows = listOf(
            NavigationEntry.Project(ProjectReference("child", "Android projects"), "Build and release mobile applications"),
            NavigationEntry.Configuration(BuildConfigurationSummary("1", "Android build with a long name that wraps across multiple lines", "Assemble and test the application", project)),
            NavigationEntry.Configuration(BuildConfigurationSummary("2", "Release", null, project))
        )
        val state = when (stateName) {
            "loading" -> NavigationUiState(project)
            "error" -> NavigationUiState(project, ListUiState.Error)
            "empty" -> NavigationUiState(project, ListUiState.Empty())
            "empty_refreshing" -> NavigationUiState(project, ListUiState.Empty(isRefreshing = true))
            "empty_refresh_failed" -> NavigationUiState(project, ListUiState.Empty(refreshFailed = true))
            "refreshing" -> NavigationUiState(project, ListUiState.Content(rows, isRefreshing = true))
            "refresh_failed" -> NavigationUiState(project, ListUiState.Content(rows, refreshFailed = true))
            "rating" -> NavigationUiState(project, ListUiState.Content(rows), RatingPromptState.Available())
            "rating_saving" -> NavigationUiState(project, ListUiState.Content(rows), RatingPromptState.Available(isSaving = true))
            "rating_save_failed" -> NavigationUiState(project, ListUiState.Content(rows), RatingPromptState.Available(saveFailed = true))
            "rating_unavailable" -> NavigationUiState(project, ListUiState.Content(rows), RatingPromptState.Unavailable)
            "long_content" -> NavigationUiState(project, ListUiState.Content((1..30).map { NavigationEntry.Configuration(BuildConfigurationSummary("$it", "Build configuration $it", null, project)) }))
            else -> NavigationUiState(project, ListUiState.Content(rows))
        }
        compose.mainClock.autoAdvance = false
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { NavigationScreen(state.copy(root = stateName == "root_content"), {}, {}, {}, {}, {}, {}) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("navigation_${stateName}_${variant.name}.png")
        if (stateName == "long_content") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("navigation:list").performScrollToNode(hasTestTag("navigation:row:configuration:30"))
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(100)
            compose.onNodeWithTag("navigation:row:configuration:30").assertIsDisplayed()
            compose.onRoot().captureRoboImage("navigation_${stateName}_${variant.name}_bottom.png")
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
            listOf("loading", "error", "empty", "empty_refreshing", "empty_refresh_failed", "content", "root_content", "refreshing", "refresh_failed", "rating", "rating_saving", "rating_save_failed", "rating_unavailable", "long_content").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
