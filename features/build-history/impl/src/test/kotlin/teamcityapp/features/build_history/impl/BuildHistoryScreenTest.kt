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

package teamcityapp.features.build_history.impl

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@Composable
internal fun HistoryTestScreen(
    state: ListUiState<BuildLaunchData> = ListUiState.Content(listOf(historyBuild())),
    controls: BuildHistoryControls = BuildHistoryControls(FavoriteState.Available(false), OnboardingState.Available()),
    rows: List<BuildLaunchData> = listOf(historyBuild()),
    append: HistoryAppendState = HistoryAppendState.Idle,
    onBack: () -> Unit = {},
    onRun: () -> Unit = {},
    onFilter: () -> Unit = {},
    onFavorite: () -> Unit = {},
    onFavoriteRetry: () -> Unit = {},
    onPromptRetry: () -> Unit = {},
    onPromptDismiss: () -> Unit = {},
    onRetry: () -> Unit = {},
    onAppendRetry: () -> Unit = {},
    onBuild: (BuildLaunchData) -> Unit = {},
    onQueuedRetry: () -> Unit = {},
    onQueuedDismiss: () -> Unit = {},
    snackbar: androidx.compose.material3.SnackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }
) {
    BuildHistoryScreen(
        "Build Android and check all supported configurations", state, controls, rows.size, { rows[it] }, { rows[it] }, append,
        onBack, onRun, onFilter, onFavorite, onFavoriteRetry, onPromptRetry, onPromptDismiss, onRetry, onRetry, onAppendRetry, onBuild, onQueuedRetry, onQueuedDismiss, snackbar = snackbar
    )
}

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildHistoryScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun duplicateLegacyIdsAndRepeatedRowsRenderAndDispatchTheirOwnFullSnapshots() {
        val first = historyBuild("randomId").copy(href = "/builds/first", number = "1")
        val second = historyBuild("randomId").copy(href = "/builds/second", number = "2")
        var opened: BuildLaunchData? = null
        compose.setContent { TeamCityTheme { HistoryTestScreen(rows = listOf(first, second, first), onBuild = { opened = it }) } }
        compose.onAllNodesWithTag("history:build:randomId").assertCountEquals(3)
        compose.onAllNodesWithTag("history:build:randomId")[1].performClick()
        assertEquals(second, opened)
        compose.onAllNodesWithTag("history:build:randomId")[2].performClick()
        assertEquals(first, opened)
    }

    @Test fun buildRowDispatchesCompleteHydratedBuildAndDateHeaderCannotNavigate() {
        val build = historyBuild().copy(personal = true, pinned = true)
        var opened: BuildLaunchData? = null
        compose.setContent { TeamCityTheme { HistoryTestScreen(rows = listOf(build), onBuild = { opened = it }) } }
        compose.onNodeWithTag("history:section:10 October").assertIsDisplayed().assertHasNoClickAction()
        compose.onNodeWithContentDescription("Personal build").assertIsDisplayed()
        compose.onNodeWithContentDescription("Pinned build").assertIsDisplayed()
        compose.onNodeWithTag("history:build:42").performClick()
        assertEquals(build, opened)
    }

    @Test fun queuedAndAdjacentDateSectionsAreGroupedWithoutClickableConfigurationHeaders() {
        val rows = listOf(
            historyBuild("queued", "queued"),
            historyBuild("42"),
            historyBuild("43"),
            historyBuild("44").copy(startDate = "20261009T102030Z")
        )
        compose.setContent { TeamCityTheme { HistoryTestScreen(rows = rows) } }
        compose.onNodeWithText("Queued").assertIsDisplayed().assertHasNoClickAction()
        compose.onAllNodesWithText("10 October").assertCountEquals(1)
        compose.onNodeWithTag("history:list").performScrollToNode(hasTestTag("history:build:44"))
        compose.onNodeWithText("09 October").assertIsDisplayed().assertHasNoClickAction()
    }

    @Test fun toolbarAndRunActionsRemainUiCallbacks() {
        var backs = 0
        var runs = 0
        var filters = 0
        var favorites = 0
        compose.setContent { TeamCityTheme { HistoryTestScreen(onBack = { backs++ }, onRun = { runs++ }, onFilter = { filters++ }, onFavorite = { favorites++ }) } }
        compose.onNodeWithTag("history:back").performClick()
        compose.onNodeWithTag("history:run").performClick()
        compose.onNodeWithTag("history:filter").performClick()
        compose.onNodeWithTag("history:favorite").performClick()
        assertEquals(listOf(1, 1, 1, 1), listOf(backs, runs, filters, favorites))
    }

    @Test fun emptyRetainsRunAndFavoriteWhileInitialErrorHidesOnlyRun() {
        val state = androidx.compose.runtime.mutableStateOf<ListUiState<BuildLaunchData>>(ListUiState.Empty())
        compose.setContent { TeamCityTheme { HistoryTestScreen(state = state.value) } }
        compose.onNodeWithText("No builds").assertIsDisplayed()
        compose.onNodeWithTag("history:run").assertIsDisplayed()
        compose.runOnIdle { state.value = ListUiState.Error }
        compose.onNodeWithTag("history:run").assertDoesNotExist()
        compose.onNodeWithTag("history:filter").assertIsDisplayed()
        compose.onNodeWithTag("history:favorite").assertIsDisplayed()
    }

    @Test fun unavailableFavoriteRetryDoesNotToggleAndSavingDisablesFavoriteControl() {
        var retries = 0
        var toggles = 0
        val favorite = androidx.compose.runtime.mutableStateOf<FavoriteState>(FavoriteState.Unavailable)
        compose.setContent { TeamCityTheme { HistoryTestScreen(controls = BuildHistoryControls(favorite.value, OnboardingState.Available()), onFavorite = { toggles++ }, onFavoriteRetry = { retries++ }) } }
        compose.onNodeWithText("Favorites are unavailable.").assertIsDisplayed()
        compose.onNodeWithTag("history:favorite").performClick()
        assertEquals(1, retries)
        assertEquals(0, toggles)
        compose.runOnIdle { favorite.value = FavoriteState.Available(true, updating = true) }
        compose.onNodeWithTag("history:favorite").assertIsNotEnabled()
    }

    @Test fun appendRetryKeepsBuildRowsVisible() {
        var retries = 0
        compose.setContent { TeamCityTheme { HistoryTestScreen(append = HistoryAppendState.Error, onAppendRetry = { retries++ }) } }
        compose.onNodeWithTag("history:build:42").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }

    @Test fun contextualTipsNameActualRunFilterAndFavoriteTargetsAndDismissInOrder() {
        val prompt = androidx.compose.runtime.mutableStateOf(BuildHistoryPrompt.Run)
        var dismissals = 0
        compose.setContent {
            TeamCityTheme {
                HistoryTestScreen(
                    controls = BuildHistoryControls(FavoriteState.Available(false), OnboardingState.Available(prompt.value)),
                    onPromptDismiss = { dismissals++ }
                )
            }
        }
        compose.onNodeWithTag("history:coachmark:Run").assertIsDisplayed()
        compose.onNodeWithText("Choose a branch, an agent or even provide custom parameters").assertIsDisplayed()
        compose.onNodeWithTag("history:prompt-dismiss").performClick()
        compose.runOnIdle { prompt.value = BuildHistoryPrompt.Filter }
        compose.onNodeWithTag("history:coachmark:Filter").assertIsDisplayed()
        compose.onNodeWithTag("history:prompt-dismiss").performClick()
        compose.runOnIdle { prompt.value = BuildHistoryPrompt.Favorite }
        compose.onNodeWithTag("history:coachmark:Favorite").assertIsDisplayed()
        compose.onNodeWithTag("history:prompt-dismiss").performClick()
        assertEquals(3, dismissals)
    }

    @Test fun promptSaveFailureKeepsTipAndOffersRetryWhileSavingDisablesDismiss() {
        val state = androidx.compose.runtime.mutableStateOf(OnboardingState.Available(BuildHistoryPrompt.Filter, saveFailed = true))
        var retries = 0
        compose.setContent { TeamCityTheme { HistoryTestScreen(controls = BuildHistoryControls(FavoriteState.Available(false), state.value), onPromptDismiss = { retries++ }) } }
        compose.onNodeWithText("Could not save this tip. Try again.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
        compose.runOnIdle { state.value = state.value.copy(saving = true, saveFailed = false) }
        compose.onNodeWithTag("history:prompt-dismiss").assertIsNotEnabled()
    }

    @Test fun queuedShowFailureOffersExplicitRetryAndDismissActions() {
        var retries = 0
        var dismissals = 0
        compose.setContent {
            TeamCityTheme {
                HistoryTestScreen(
                    controls = BuildHistoryControls(FavoriteState.Available(false), OnboardingState.Available(), QueuedBuildState.Failed),
                    onQueuedRetry = { retries++ },
                    onQueuedDismiss = { dismissals++ }
                )
            }
        }
        compose.onNodeWithText("We encountered problem opening build").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithText("Cancel").performClick()
        assertEquals(1, retries)
        assertEquals(1, dismissals)
    }
}
