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

package teamcityapp.features.build_log.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.build_log.api.BuildLogSession
import teamcityapp.libraries.theme.*

@Composable
fun BuildLogScreen(state: BuildLogUiState, onRetry: () -> Unit, onAuthenticate: () -> Unit, onBrowser: (String) -> Unit, webContent: @Composable (BuildLogUiState.Session, Modifier) -> Unit = { _, _ -> }) {
    Box(Modifier.fillMaxSize().testTag("build-log:screen")) {
        when (state) {
            BuildLogUiState.Loading -> LoadingContent(Modifier.fillMaxSize())

            BuildLogUiState.Error -> ErrorContent(Modifier.fillMaxSize(), onRetry)

            is BuildLogUiState.Session -> when {
                state.session.sslDisabled -> LogMessage(R.drawable.ic_warning_black_24dp, requireNotNull(state.messageRes), requireNotNull(state.actionLabelRes), { onBrowser(state.session.url) }, "build-log:browser")

                state.authenticationFailed -> ErrorContent(Modifier.fillMaxSize(), onAuthenticate, message = stringResource(requireNotNull(state.messageRes)), actionLabel = stringResource(requireNotNull(state.actionLabelRes)))

                state.session.needsAuthentication -> LogMessage(R.drawable.ic_lock_24dp, requireNotNull(state.messageRes), requireNotNull(state.actionLabelRes), onAuthenticate, "build-log:authenticate", !state.acknowledging)

                else -> {
                    webContent(state, Modifier.fillMaxSize().alpha(if (state.page == BuildLogPage.Content) 1f else 0f))
                    if (state.page == BuildLogPage.Loading) LoadingContent(Modifier.fillMaxSize())
                    if (state.page == BuildLogPage.Error) ErrorContent(Modifier.fillMaxSize(), onRetry)
                }
            }
        }
    }
}

@Composable
private fun LogMessage(icon: Int, message: Int, action: Int, onClick: () -> Unit, tag: String, enabled: Boolean = true) {
    Box(Modifier.fillMaxSize().padding(TeamCityDimensions.contentPadding), contentAlignment = Alignment.Center) {
        Card(Modifier.widthIn(max = TeamCityDimensions.formMaxWidth), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(TeamCityDimensions.sectionSpacing), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.messageIconSize), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(message), Modifier.padding(top = if (tag == "build-log:browser") TeamCityDimensions.extraLargeSpacing else TeamCityDimensions.contentPadding, bottom = TeamCityDimensions.contentPadding), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = if (tag == "build-log:browser") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick, enabled = enabled, shape = MaterialTheme.shapes.large, modifier = Modifier.heightIn(min = TeamCityDimensions.controlMinHeight).testTag(tag)) { Text(stringResource(action)) }
            }
        }
    }
}

@Preview @Composable
private fun LogPreview() {
    TeamCityTheme { BuildLogScreen(BuildLogUiState.Session(BuildLogSession("https://server.example", sslDisabled = true)), {}, {}, {}) }
}
