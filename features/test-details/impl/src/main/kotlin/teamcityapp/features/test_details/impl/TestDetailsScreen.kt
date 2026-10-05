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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.libraries.theme.*

@Composable
fun TestDetailsScreen(state: TestDetailsUiState, onRetry: () -> Unit, onClose: () -> Unit) {
    TeamCityScreen(title = stringResource(R.string.test_details_title), onClose = onClose) { modifier ->
        when (state) {
            TestDetailsUiState.Loading -> LoadingContent(modifier)
            TestDetailsUiState.Empty -> Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.text_empty_test_details),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            TestDetailsUiState.Error -> ErrorContent(modifier, onRetry)
            TestDetailsUiState.InvalidInput -> Unit
            is TestDetailsUiState.Content -> SelectionContainer(modifier) {
                Text(
                    text = remember(state.details) { formatTestDetails(state.details) },
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxSize().testTag("test_details:text")
                        .verticalScroll(rememberScrollState()).padding(TeamCityDimensions.contentPadding),
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
private fun EmptyPreview() { TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Empty, {}, {}) } }

@Preview
@Composable
private fun ErrorPreview() { TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Error, {}, {}) } }
