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

package teamcityapp.features.snapshot_dependencies.impl

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
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SnapshotDependenciesScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val base = snapshotBuild()
        val rows = when (stateName) {
            "long_content" -> (1..30).map { snapshotBuild("$it") }

            "statuses" -> listOf(
                base.copy(state = "running", statusText = "Building and running tests"),
                snapshotBuild("2").copy(state = "queued", number = null, waitReason = "Waiting for an agent"),
                snapshotBuild("3").copy(status = "FAILURE", statusText = "Tests failed"),
                snapshotBuild("4").copy(status = "ERROR", statusText = "Failed to start"),
                snapshotBuild("5").copy(status = "UNKNOWN", statusText = "Unknown status")
            )

            "partial_configuration" -> listOf(base.copy(configuration = null), snapshotBuild("2").copy(configuration = BuildConfigurationData("Android_Debug", projectName = "Mobile")))

            "long_labels" -> listOf(base.copy(number = "123456789012345678901234567890", statusText = "Building a release with a long configuration name while waiting for snapshot dependencies and compatible agents", branchName = "feature/preserve-build-details-navigation-and-all-existing-tab-links", personal = true, pinned = true, configuration = BuildConfigurationData("long", "Build Android and iOS applications with all deployment environments", "Mobile", "Mobile application development")))

            else -> listOf(base.copy(personal = true, pinned = true), snapshotBuild("2"))
        }
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
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) { SnapshotDependenciesScreen(SnapshotDependenciesUiState(state), {}, {}, {}, { _, _ -> }) }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("snapshot_${stateName}_${variant.name}.png")
        if (stateName == "long_content") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("snapshot:list").performScrollToNode(hasTestTag("snapshot:build:30"))
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(100)
            compose.onNodeWithTag("snapshot:build:30").assertIsDisplayed()
            compose.onRoot().captureRoboImage("snapshot_${stateName}_${variant.name}_bottom.png")
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
            listOf("loading", "error", "empty", "empty_refreshing", "empty_refresh_failed", "content", "statuses", "partial_configuration", "long_labels", "refreshing", "refresh_failed", "long_content").flatMap { state -> variants.map { arrayOf<Any>(state, it) } }
        }
    }
}
