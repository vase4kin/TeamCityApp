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

package teamcityapp.features.tests.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.tests.api.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TestsScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun render(
        rows: List<TestOccurrence> = listOf(testOccurrence()),
        filter: TestsFilter = TestsFilter.Failed,
        counts: TestsCounts = TestsCounts(2, 2, 2),
        state: ListUiState<TestOccurrence> = ListUiState.Content(rows),
        count: TestsCountState = TestsCountState.Available(6),
        append: TestsAppendState = TestsAppendState.Idle,
        onFilter: (TestsFilter) -> Unit = {},
        onTest: (String) -> Unit = {},
        onRetry: () -> Unit = {},
        onCountRetry: () -> Unit = {},
        onAppendRetry: () -> Unit = onRetry
    ) {
        compose.setContent {
            TeamCityTheme {
                TestsScreen(state, TestsPresentation(filter, counts), count, rows.size, { TestRowUiState(rows[it]) }, { TestRowUiState(rows[it]) }, append, onFilter, {}, onRetry, onCountRetry, onTest, onAppendRetry)
            }
        }
    }

    @Test fun onlyFailedOccurrencesOpenDetails() {
        val opened = mutableListOf<String>()
        val rows = TestStatus.entries.mapIndexed { index, status -> testOccurrence("$index", status) }
        render(rows, onTest = { opened += it })
        compose.onNodeWithTag("tests:test:1").performClick()
        assertEquals(listOf("/testOccurrences/1"), opened)
        compose.onNodeWithTag("tests:test:0").assertHasNoClickAction()
        compose.onNodeWithTag("tests:test:2").assertHasNoClickAction()
        compose.onNodeWithTag("tests:test:3").assertHasNoClickAction()
    }

    @Test fun allFiltersRemainVisibleRegardlessOfCounts() {
        var selected: TestsFilter? = null
        render(counts = TestsCounts(3, 0, 0), onFilter = { selected = it })
        compose.onNodeWithTag("tests:filter:Failed").assertIsSelected()
        compose.onNodeWithTag("tests:filter:Ignored").assertIsDisplayed()
        compose.onNodeWithTag("tests:filter:Passed").performClick()
        assertEquals(TestsFilter.Passed, selected)
    }

    @Test fun zeroCountsKeepStableFilterControls() {
        render(counts = TestsCounts(0, 0, 0), state = ListUiState.Empty())
        TestsFilter.entries.forEach { compose.onNodeWithTag("tests:filter:${it.name}").assertIsDisplayed() }
        compose.onNodeWithText("There are no failed tests").assertIsDisplayed()
    }

    @Test fun selectionKeepsChipBoundsAndSingleChoiceSemanticsStable() {
        val filter = mutableStateOf(TestsFilter.Failed)
        compose.setContent {
            TeamCityTheme {
                TestsScreen(
                    ListUiState.Empty(), TestsPresentation(filter.value, TestsCounts(3, 0, 0)), TestsCountState.Available(3),
                    0, { null }, { null }, TestsAppendState.Idle, { filter.value = it }, {}, {}, {}, {}
                )
            }
        }
        val nodes = TestsFilter.entries.map { compose.onNodeWithTag("tests:filter:${it.name}") }
        val bounds = nodes.map { it.fetchSemanticsNode().boundsInRoot }
        compose.onNodeWithTag("tests:filter:Passed").performClick()
        compose.waitForIdle()
        assertEquals(bounds, nodes.map { it.fetchSemanticsNode().boundsInRoot })
        compose.onNodeWithTag("tests:filter:Passed").assertIsSelected()
        compose.onNodeWithTag("tests:filter:Failed").assertIsNotSelected()
        compose.onNodeWithTag("tests:filter:Ignored").assertIsNotSelected()
    }

    @Test fun clickingSelectedFilterDoesNotReload() {
        var selections = 0
        render(onFilter = { selections++ })
        compose.onNodeWithTag("tests:filter:Failed").performClick()
        assertEquals(0, selections)
    }

    @Test fun emptyMessageMatchesPassedFilter() {
        render(filter = TestsFilter.Passed, state = ListUiState.Empty())
        compose.onNodeWithText("There are no passed tests").assertIsDisplayed()
    }

    @Test fun contiguousStatusesShareAHeaderAcrossPageBoundaries() {
        val rows = listOf(testOccurrence("1"), testOccurrence("2"), testOccurrence("3", TestStatus.Passed), testOccurrence("4", TestStatus.Passed), testOccurrence("5", TestStatus.Ignored), testOccurrence("6", TestStatus.Error))
        render(rows)
        compose.onAllNodesWithText("Failed (2)").assertCountEquals(1)
        compose.onAllNodesWithText("Passed (2)").assertCountEquals(1)
        compose.onAllNodesWithText("Ignored (2)").assertCountEquals(1)
    }

    @Test fun appendFailureKeepsRowsAndRetryDelegates() {
        var retries = 0
        var refreshRetries = 0
        render(append = TestsAppendState.Error, onRetry = { refreshRetries++ }, onAppendRetry = { retries++ })
        compose.onNodeWithTag("tests:test:1").assertIsDisplayed()
        compose.onNodeWithText("Retry").performScrollTo().performClick()
        assertEquals(1, retries)
        assertEquals(0, refreshRetries)
    }

    @Test fun countRetryIsSeparateFromListRetry() {
        var retries = 0
        var countRetries = 0
        render(count = TestsCountState.Unavailable, onRetry = { retries++ }, onCountRetry = { countRetries++ })
        compose.onNode(hasText("Retry") and hasAnyAncestor(hasTestTag("retry:snackbar"))).performClick()
        assertEquals(1, countRetries)
        assertEquals(0, retries)
    }
}
