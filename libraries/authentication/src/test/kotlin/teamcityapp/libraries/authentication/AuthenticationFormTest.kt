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

package teamcityapp.libraries.authentication

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
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
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AuthenticationFormTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bothRowsOwnPaneWidthAndSingleSwitchSemantics() {
        compose.setContent { TeamCityTheme { AuthenticationForm(AuthenticationFormState(), {}, {}, {}, Modifier.fillMaxWidth().testTag("form"), horizontalPadding = 24.dp) } }
        val pane = compose.onNodeWithTag("form").fetchSemanticsNode().boundsInRoot
        listOf("guest", "ssl").forEach { name ->
            val row = compose.onNodeWithTag("auth:$name")
            row.assertIsOff().assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            val bounds = row.fetchSemanticsNode().boundsInRoot
            assertEquals(pane.left, bounds.left)
            assertEquals(pane.right, bounds.right)
        }
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ToggleableState), useUnmergedTree = true).assertCountEquals(2)
        val field = compose.onNodeWithTag("auth:url").fetchSemanticsNode().boundsInRoot
        assertEquals(pane.left + 24f, field.left, .5f)
        assertEquals(pane.right - 24f, field.right, .5f)
    }

    @Test fun eachEdgeLabelAndThumbTogglesExactlyOnceWithoutChangingCredentials() {
        val state = mutableStateOf(AuthenticationFormState(serverUrl = "https://server", userName = "Alice", password = "secret"))
        var guestCalls = 0
        var sslCalls = 0
        compose.setContent {
            TeamCityTheme {
                AuthenticationForm(state.value, {
                    guestCalls++
                    state.value = it
                }, {
                    sslCalls++
                    state.value = state.value.copy(sslDisabled = it)
                }, {}, horizontalPadding = 24.dp)
            }
        }
        listOf("guest", "ssl").forEach { name ->
            repeat(4) { target ->
                compose.onNodeWithTag("auth:$name").performTouchInput {
                    val x = when (target) {
                        0 -> 1f
                        1 -> width - 1f
                        2 -> 60f
                        else -> width - 48f
                    }
                    click(Offset(x, height - 28f))
                }
                assertEquals(if (name == "guest") target + 1 else 4, guestCalls)
                assertEquals(if (name == "ssl") target + 1 else 0, sslCalls)
                val checked = if (name == "guest") state.value.guest else state.value.sslDisabled
                assertEquals(target % 2 == 0, checked)
            }
        }
        assertEquals("https://server", state.value.serverUrl)
        assertEquals("Alice", state.value.userName)
        assertEquals("secret", state.value.password)
    }

    @Test fun busyRowsRejectEdgeLabelAndThumbTaps() {
        var changes = 0
        compose.setContent { TeamCityTheme { AuthenticationForm(AuthenticationFormState(busy = true), { changes++ }, { changes++ }, {}, horizontalPadding = 24.dp) } }
        listOf("guest", "ssl").forEach { name ->
            val row = compose.onNodeWithTag("auth:$name")
            row.assertIsNotEnabled().assertIsOff()
            listOf(1f, 60f, 310f, 359f).forEach { x -> row.performTouchInput { click(Offset(x, height - 28f)) } }
        }
        assertEquals(0, changes)
    }
}
