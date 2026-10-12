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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildDetailsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val build = BuildLaunchData("1", "/builds/1", number = "1", state = "running", changes = BuildCollectionLink("/changes"), webUrl = "https://server/build/1")

    @Test fun floatingBuildActionCallsUiRouterAndTabsAreAbsent() {
        var action: BuildDetailsAction? = null
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build), {}, { action = it }, {}, {}, remember { SnackbarHostState() }) { Surface {} } } }
        compose.onNodeWithTag("details:action:Cancel").performClick()
        assertEquals(BuildDetailsAction.Cancel, action)
        compose.onNodeWithTag("details:tab:Changes").assertDoesNotExist()
    }

    @Test fun cancellationCheckboxIsExplicitAndConfirmationHasOneCallback() {
        var confirmed: Boolean? = null
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build, confirmation = BuildConfirmation(build, ConfirmationKind.Stop)), {}, {}, { confirmed = it }, {}, remember { SnackbarHostState() }) { Surface {} } } }
        compose.onNodeWithText("Re-add build to the queue").performClick()
        compose.onNodeWithTag("details:readd").assertIsOn()
        compose.onNodeWithTag("details:readd").performClick()
        compose.onNodeWithTag("details:readd").assertIsOff()
        compose.onNodeWithText("Re-add build to the queue").performClick()
        compose.onNodeWithTag("details:confirm").performClick()
        assertEquals(true, confirmed)
    }

    @Test fun busyOperationDisablesNavigationAndDoesNotOfferConfirmation() {
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build, operation = BuildDetailsOperation.Stop), {}, {}, {}, {}, remember { SnackbarHostState() }) { Surface {} } } }
        compose.onNodeWithContentDescription("Navigate up").assertIsNotEnabled()
        compose.onNodeWithTag("details:action:Cancel").assertIsNotEnabled()
        compose.onNodeWithTag("details:confirm").assertDoesNotExist()
        compose.onNodeWithText("Stopping build..").assertExists()
    }

    @Test fun unavailableCountIsExplicitAndMetadataRetryHasOneCallback() {
        var retries = 0
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build, countFailures = setOf(BuildDetailsSection.Changes)), {}, {}, {}, {}, remember { SnackbarHostState() }, onRetryMetadata = { retries++ }) { Surface {} } } }
        compose.onNodeWithText("Retry build counts and tips").assertExists()
        compose.onNodeWithTag("details:retry-metadata").performClick()
        assertEquals(1, retries)
    }

    @Test fun menuTipUsesPreservedCopyAndDismissesThroughTheViewModelCallback() {
        var dismissals = 0
        val prompt = BuildDetailsPromptUi(BuildDetailsPrompt.Stop, description = R.string.text_onboarding_stop_build)
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build, prompt = prompt), {}, {}, {}, {}, remember { SnackbarHostState() }, onDismissPrompt = { dismissals++ }) { Surface {} } } }
        compose.onNodeWithTag("details:coachmark:Stop").assertIsDisplayed()
        compose.onNodeWithTag("details:actions").assertDoesNotExist()
        compose.onNodeWithText("Explore build actions").assertIsDisplayed()
        compose.onNodeWithText("Stop, share or open running builds in a browser").assertExists()
        compose.onNodeWithTag("details:dismiss-prompt").assertIsDisplayed().performClick()
        assertEquals(1, dismissals)
    }
}
