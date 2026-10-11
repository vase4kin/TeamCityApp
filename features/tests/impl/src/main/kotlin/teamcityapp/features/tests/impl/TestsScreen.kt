/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2020 Andrey Tolpeev
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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.tests.api.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.resolve

internal enum class TestsAppendState { Idle, Loading, Error }

/** Stateless content; only lazy itemAt access participates in Paging prefetch. */
@Composable
internal fun TestsScreen(
    state: ListUiState<TestOccurrence>,
    presentation: TestsPresentation,
    countState: TestsCountState,
    itemCount: Int,
    itemAt: (Int) -> TestRowUiState?,
    itemPeek: (Int) -> TestRowUiState?,
    appendState: TestsAppendState,
    onFilter: (TestsFilter) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onCountRetry: () -> Unit,
    onFailedTest: (String) -> Unit,
    onAppendRetry: () -> Unit = onRetry,
    modifier: Modifier = Modifier
) {
    Surface(modifier.fillMaxSize()) {
        Column {
            if (presentation.showFilters) {
                FlowRow(
                    Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.extraSmallSpacing),
                    horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)
                ) {
                    presentation.filterOptions.forEach { option ->
                        FilterChip(
                            selected = option.filter == presentation.filter,
                            onClick = { onFilter(option.filter) },
                            label = { Text(stringResource(option.labelRes)) },
                            modifier = Modifier.testTag("tests:filter:${option.filter.name}")
                        )
                    }
                }
            }
            if (countState == TestsCountState.Unavailable) {
                ErrorNotice(stringResource(R.string.tests_count_unavailable), onCountRetry, Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing), actionLabel = stringResource(R.string.tests_retry_count))
            }
            TeamCityListContainer(
                state,
                onRefresh,
                onRetry,
                Modifier.weight(1f).fillMaxWidth(),
                empty = { TeamCityListEmpty(stringResource(presentation.emptyMessageRes)) }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(
                        Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("tests:list"),
                        contentPadding = PaddingValues(vertical = TeamCityDimensions.smallSpacing)
                    ) {
                        items(itemCount, key = { itemPeek(it)?.test?.id ?: "tests:placeholder:$it" }, contentType = { "test" }) { index ->
                            val test = itemAt(index)
                            if (test == null) {
                                TeamCityListLoadingRow()
                            } else {
                                // Adjacent pages can belong to the same status section. peek
                                // examines their boundary without requesting additional pages.
                                if (index == 0 || itemPeek(index - 1)?.section != test.section) {
                                    TeamCityListSectionHeader(presentation.sectionTitles.getValue(test.section).resolve())
                                }
                                val previousInSection = index > 0 && itemPeek(index - 1)?.section == test.section
                                val nextInSection = index + 1 < itemCount && itemPeek(index + 1)?.section == test.section
                                TestRow(test, { onFailedTest(test.test.href) }, position = listRowPosition(previousInSection, nextInSection))
                            }
                        }
                        when (appendState) {
                            TestsAppendState.Loading -> item(key = "tests:append-loading") { TeamCityListAppendLoading() }
                            TestsAppendState.Error -> item(key = "tests:append-error") { TeamCityListAppendRetry(onAppendRetry) }
                            TestsAppendState.Idle -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TestRow(row: TestRowUiState, onClick: () -> Unit, modifier: Modifier = Modifier, position: ListRowPosition = ListRowPosition.Single) {
    val test = row.test
    val statusLabel = stringResource(row.statusLabelRes)
    val icon = when (test.status) {
        TestStatus.Failed -> R.drawable.ic_error_black_24dp
        TestStatus.Passed -> R.drawable.ic_check_circle_black_24dp
        TestStatus.Ignored -> R.drawable.ic_help_black_24dp
        TestStatus.Error -> R.drawable.ic_report_problem_black_24dp
    }
    val isError = test.status == TestStatus.Failed || test.status == TestStatus.Error
    TeamCityListRow(
        onClick = if (test.status == TestStatus.Failed) onClick else null,
        modifier = modifier.testTag("tests:test:${test.id}"),
        position = position,
        leadingContent = {
            TeamCityListLeadingIcon(
                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.iconSize))
            }
        }
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Text(test.name, style = MaterialTheme.typography.titleMedium)
            Text(statusLabel, style = MaterialTheme.typography.labelMedium, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun TestRowPreview() {
    TeamCityTheme { TestRow(TestRowUiState(TestOccurrence("1", "BuildQueueTest.processesNextBuild", TestStatus.Failed, "/test/1")), {}) }
}
