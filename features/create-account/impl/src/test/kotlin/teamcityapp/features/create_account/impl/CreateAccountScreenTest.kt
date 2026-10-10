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

package teamcityapp.features.create_account.impl

import android.app.Application
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
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
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CreateAccountScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bottomActionsSeparateOnlyWhileContentRemainsBelowTheViewport() {
        val viewportHeight = mutableStateOf(760)
        val state = CreateAccountUiState()
        var flat = androidx.compose.ui.graphics.Color.Unspecified
        var raised = androidx.compose.ui.graphics.Color.Unspecified
        compose.setContent {
            TeamCityTheme {
                flat = androidx.compose.material3.MaterialTheme.colorScheme.surface
                raised = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.height(viewportHeight.value.dp)) {
                    CreateAccountScreen(state, {}, {}, {}, {})
                }
            }
        }
        fun assertBar(expected: androidx.compose.ui.graphics.Color) {
            compose.mainClock.advanceTimeBy(1_000)
            compose.waitForIdle()
            val pixels = compose.onNodeWithTag("create-account:bottom-action").captureToImage().toPixelMap()
            assertEquals(expected, pixels[8, 8])
            compose.onNodeWithTag("create-account:submit").assertIsDisplayed()
        }
        assertBar(flat)
        compose.runOnIdle { viewportHeight.value = 360 }
        assertBar(raised)
        val before = compose.onNodeWithTag("create-account:submit").fetchSemanticsNode().boundsInRoot
        val scroll = compose.onNodeWithTag("create-account:scroll")
        scroll.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10_000f) }
        assertBar(flat)
        assertEquals(before, compose.onNodeWithTag("create-account:submit").fetchSemanticsNode().boundsInRoot)
        scroll.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, -40f) }
        assertBar(raised)
    }

    @Config(qualifiers = "en-rUS-w1000dp-h1000dp-notnight-mdpi")
    @Test
    fun tabletSwitchRowsStayWithin560dpFormPane() {
        compose.setContent { TeamCityTheme { CreateAccountScreen(CreateAccountUiState(), {}, {}, {}, {}) } }
        compose.onNodeWithTag("create-account:form").assertWidthIsEqualTo(560.dp)
        listOf("auth:guest", "auth:ssl").forEach { compose.onNodeWithTag(it).assertWidthIsEqualTo(560.dp) }
    }

    @Test fun switchRowsSpanTheBoundedFormPane() {
        compose.setContent { TeamCityTheme { CreateAccountScreen(CreateAccountUiState(), { }, { }, { }, { }) } }
        val pane = compose.onNodeWithTag("create-account:form").fetchSemanticsNode().boundsInRoot
        listOf("auth:guest", "auth:ssl").forEach { tag ->
            val row = compose.onNodeWithTag(tag)
            row.assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Switch))
            val bounds = row.fetchSemanticsNode().boundsInRoot
            assertEquals(pane.left, bounds.left)
            assertEquals(pane.right, bounds.right)
        }
    }

    @Test fun discardDialogCanBeCancelledOrConfirmed() {
        var discarded = 0
        var cancelled = 0
        compose.setContent { TeamCityTheme { CreateAccountScreen(CreateAccountUiState(), {}, {}, {}, {}, dialog = CreateAccountDialog.Discard, onConfirm = { discarded++ }, onDecline = { cancelled++ }) } }
        compose.onNodeWithText("Cancel").performClick()
        assertEquals(1, cancelled)
        assertEquals(0, discarded)
        compose.onNodeWithText("Discard").performClick()
        assertEquals(1, discarded)
    }

    @Test fun savingDisablesFieldsAndToolbarSubmission() {
        var submitted = 0
        compose.setContent { TeamCityTheme { CreateAccountScreen(CreateAccountUiState(AuthenticationFormState(busy = true)), {}, { submitted++ }, {}, {}) } }
        compose.onNodeWithTag("auth:url").assertIsNotEnabled()
        compose.onNodeWithTag("auth:progress").assertExists()
        assertEquals(0, submitted)
    }
}
