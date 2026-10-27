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

package teamcityapp.features.test_details.impl

import android.content.ClipData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import teamcityapp.features.test_details.impl.router.TestDetailsRouter

// Regression harness for explicit fake ViewModels; production uses shared-app routes.
@Composable
internal fun TestDetailsRoute(router: TestDetailsRouter, viewModel: TestDetailsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.onScreenViewed() }
    LaunchedEffect(state) {
        if (state == TestDetailsUiState.InvalidInput) router.closeInvalidInput()
    }
    TestDetailsReaderRoute(state, viewModel::retry, router::close)
}

/** UI-only reader choices belong to the saved destination composition. */
@Composable
internal fun TestDetailsReaderRoute(state: TestDetailsUiState, onRetry: () -> Unit, onClose: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var wrapLines by rememberSaveable { mutableStateOf(true) }
    var activeMatch by rememberSaveable { mutableIntStateOf(0) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val label = stringResource(R.string.test_details_title)
    val copiedMessage = stringResource(R.string.test_details_copied)
    val snackbar = remember { SnackbarHostState() }
    TestDetailsScreen(
        state = state,
        onRetry = onRetry,
        onClose = onClose,
        query = query,
        wrapLines = wrapLines,
        activeMatch = activeMatch,
        onQueryChange = {
            query = it
            activeMatch = 0
        },
        onWrapLinesChange = { wrapLines = it },
        onActiveMatchChange = { activeMatch = it },
        onCopy = { details ->
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, details)))
                snackbar.showSnackbar(copiedMessage)
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    )
}
