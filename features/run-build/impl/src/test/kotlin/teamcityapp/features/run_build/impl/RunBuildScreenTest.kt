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
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RunBuildScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bottomActionsSeparateOnlyWhileContentRemainsBelowTheViewport() {
        val viewportHeight = mutableStateOf(760)
        val state = RunBuildUiState(branches = emptyList(), agents = emptyList())
        var flat = androidx.compose.ui.graphics.Color.Unspecified
        var raised = androidx.compose.ui.graphics.Color.Unspecified
        compose.setContent {
            TeamCityTheme {
                flat = androidx.compose.material3.MaterialTheme.colorScheme.surface
                raised = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.height(viewportHeight.value.dp)) {
                    RunBuildScreen(state, {}, {}, {}, {}, {}, {})
                }
            }
        }
        fun assertBar(expected: androidx.compose.ui.graphics.Color) {
            compose.mainClock.advanceTimeBy(1_000)
            compose.waitForIdle()
            val pixels = compose.onNodeWithTag("run-build:bottom-action").captureToImage().toPixelMap()
            assertEquals(expected, pixels[8, 8])
            compose.onNodeWithTag("run-build:submit").assertIsDisplayed()
        }
        assertBar(flat)
        compose.runOnIdle { viewportHeight.value = 360 }
        assertBar(raised)
        val before = compose.onNodeWithTag("run-build:submit").fetchSemanticsNode().boundsInRoot
        val scroll = compose.onNodeWithTag("run-build:scroll")
        scroll.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10_000f) }
        assertBar(flat)
        assertEquals(before, compose.onNodeWithTag("run-build:submit").fetchSemanticsNode().boundsInRoot)
        scroll.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, -40f) }
        assertBar(raised)
    }

    @Test fun personalSwitchOnlyChangesItsOptionAndQueueUsesItsOwnCallback() {
        val state = mutableStateOf(RunBuildUiState(branches = emptyList(), agents = emptyList(), request = BuildRequest("bt1")))
        var queued = 0
        compose.setContent { TeamCityTheme { RunBuildScreen(state.value, { state.value = state.value.copy(request = it) }, { queued++ }, {}, {}, {}, {}) } }
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:personal").performClick()
        assertTrue(state.value.request.personal)
        assertTrue(state.value.request.cleanSources)
        assertFalse(state.value.request.queueAtTop)
        compose.onNodeWithTag("run-build:submit").assertHeightIsEqualTo(56.dp).performClick()
        assertEquals(1, queued)
    }

    @Test fun agentDialogReturnsTheSelectedAgentIdentity() {
        val agents = listOf(BuildAgent("1", "Linux"), BuildAgent("2", "Windows"))
        var selected: BuildAgent? = null
        compose.setContent { TeamCityTheme { RunBuildScreen(RunBuildUiState(agents = agents), {}, {}, {}, {}, {}, {}, agentDialog = true, onAgentSelected = { selected = it }) } }
        compose.onNodeWithText("Windows").performClick()
        assertEquals(agents[1], selected)
    }

    @Test fun optionTilesAcceptTouchesInAllTheirPadding() {
        val state = mutableStateOf(RunBuildUiState(branches = emptyList(), agents = emptyList(), request = BuildRequest("bt1")))
        var changes = 0
        compose.setContent {
            TeamCityTheme {
                RunBuildScreen(state.value, {
                    changes++
                    state.value = state.value.copy(request = it)
                }, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithTag("run-build:options").performClick()
        listOf("personal", "top", "clean").forEach { option ->
            val row = compose.onNodeWithTag("run-build:$option")
            row.performScrollTo()
            row.assertHeightIsAtLeast(48.dp).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Switch))
            val card = compose.onNodeWithTag("run-build:options-card").fetchSemanticsNode().boundsInRoot
            val bounds = row.fetchSemanticsNode().boundsInRoot
            assertEquals(card.left, bounds.left)
            assertEquals(card.right, bounds.right)
            repeat(6) { edge ->
                val before = changes
                val request = state.value.request
                row.performTouchInput {
                    click(
                        when (edge) {
                            0 -> Offset(1f, center.y)
                            1 -> Offset(width - 1f, center.y)
                            2 -> Offset(center.x, 1f)
                            3 -> Offset(center.x, height - 1f)
                            4 -> Offset(48f, center.y)
                            else -> Offset(width - 42f, center.y)
                        }
                    )
                }
                compose.runOnIdle {
                    assertEquals(before + 1, changes)
                    assertEquals(
                        when (option) {
                            "personal" -> request.copy(personal = !request.personal)
                            "top" -> request.copy(queueAtTop = !request.queueAtTop)
                            else -> request.copy(cleanSources = !request.cleanSources)
                        },
                        state.value.request
                    )
                }
            }
        }
    }

    @Test fun busyOptionRowsRejectTapsAtBothCardEdges() {
        val state = mutableStateOf(RunBuildUiState(branches = emptyList(), agents = emptyList()))
        var changes = 0
        compose.setContent { TeamCityTheme { RunBuildScreen(state.value, { changes++ }, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("run-build:options").performClick()
        compose.runOnIdle { state.value = state.value.copy(queuing = true) }
        listOf("personal", "top", "clean").forEach { option ->
            val row = compose.onNodeWithTag("run-build:$option")
            row.assertIsNotEnabled()
            row.performTouchInput {
                click(Offset(1f, center.y))
                click(Offset(width - 1f, center.y))
            }
        }
        assertEquals(0, changes)
    }

    @Test fun lightBackgroundContinuesBelowTheFormToTheBottom() {
        assertBackgroundContinuesToBottom(dark = false)
    }

    @Test fun darkBackgroundContinuesBelowTheFormToTheBottom() {
        assertBackgroundContinuesToBottom(dark = true)
    }

    @Test fun quickSetupKeepsConsequentialDefaultsVisibleAndDisclosureDoesNotMutateRequest() {
        val request = BuildRequest("bt1", parameters = listOf(BuildParameter("env", "test")))
        compose.setContent { TeamCityTheme { RunBuildScreen(RunBuildUiState(request = request), { fail("Disclosure must not alter the request") }, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("run-build:summary").assertTextContains("Personal: Off • Priority: Normal • Clean sources: On")
        compose.onNodeWithTag("run-build:parameter-count").assertTextEquals("Custom parameters: 1")
        compose.onNodeWithTag("run-build:personal").assertDoesNotExist()
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:personal").assertExists()
        compose.onNodeWithTag("run-build:options").performScrollTo().performClick()
        compose.onNodeWithTag("run-build:personal").assertDoesNotExist()
    }

    @Test fun errorsRevealOptionsAndBusySubmissionIsDisabled() {
        compose.setContent { TeamCityTheme { RunBuildScreen(RunBuildUiState(queueError = QueueBuildResult.Error, queuing = true), {}, { fail("Busy submission must be disabled") }, {}, {}, {}, {}) } }
        compose.onNodeWithTag("run-build:personal").assertExists()
        compose.onNodeWithTag("run-build:submit").assertIsNotEnabled()
    }

    @Test fun submissionFailureRemainsVisibleAfterEditingScrolledParameters() {
        val state = mutableStateOf(RunBuildUiState(request = BuildRequest("bt1", parameters = List(12) { BuildParameter("name$it", "value$it") })))
        compose.setContent { TeamCityTheme { RunBuildScreen(state.value, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithTag("run-build:options").performClick()
        compose.onNodeWithTag("run-build:clear").performScrollTo()
        compose.runOnIdle { state.value = state.value.copy(queueError = QueueBuildResult.Forbidden) }
        compose.onNodeWithTag("run-build:error").assertIsDisplayed()
        compose.onNodeWithTag("run-build:submit").assertIsDisplayed()
    }

    private fun assertBackgroundContinuesToBottom(dark: Boolean) {
        compose.setContent { TeamCityTheme(darkTheme = dark) { RunBuildScreen(RunBuildUiState(branches = emptyList(), agents = emptyList()), {}, {}, {}, {}, {}, {}) } }
        val expected = Color(if (dark) 0xFF11131B else 0xFFF9F9FF)
        val pixels = compose.onNodeWithTag("run-build:scroll").captureToImage().toPixelMap()
        assertEquals(expected, pixels[8, pixels.height - 8])
        val root = compose.onRoot().captureToImage().toPixelMap()
        assertEquals(expected, root[8, root.height - 8])
    }
}
