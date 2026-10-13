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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.theme.LocalFloatingActionClearance
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildDetailsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val build = BuildLaunchData("1", "/builds/1", number = "1", state = "running", changes = BuildCollectionLink("/changes"), webUrl = "https://server/build/1")

    @Test fun floatingActionHintsPreserveClickAndConfirmationCallbacks() {
        val actions = mutableListOf<BuildDetailsAction>()
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build), {}, actions::add, {}, {}, remember { SnackbarHostState() }) { Surface {} } } }
        listOf(BuildDetailsAction.Share, BuildDetailsAction.Browser, BuildDetailsAction.Cancel).forEach { action ->
            compose.onNodeWithTag("details:action:$action").performTouchInput { longClick() }
            compose.onNode(isPopup()).assertExists()
            assertTrue(actions.isEmpty())
            compose.mainClock.advanceTimeBy(2500)
        }
        compose.onNodeWithTag("details:action:Share").performClick()
        compose.onNodeWithTag("details:action:Browser").performClick()
        compose.onNodeWithTag("details:action:Cancel").performClick()
        assertEquals(listOf(BuildDetailsAction.Share, BuildDetailsAction.Browser, BuildDetailsAction.Cancel), actions)
    }

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
        compose.onNodeWithText("Stopping…", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("details:action:Cancel").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Stopping…"))
    }

    @Test fun unavailableCountIsExplicitAndMetadataRetryHasOneCallback() {
        var retries = 0
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build, countFailures = setOf(BuildDetailsSection.Changes)), {}, {}, {}, {}, remember { SnackbarHostState() }, onRetryMetadata = { retries++ }) { Surface {} } } }
        compose.onNodeWithText("Retry build counts and tips").assertExists()
        compose.onNodeWithTag("details:retry-metadata").performClick()
        assertEquals(1, retries)
    }

    @Test fun actionTipUsesSharedCoachmarkAndDismissesThroughTheViewModelCallback() {
        var dismissals = 0
        val prompt = BuildDetailsPromptUi(BuildDetailsPrompt.Stop)
        compose.setContent { TeamCityTheme { BuildDetailsScreen(BuildDetailsUiState(build, prompt = prompt), {}, {}, {}, {}, remember { SnackbarHostState() }, onDismissPrompt = { dismissals++ }) { Surface {} } } }
        compose.onNodeWithTag("details:coachmark:Stop").assertIsDisplayed()
        compose.onNodeWithTag("details:actions").assertDoesNotExist()
        compose.onNodeWithText("Stop build").assertIsDisplayed()
        compose.onNodeWithText("Stop this running build. You can also add it back to the queue.").assertExists()
        compose.onNodeWithTag("details:dismiss-prompt").assertIsDisplayed().performClick()
        assertEquals(1, dismissals)
    }

    @Test fun floatingActionsOverlayTheScrollableViewportAndLastItemCanClearThem() {
        compose.setContent {
            TeamCityTheme {
                BuildDetailsScreen(BuildDetailsUiState(build), {}, {}, {}, {}, remember { SnackbarHostState() }) {
                    val clearance = LocalFloatingActionClearance.current
                    LazyColumn(Modifier.fillMaxSize().testTag("content"), contentPadding = PaddingValues(bottom = clearance)) {
                        items(30) { index -> Text("Item $index", Modifier.fillMaxWidth().height(80.dp).testTag("item:$index")) }
                    }
                }
            }
        }
        val viewport = compose.onNodeWithTag("content").fetchSemanticsNode().boundsInRoot
        val actions = compose.onNodeWithTag("details:actions").fetchSemanticsNode().boundsInRoot
        val action = compose.onNodeWithTag("details:action:Cancel").fetchSemanticsNode().boundsInRoot
        val toolbar = compose.onNodeWithTag("details:toolbar").fetchSemanticsNode().boundsInRoot
        assertEquals(viewport.right - with(compose.density) { 16.dp.toPx() }, action.right, .5f)
        assertEquals(toolbar.top, action.top, .5f)
        assertEquals(toolbar.bottom, action.bottom, .5f)
        assertTrue("Content should extend behind the actions", viewport.bottom > actions.bottom)
        compose.onNodeWithTag("content").performTouchInput { swipeUp(startY = height * .65f, endY = height * .25f, durationMillis = 500) }
        compose.onNodeWithTag("details:toolbar").assertIsDisplayed()
        compose.onNodeWithTag("details:action:Cancel").assertIsDisplayed()
        assertEquals(toolbar, compose.onNodeWithTag("details:toolbar").fetchSemanticsNode().boundsInRoot)
        assertEquals(action, compose.onNodeWithTag("details:action:Cancel").fetchSemanticsNode().boundsInRoot)
        compose.onNodeWithTag("content").performScrollToIndex(29)
        compose.onNodeWithTag("item:29").assertIsDisplayed()
        assertTrue(compose.onNodeWithTag("item:29").fetchSemanticsNode().boundsInRoot.bottom <= actions.top)
    }

    @Test fun eachActionHasItsOwnTipAndSavingPreventsDuplicateDismissal() {
        val prompt = mutableStateOf(BuildDetailsPromptUi(BuildDetailsPrompt.Share))
        var dismissals = 0
        compose.setContent {
            TeamCityTheme {
                BuildDetailsScreen(BuildDetailsUiState(build, prompt = prompt.value), {}, {}, {}, {}, remember { SnackbarHostState() }, onDismissPrompt = { dismissals++ }) { Surface {} }
            }
        }
        compose.onNodeWithText("Share a link to this build with others.").assertIsDisplayed()
        compose.runOnIdle { prompt.value = BuildDetailsPromptUi(BuildDetailsPrompt.Share, saving = true) }
        compose.onNodeWithTag("details:dismiss-prompt").assertIsNotEnabled()
        assertEquals(0, dismissals)
        compose.runOnIdle { prompt.value = BuildDetailsPromptUi(BuildDetailsPrompt.Browser, saveFailed = true) }
        compose.onNodeWithText("Open this build’s page in a browser.").assertIsDisplayed()
        compose.onNodeWithText("Couldn’t save this tip. Try Continue again.").assertIsDisplayed()
        compose.onNodeWithTag("details:dismiss-prompt").performClick()
        assertEquals(1, dismissals)
    }
}
