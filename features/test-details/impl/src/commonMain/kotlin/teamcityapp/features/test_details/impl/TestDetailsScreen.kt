/*
 * Copyright 2020 Andrey Tolpeev
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

/* Updated for Compose in 2026. */

package teamcityapp.features.test_details.impl

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.test_details.impl.resources.*
import teamcityapp.libraries.theme.*

@Composable
fun TestDetailsScreen(
    state: TestDetailsUiState,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    query: String = "",
    wrapLines: Boolean = true,
    activeMatch: Int = 0,
    onQueryChange: (String) -> Unit = {},
    onWrapLinesChange: (Boolean) -> Unit = {},
    onActiveMatchChange: (Int) -> Unit = {},
    onCopy: (String) -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    copyAvailable: Boolean = true
) {
    SharedTeamCityScreen(title = stringResource(Res.string.test_details_title), onClose = onClose, snackbarHost = snackbarHost) { modifier ->
        when (state) {
            TestDetailsUiState.Loading -> LoadingContent(modifier)

            TestDetailsUiState.Empty -> MessageContent(
                message = stringResource(Res.string.text_empty_test_details),
                modifier = modifier
            )

            TestDetailsUiState.Error -> SharedErrorContent(modifier, onRetry)

            TestDetailsUiState.InvalidInput -> Unit

            is TestDetailsUiState.Content -> TestDetailsOutput(
                state.details, query, wrapLines, activeMatch,
                onQueryChange, onWrapLinesChange, onActiveMatchChange, onCopy,
                modifier.padding(TeamCityDimensions.contentPadding).imePadding(), copyAvailable
            )
        }
    }
}

@Composable
private fun TestDetailsOutput(
    details: String,
    query: String,
    wrapLines: Boolean,
    activeMatch: Int,
    onQueryChange: (String) -> Unit,
    onWrapLinesChange: (Boolean) -> Unit,
    onActiveMatchChange: (Int) -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    copyAvailable: Boolean = true
) {
    val matches = remember(details, query) { findTestDetailsMatches(details, query) }
    val selected = if (matches.isEmpty()) 0 else activeMatch.mod(matches.size)
    val colors = MaterialTheme.colorScheme
    val highlighted = remember(details, matches, selected, colors) {
        buildAnnotatedString {
            append(details)
            matches.forEachIndexed { index, range ->
                addStyle(
                    if (index == selected) {
                        SpanStyle(background = colors.tertiary, color = colors.onTertiary)
                    } else {
                        SpanStyle(background = colors.tertiaryContainer, color = colors.onTertiaryContainer)
                    },
                    range.first,
                    range.last + 1
                )
            }
        }
    }
    val vertical = rememberScrollState()
    val horizontal = rememberScrollState()
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    LaunchedEffect(highlighted, selected, wrapLines, layout) {
        val textLayout = layout
        val range = matches.getOrNull(selected)
        if (textLayout?.layoutInput?.text == highlighted && range != null) {
            val bounds = textLayout.getBoundingBox(range.first)
            vertical.animateScrollTo(bounds.top.roundToInt())
            horizontal.animateScrollTo(if (wrapLines) 0 else bounds.left.roundToInt())
        } else if (wrapLines) {
            horizontal.scrollTo(0)
        }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (copyAvailable) TextButton(onClick = { onCopy(details) }) { Text(stringResource(Res.string.test_details_copy_all)) }
            Row(
                Modifier.heightIn(min = 48.dp).toggleable(wrapLines, role = Role.Checkbox, onValueChange = onWrapLinesChange).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = wrapLines, onCheckedChange = null)
                Text(stringResource(Res.string.test_details_wrap_lines), style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().testTag("test_details:search"),
            label = { Text(stringResource(Res.string.test_details_find)) },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            trailingIcon = if (query.isEmpty()) {
                null
            } else {
                { TextButton(onClick = { onQueryChange("") }) { Text(stringResource(Res.string.test_details_clear_search)) } }
            }
        )
        if (query.isNotEmpty()) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (matches.isEmpty()) {
                        stringResource(Res.string.test_details_no_matches)
                    } else {
                        stringResource(Res.string.test_details_match_position, selected + 1, matches.size)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically).semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                TextButton(enabled = matches.size > 1, onClick = { onActiveMatchChange((selected - 1).mod(matches.size)) }) {
                    Text(stringResource(Res.string.test_details_previous_match))
                }
                TextButton(enabled = matches.size > 1, onClick = { onActiveMatchChange((selected + 1).mod(matches.size)) }) {
                    Text(stringResource(Res.string.test_details_next_match))
                }
            }
        }
        Surface(Modifier.fillMaxWidth().weight(1f), color = colors.surfaceContainerLow, shape = MaterialTheme.shapes.large) {
            SelectionContainer(Modifier.fillMaxSize()) {
                Text(
                    text = highlighted,
                    color = colors.onSurface,
                    style = TeamCityMonospace,
                    softWrap = wrapLines,
                    onTextLayout = { layout = it },
                    modifier = Modifier.fillMaxSize().testTag("test_details:text")
                        .verticalScroll(vertical)
                        .then(if (wrapLines) Modifier else Modifier.horizontalScroll(horizontal))
                        .padding(TeamCityDimensions.contentPadding)
                )
            }
        }
    }
}

@Preview
@Composable
private fun DetailsPreview() {
    TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Content("java.lang.AssertionError: expected <true> but was <false>\n    at example.Test.test(Test.kt:42)"), {}, {}) }
}

@Preview
@Composable
private fun EmptyPreview() {
    TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Empty, {}, {}) }
}

@Preview
@Composable
private fun ErrorPreview() {
    TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Error, {}, {}) }
}
