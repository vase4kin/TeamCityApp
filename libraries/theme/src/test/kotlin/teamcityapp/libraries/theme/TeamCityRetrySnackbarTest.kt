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

package teamcityapp.libraries.theme

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
class TeamCityRetrySnackbarTest {
    @get:Rule val compose = createComposeRule()

    @Test fun dismissedFailureDoesNotReplayOnRecompositionAndNextFailureCanRetry() {
        val message = mutableStateOf<String?>("Count unavailable")
        val generation = mutableStateOf(0)
        val callback = mutableStateOf(0)
        var retriedWith: Int? = null
        compose.setContent {
            val snackbar = remember { SnackbarHostState() }
            val current = callback.value
            TeamCityTheme {
                TeamCityRetrySnackbar(message.value, "Retry count", snackbar, generation.value) { retriedWith = current }
                Box(Modifier.fillMaxSize()) { TeamCitySnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter)) }
            }
        }
        compose.onNodeWithContentDescription("Dismiss").performClick()
        compose.runOnIdle { callback.value = 1 }
        compose.onNodeWithText("Count unavailable").assertDoesNotExist()
        compose.runOnIdle { generation.value++ }
        compose.runOnIdle { callback.value = 2 }
        compose.onNodeWithText("Retry count").performClick()
        assertEquals(2, retriedWith)
    }

    @Test fun immediateRepeatedFailureRemainsRetryable() {
        var retries = 0
        compose.setContent {
            val snackbar = remember { SnackbarHostState() }
            TeamCityTheme {
                TeamCityRetrySnackbar("Count unavailable", "Retry count", snackbar) { retries++ }
                Box(Modifier.fillMaxSize()) { TeamCitySnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter)) }
            }
        }
        repeat(2) { compose.onNodeWithText("Retry count").assertIsDisplayed().performClick() }
        assertEquals(2, retries)
    }

    @Test fun recoveryAndDepartureRemovePendingFailureWithoutRetry() {
        val message = mutableStateOf<String?>("Count unavailable")
        val visible = mutableStateOf(true)
        var retries = 0
        compose.setContent {
            val snackbar = remember { SnackbarHostState() }
            TeamCityTheme {
                if (visible.value) TeamCityRetrySnackbar(message.value, "Retry count", snackbar) { retries++ }
                Box(Modifier.fillMaxSize()) { TeamCitySnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter)) }
            }
        }
        compose.onNodeWithText("Count unavailable").assertIsDisplayed()
        compose.runOnIdle { message.value = null }
        compose.onNodeWithText("Count unavailable").assertDoesNotExist()
        compose.runOnIdle { message.value = "Count unavailable" }
        compose.onNodeWithText("Count unavailable").assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.onNodeWithText("Count unavailable").assertDoesNotExist()
        assertEquals(0, retries)
    }
}
