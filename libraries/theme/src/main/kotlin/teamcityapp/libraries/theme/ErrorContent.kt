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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

/** One full-screen failure presentation; the owner supplies the recovery action and specific copy. */
@Composable
fun ErrorContent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    title: String? = null,
    message: String? = null,
    actionLabel: String? = null
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = TeamCityDimensions.messageMaxWidth).fillMaxWidth().verticalScroll(rememberScrollState()).padding(TeamCityDimensions.sectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = TeamCityDimensions.largeCornerRadius, topEnd = TeamCityDimensions.errorIllustrationCornerRadius, bottomEnd = TeamCityDimensions.largeCornerRadius, bottomStart = TeamCityDimensions.errorIllustrationCornerRadius),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(Modifier.size(TeamCityDimensions.errorIllustrationSize), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_cloud_off_24dp), null, Modifier.size(TeamCityDimensions.errorIllustrationIconSize))
                }
            }
            Text(
                title ?: stringResource(R.string.error_load_title),
                Modifier.padding(top = TeamCityDimensions.sectionSpacing).semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                message ?: stringResource(R.string.error_load_message),
                Modifier.padding(top = TeamCityDimensions.smallSpacing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry, modifier = Modifier.padding(top = TeamCityDimensions.sectionSpacing).heightIn(min = TeamCityDimensions.controlMinHeight)) {
                if (actionLabel == null) {
                    Icon(painterResource(R.drawable.ic_refresh_24dp), null, Modifier.size(TeamCityDimensions.compactIconSize))
                    Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
                }
                Text(actionLabel ?: stringResource(R.string.action_retry), textAlign = TextAlign.Center)
            }
        }
    }
}

/** Optional failures stay beside retained content rather than replacing the whole screen. */
@Composable
fun ErrorNotice(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val scroll = if (constraints.hasBoundedHeight) Modifier.verticalScroll(rememberScrollState()) else Modifier
            val stacked = maxWidth < TeamCityDimensions.errorNoticeInlineMinWidth || LocalDensity.current.fontScale >= 1.3f
            Box(scroll.padding(TeamCityDimensions.mediumSpacing)) {
                if (stacked) {
                    Column {
                        ErrorNoticeMessage(message, Modifier.fillMaxWidth())
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { ErrorNoticeAction(onRetry, enabled, actionLabel) }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
                        ErrorNoticeMessage(message, Modifier.weight(1f))
                        ErrorNoticeAction(onRetry, enabled, actionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorNoticeMessage(message: String, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.mediumSpacing)) {
        Icon(painterResource(R.drawable.ic_error_outline_24dp), null, Modifier.size(TeamCityDimensions.iconSize), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(message, Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorNoticeAction(onRetry: (() -> Unit)?, enabled: Boolean, actionLabel: String?) {
    if (onRetry != null) {
        TextButton(onClick = onRetry, enabled = enabled, modifier = Modifier.widthIn(max = TeamCityDimensions.retryActionMaxWidth).heightIn(min = TeamCityDimensions.minimumTouchTarget)) {
            Text(actionLabel ?: stringResource(R.string.action_retry), textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    TeamCityTheme { ErrorContent(Modifier.fillMaxSize(), {}) }
}

@Preview
@Composable
private fun ErrorNoticePreview() {
    TeamCityTheme { ErrorNotice("Couldn’t refresh. Showing previously loaded data.", {}) }
}
