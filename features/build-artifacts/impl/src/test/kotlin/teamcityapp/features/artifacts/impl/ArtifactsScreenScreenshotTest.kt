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

package teamcityapp.features.artifacts.impl

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
import teamcityapp.features.artifacts.api.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtifactsScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
            Artifact("build-output", "/folder", childrenHref = "/folder/children"),
            Artifact("application-release-with-a-long-name-for-enlarged-text.apk", "/apk", 12_345_678, "/content/app.apk"),
            Artifact("build.zip", "/archive", 4096, "/content/build.zip", "/archive/children"),
            Artifact("index.html", "/metadata/index.html", 1024, "/content/index.html"),
            Artifact("empty.txt", "/empty", contentHref = "/content/empty.txt")
        )
        val list = when (stateName) {
            "loading" -> ListUiState.Loading
            "error" -> ListUiState.Error
            "empty" -> ListUiState.Empty()
            "empty_refreshing" -> ListUiState.Empty(isRefreshing = true)
            "empty_refresh_failed" -> ListUiState.Empty(refreshFailed = true)
            "refreshing" -> ListUiState.Content(rows, isRefreshing = true)
            "refresh_failed" -> ListUiState.Content(rows, refreshFailed = true)
            "long_content" -> ListUiState.Content((1..30).map { Artifact("Artifact file $it.zip", "/file/$it", 1000L * it, "/content/$it") })
            else -> ListUiState.Content(rows)
        }
        val download = when (stateName) {
            "downloading" -> ArtifactDownloadState.Downloading(ArtifactDownload("build.zip", "/content/build.zip"))
            "download_failed" -> ArtifactDownloadState.Failed(ArtifactDownload("build.zip", "/content/build.zip"))
            else -> ArtifactDownloadState.Idle
        }
        val platform = when (stateName) {
            "permission_denied" -> ArtifactPlatformError.PermissionDenied
            "file_unavailable" -> ArtifactPlatformError.FileUnavailable
            "browser_unavailable" -> ArtifactPlatformError.BrowserUnavailable
            else -> null
        }
        val state = ArtifactsUiState("Build artifacts", list, download)
        compose.mainClock.autoAdvance = false
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { ArtifactsScreen(state, true, platform, {}, {}, {}, {}, {}, {}, {}, {}) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (stateName == "downloading" || platform != null) {
            captureScreenRoboImage("artifacts_${stateName}_${variant.name}.png")
        } else {
            compose.onRoot().captureRoboImage("artifacts_${stateName}_${variant.name}.png")
        }
        if (stateName == "long_content") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("artifacts:list").performScrollToNode(hasTestTag("artifacts:row:/file/30"))
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(100)
            compose.onNodeWithTag("artifacts:row:/file/30").assertIsDisplayed()
            compose.onRoot().captureRoboImage("artifacts_${stateName}_${variant.name}_bottom.png")
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
            listOf("loading", "error", "empty", "empty_refreshing", "empty_refresh_failed", "content", "refreshing", "refresh_failed", "long_content", "downloading", "download_failed", "permission_denied", "file_unavailable", "browser_unavailable").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
