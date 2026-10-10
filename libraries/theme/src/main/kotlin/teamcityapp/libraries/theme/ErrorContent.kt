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
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.resources.R as SharedR

/** Shared Compose error state using the app's Material color roles and type scale. */
@Composable
fun ErrorContent(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Card(Modifier.widthIn(max = 480.dp).padding(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(tr.xip.errorview.R.drawable.error_view_cloud),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(SharedR.string.error_view_oops_message),
                    modifier = Modifier.padding(top = TeamCityDimensions.contentPadding),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(SharedR.string.error_view_error_text),
                    modifier = Modifier.padding(top = TeamCityDimensions.smallSpacing),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                FilledTonalButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = TeamCityDimensions.contentPadding)
                ) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    TeamCityTheme { ErrorContent(Modifier.fillMaxSize(), {}) }
}
