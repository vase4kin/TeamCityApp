/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.changes.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_ui.*
import teamcityapp.libraries.theme.TeamCityTheme

internal enum class ChangesAppendState { Idle, Loading, Error }

/** Item access stays lazy, so Paging can prefetch as rows become visible. */
@Composable
internal fun ChangesScreen(
    state: ListUiState<ChangeDetails>,
    countState: ChangesCountState,
    itemCount: Int,
    itemAt: (Int) -> ChangeDetails?,
    itemKey: (Int) -> Any,
    appendState: ChangesAppendState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onCountRetry: () -> Unit,
    onChange: (ChangeDetails) -> Unit,
    onAppendRetry: () -> Unit = onRetry,
    modifier: Modifier = Modifier
) {
    Surface(modifier.fillMaxSize()) {
        Column {
            if (countState == ChangesCountState.Unavailable) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(stringResource(R.string.changes_count_unavailable), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onCountRetry) { Text(stringResource(R.string.changes_retry_count)) }
                    }
                }
            }
            TeamCityListContainer(
                state,
                onRefresh,
                onRetry,
                Modifier.weight(1f).fillMaxWidth(),
                empty = { TeamCityListEmpty(stringResource(R.string.changes_empty)) }
            ) {
                LazyColumn(Modifier.fillMaxSize().testTag("changes:list"), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(itemCount, key = itemKey, contentType = { "change" }) { index ->
                        val change = itemAt(index)
                        if (change == null) TeamCityListLoadingRow() else ChangeRow(change, { onChange(change) })
                    }
                    when (appendState) {
                        ChangesAppendState.Loading -> item(key = "changes:append-loading") { TeamCityListAppendLoading() }
                        ChangesAppendState.Error -> item(key = "changes:append-error") { TeamCityListAppendRetry(onAppendRetry) }
                        ChangesAppendState.Idle -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeRow(change: ChangeDetails, onClick: () -> Unit) {
    ListItem(
        onClick = onClick,
        modifier = Modifier.testTag("changes:change:${change.id}"),
        leadingContent = {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(if (change.files.size > 9) stringResource(R.string.changes_many_files) else change.files.size.toString(), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.changes_author_date, change.userName, change.date))
                Text(change.revision, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(pluralStringResource(R.plurals.changes_files, change.files.size, change.files.size), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    ) { Text(change.comment.trim(), maxLines = 3, overflow = TextOverflow.Ellipsis) }
}

@Preview
@Composable
private fun ChangesPreview() {
    val items = listOf(ChangeDetails("42", "Keep the build queue responsive", "john-117", "30 Jul 16 00:36", listOf(ChangedFile("Build.kt", "edited")), "21312fsd1321", ""))
    TeamCityTheme {
        ChangesScreen(ListUiState.Content(items), ChangesCountState.Available(1), items.size, { items[it] }, { items[it].id }, ChangesAppendState.Idle, {}, {}, {}, {})
    }
}
