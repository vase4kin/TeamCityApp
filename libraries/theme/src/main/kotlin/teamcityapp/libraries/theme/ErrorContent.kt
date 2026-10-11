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
import androidx.compose.ui.unit.dp

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
            Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 44.dp, bottomEnd = 28.dp, bottomStart = 44.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_cloud_off_24dp), null, Modifier.size(36.dp))
                }
            }
            Text(
                title ?: stringResource(R.string.error_load_title),
                Modifier.padding(top = 24.dp).semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                message ?: stringResource(R.string.error_load_message),
                Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry, modifier = Modifier.padding(top = 24.dp).heightIn(min = 56.dp)) {
                if (actionLabel == null) {
                    Icon(painterResource(R.drawable.ic_refresh_24dp), null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
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
            val stacked = maxWidth < 324.dp || LocalDensity.current.fontScale >= 1.3f
            Box(scroll.padding(12.dp)) {
                if (stacked) {
                    Column {
                        ErrorNoticeMessage(message, Modifier.fillMaxWidth())
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { ErrorNoticeAction(onRetry, enabled, actionLabel) }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(painterResource(R.drawable.ic_error_outline_24dp), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(message, Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorNoticeAction(onRetry: (() -> Unit)?, enabled: Boolean, actionLabel: String?) {
    if (onRetry != null) {
        TextButton(onClick = onRetry, enabled = enabled, modifier = Modifier.widthIn(max = 200.dp).heightIn(min = 48.dp)) {
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
