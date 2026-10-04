package teamcityapp.features.test_details.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.test_details.R
import teamcityapp.features.test_details.viewmodel.TestDetailsUiState
import teamcityapp.libraries.theme.LoadingContent
import teamcityapp.libraries.theme.MessageContent
import teamcityapp.libraries.theme.TeamCityScreen
import teamcityapp.libraries.theme.TeamCityTheme

@Composable
fun TestDetailsScreen(state: TestDetailsUiState, onRetry: () -> Unit, onClose: () -> Unit) {
    TeamCityScreen(stringResource(R.string.test_details_title), onClose) { modifier ->
        when (state) {
            TestDetailsUiState.Loading -> LoadingContent(modifier.testTag("test_details_loading"))
            TestDetailsUiState.Empty -> MessageContent(stringResource(R.string.text_empty_test_details), modifier)
            TestDetailsUiState.Error -> MessageContent(stringResource(R.string.error_view_error_text), modifier) {
                Button(onClick = onRetry) { Text(stringResource(R.string.error_view_retry_button_text)) }
            }
            TestDetailsUiState.InvalidInput -> Unit
            is TestDetailsUiState.Content -> SelectionContainer(modifier.verticalScroll(rememberScrollState())) {
                Text(state.text, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Preview
@Composable
private fun ContentPreview() {
    TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Content("Assertion failed\nExpected: success\nActual: failure"), {}, {}) }
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
