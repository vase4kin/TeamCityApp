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

package teamcityapp.libraries.list_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.theme.MessageContent
import teamcityapp.libraries.theme.TeamCityTheme

/** The owning feature resolves the message from its resources and current selection. */
@Composable
fun TeamCityListEmpty(message: String, modifier: Modifier = Modifier) {
    MessageContent(message, modifier.fillMaxSize())
}

@Composable
fun TeamCityListSectionHeader(title: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val interaction = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Text(
        title,
        modifier.fillMaxWidth().heightIn(min = 48.dp).then(interaction).semantics { heading() }.padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

/** Skeletons are presentation only; they never enter the feature's domain item collection. */
@Composable
fun TeamCityListLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.list_loading)
    Column(modifier.fillMaxSize().semantics { stateDescription = description }) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        repeat(6) { TeamCityListLoadingRow() }
    }
}

@Composable
fun TeamCityListLoadingRow(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerHighest))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.fillMaxWidth(.65f).height(16.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainerHighest))
            Spacer(Modifier.fillMaxWidth(.4f).height(12.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainer))
        }
    }
}

/** A feature can place this footer in its lazy list while a page is being appended. */
@Composable
fun TeamCityListAppendLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.list_loading_more)
    Box(modifier.fillMaxWidth().padding(24.dp).semantics { stateDescription = description }, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(32.dp))
    }
}

@Composable
fun TeamCityListAppendRetry(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.list_append_failed), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text(stringResource(R.string.list_action_retry)) }
    }
}

@Preview
@Composable
private fun ListComponentsPreview() {
    TeamCityTheme {
        Surface {
            Column {
                TeamCityListSectionHeader("Project")
                TeamCityListLoadingRow()
                TeamCityListAppendLoading()
                TeamCityListAppendRetry({})
            }
        }
    }
}
