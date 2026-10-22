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

package teamcityapp.libraries.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

/** A failure gets one dismissible retry snackbar per request, without replay on recomposition. */
@Composable
fun TeamCityRetrySnackbar(
    message: String?,
    actionLabel: String,
    snackbar: SnackbarHostState,
    requestKey: Any? = null,
    onRetry: () -> Unit
) {
    if (message == null) return
    val retry by rememberUpdatedState(onRetry)
    var retryAttempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(message, actionLabel, snackbar, requestKey, retryAttempt) {
        if (snackbar.showSnackbar(message, actionLabel, withDismissAction = true, duration = SnackbarDuration.Long) == SnackbarResult.ActionPerformed) {
            retry()
            // A fast failed request can conflate Loading and Unavailable into the same
            // observed state. Keep that failure retryable even without a state transition.
            retryAttempt++
        }
    }
}

/** Retry actions remain readable on narrow screens with enlarged text. */
@Composable
fun TeamCitySnackbarHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    if (state.currentSnackbarData == null) return
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val actionOnNewLine = LocalDensity.current.fontScale >= 1.3f
        SnackbarHost(state) { Snackbar(it, modifier = Modifier.testTag("retry:snackbar"), actionOnNewLine = actionOnNewLine) }
    }
}
