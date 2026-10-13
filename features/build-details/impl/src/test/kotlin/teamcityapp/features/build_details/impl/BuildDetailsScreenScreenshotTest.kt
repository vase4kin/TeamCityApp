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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Assert.assertEquals
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
import teamcityapp.libraries.theme.LocalFloatingActionClearance
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
        val counts = mapOf(BuildDetailsSection.Changes to 12, BuildDetailsSection.Tests to 123)
        val state = when {
            stateName.startsWith("prompt_") -> {
                val kind = BuildDetailsPrompt.valueOf(stateName.removePrefix("prompt_").removeSuffix("_saving").removeSuffix("_failed"))
                BuildDetailsUiState(
                    snapshot.copy(
                        state = when (kind) {
                            BuildDetailsPrompt.Stop -> "running"
                            BuildDetailsPrompt.Remove -> "queued"
                            else -> "finished"
                        }
                    ),
                    prompt = BuildDetailsPromptUi(kind, saving = stateName.endsWith("_saving"), saveFailed = stateName.endsWith("_failed"))
                )
            }

            stateName.startsWith("notice_") -> {
                val notice = when (stateName) {
                    "notice_stopped" -> BuildDetailsNotice(1, R.string.text_build_is_stopped)
                    "notice_removed" -> BuildDetailsNotice(1, R.string.text_build_is_removed_from_queue)
                    "notice_restarted" -> BuildDetailsNotice(1, R.string.text_build_is_restarted, openQueued = true)
                    else -> BuildDetailsNotice(1, R.string.error_opening_build, openQueued = true)
                }
                BuildDetailsUiState(snapshot, notice = notice)
            }

            stateName == "counts_loading" -> BuildDetailsUiState(snapshot)

            stateName == "counts_partial_failed" -> BuildDetailsUiState(snapshot, countFailures = setOf(BuildDetailsSection.Tests))

            stateName == "counts_all_failed" -> BuildDetailsUiState(snapshot, countFailures = setOf(BuildDetailsSection.Changes, BuildDetailsSection.Tests))

            stateName == "tips_failed" -> BuildDetailsUiState(snapshot, onboardingFailed = true)

            stateName == "running" -> BuildDetailsUiState(snapshot.copy(state = "running"))

            stateName == "queued" -> BuildDetailsUiState(snapshot.copy(state = "queued"))

            stateName.startsWith("dialog_") -> {
                val kind = when {
                    stateName.contains("Stop") -> ConfirmationKind.Stop
                    stateName.contains("Remove") -> ConfirmationKind.Remove
                    else -> ConfirmationKind.Restart
                }
                BuildDetailsUiState(snapshot, confirmation = BuildConfirmation(snapshot, kind, own = !stateName.endsWith("other")))
            }

            stateName == "preparing_confirmation" -> BuildDetailsUiState(snapshot.copy(state = "running"), preparingConfirmation = true)

            stateName.startsWith("progress_") -> {
                val operation = BuildDetailsOperation.valueOf(stateName.removePrefix("progress_"))
                BuildDetailsUiState(
                    snapshot.copy(
                        state = when (operation) {
                            BuildDetailsOperation.Stop -> "running"
                            BuildDetailsOperation.Remove -> "queued"
                            BuildDetailsOperation.Restart -> "finished"
                        }
                    ),
                    operation = operation
                )
            }

            else -> BuildDetailsUiState(snapshot)
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                val snackbar = remember { SnackbarHostState() }
                val message = state.notice?.let { stringResource(it.message) }
                val action = state.notice?.actionLabel?.let { stringResource(it) }
                LaunchedEffect(message, action) {
                    message?.let { snackbar.showSnackbar(it, action, duration = SnackbarDuration.Indefinite) }
                }
                if (stateName.startsWith("section_")) {
                    BuildDetailsSectionScreen(BuildDetailsSection.valueOf(stateName.removePrefix("section_").removeSuffix("_scrolled")), "42", "Build Android application", {}) { if (stateName.endsWith("_scrolled")) AppBarScrollableTestContent() else Surface(Modifier.fillMaxSize()) {} }
                } else {
                    BuildDetailsScreen(state, {}, {}, {}, {}, snackbar) {
                        if (stateName == "overview_scrolled") {
                            AppBarScrollableTestContent()
                        } else if (stateName == "floating_content") {
                            val clearance = LocalFloatingActionClearance.current
                            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = clearance)) {
                                items(20) { index ->
                                    Surface(color = if (index % 2 == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer) {
                                        Text("Overview row ${index + 1}", Modifier.fillMaxWidth().padding(24.dp))
                                    }
                                }
                            }
                        } else {
                            Surface(Modifier.fillMaxSize()) {
                                Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
                                    Text("Build #42", style = MaterialTheme.typography.headlineSmall)
                                    Text("Overview content", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (stateName in listOf("tooltip", "finished", "running", "queued", "floating_content")) {
            val toolbar = compose.onNodeWithTag("details:toolbar").fetchSemanticsNode().boundsInRoot
            val action = compose.onNodeWithTag("details:action:${state.primaryAction.action}").fetchSemanticsNode().boundsInRoot
            assertEquals("Primary action should match the floating toolbar height", toolbar.height, action.height, 0.5f)
        }
        val actionBounds = if (!stateName.startsWith("section_") && state.prompt == null && state.confirmation == null) {
            compose.onNodeWithTag("details:action:${state.primaryAction.action}").assertIsDisplayed().fetchSemanticsNode().boundsInRoot.also { bounds ->
                assertEquals("Primary action stays at the right edge", variant.width - 16f, bounds.right, .5f)
                compose.onNodeWithTag("details:toolbar").assertIsDisplayed()
            }
        } else {
            null
        }
        if (stateName.endsWith("_scrolled")) {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("details:scroll").performTouchInput { swipeUp(startY = height * .65f, endY = height * .35f, durationMillis = 1_000) }
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
            compose.onNodeWithTag("screen:app-bar").assertIsDisplayed()
        }
        if (actionBounds != null) assertEquals("Scrolling keeps the action fixed", actionBounds, compose.onNodeWithTag("details:action:${state.primaryAction.action}").fetchSemanticsNode().boundsInRoot)
        if (stateName == "tooltip") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("details:action:Browser").performTouchInput { longClick() }
            compose.waitForIdle()
            compose.onNode(isPopup()).assertExists()
            compose.mainClock.autoAdvance = false
            captureScreenRoboImage("details_${stateName}_${variant.name}.png")
            return
        }
        compose.onRoot().captureRoboImage("details_${stateName}_${variant.name}.png")
        if (stateName.endsWith("_scrolled")) {
            compose.onNodeWithTag("details:scroll").performScrollToIndex(0)
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("details_${stateName}_${variant.name}_returned.png")
        }
    }
    companion object {
        private val snapshotSections = listOf(BuildDetailsSection.Changes, BuildDetailsSection.Tests, BuildDetailsSection.Artifacts, BuildDetailsSection.Parameters, BuildDetailsSection.Snapshots)

        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(Variant("phone_$theme", 360, 800, dark, 1f), Variant("tablet_$theme", 1000, 700, dark, 1f), Variant("large_font_$theme", 360, 800, dark, 1.5f))
            (listOf("tooltip", "finished", "running", "queued", "preparing_confirmation", "floating_content", "overview_scrolled", "prompt_Share_saving", "prompt_Share_failed") + snapshotSections.flatMap { listOf("section_${it.name}", "section_${it.name}_scrolled") } + listOf("dialog_Stop_own", "dialog_Stop_other", "dialog_Remove_own", "dialog_Remove_other", "dialog_Restart") + BuildDetailsOperation.entries.map { "progress_${it.name}" } + BuildDetailsPrompt.entries.map { "prompt_${it.name}" } + listOf("counts_loading", "counts_partial_failed", "counts_all_failed", "tips_failed", "notice_stopped", "notice_removed", "notice_restarted")).flatMap { state -> variants.map { arrayOf<Any>(state, it) } }
        }
    }
}
