/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.change_details.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityScreen
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeDetailsScreen(
    state: ChangeDetailsUiState,
    onOpenUrl: (String) -> Unit,
    onOpenDiff: (String, String) -> Unit,
    onClose: () -> Unit
) {
    TeamCityScreen(
        title = stringResource(R.string.title_activity),
        onClose = onClose,
        scrollToolbarWithContent = true
    ) { modifier ->
        if (state is ChangeDetailsUiState.Content) {
            val details = state.details
            LazyColumn(
                modifier.testTag("change_details:list")
            ) {
                item("details") {
                    ChangeCard(details, { onOpenUrl(details.webUrl) }, Modifier.padding(TeamCityDimensions.smallSpacing))
                }
                item("files_header") { ChangedFilesHeader(details.files.size) }
                // File names can occur more than once; their position preserves the source ordering.
                itemsIndexed(details.files) { index, file ->
                    ChangedFileRow(file, { onOpenDiff(details.id, file.name) }, Modifier.testTag("change_details:file:$index"))
                }
            }
        }
    }
}

@Composable
private fun ChangeCard(details: ChangeDetails, onOpenUrl: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small, shadowElevation = 1.dp) {
        Column(Modifier.padding(start = TeamCityDimensions.smallSpacing, end = TeamCityDimensions.smallSpacing, top = TeamCityDimensions.smallSpacing)) {
            DetailField(stringResource(R.string.text_comment), details.comment, MaterialTheme.typography.bodyLarge)
            HorizontalDivider(Modifier.padding(top = TeamCityDimensions.smallSpacing, bottom = TeamCityDimensions.smallSpacing), color = MaterialTheme.colorScheme.outlineVariant)
            DetailField(stringResource(R.string.text_revision), details.revision, MaterialTheme.typography.bodyMedium)
            HorizontalDivider(Modifier.padding(top = TeamCityDimensions.smallSpacing, bottom = TeamCityDimensions.smallSpacing), color = MaterialTheme.colorScheme.outlineVariant)
            DetailField(stringResource(R.string.text_user), stringResource(R.string.text_description, details.userName, details.date), MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(TeamCityDimensions.smallSpacing))
            OutlinedButton(onClick = onOpenUrl) {
                Icon(painterResource(R.drawable.ic_web_black_24dp), null, Modifier.size(TeamCityDimensions.iconSize))
                Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
                Text(stringResource(R.string.text_button_open_in_browser).uppercase(LocalLocale.current.platformLocale))
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String, style: TextStyle) {
    Text(label.uppercase(LocalLocale.current.platformLocale), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(TeamCityDimensions.extraSmallSpacing))
    Text(value, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface, style = style)
}

@Composable
private fun ChangedFilesHeader(count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing)
    ) {
        Text(stringResource(R.string.title_changed_files, count.toString()), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
        if (count > 0) Text(stringResource(R.string.text_info), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ChangedFileRow(file: ChangedFile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = TeamCityDimensions.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Text(
                file.type.uppercase(LocalLocale.current.platformLocale),
                Modifier
                    .padding(TeamCityDimensions.extraSmallSpacing)
                    .widthIn(min = TeamCityDimensions.minimumTouchTarget),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
        Text(
            file.name,
            Modifier
                .weight(1f)
                .padding(vertical = TeamCityDimensions.smallSpacing),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val previewDetails = ChangeDetails(
    "123",
    "Preserve the Compose screen appearance",
    "Developer",
    "01 Oct 2026",
    listOf(ChangedFile("app/src/main/kotlin/example/Screen.kt", "changed"), ChangedFile("README.md", "added")),
    "abc123",
    "https://teamcity.example/change/123"
)

@Preview
@Composable
private fun ScreenPreview() {
    TeamCityTheme { ChangeDetailsScreen(ChangeDetailsUiState.Content(previewDetails), {}, { _, _ -> }, {}) }
}

@Preview
@Composable
private fun CardPreview() {
    TeamCityTheme { ChangeCard(previewDetails, {}) }
}

@Preview
@Composable
private fun HeaderPreview() {
    TeamCityTheme { ChangedFilesHeader(2) }
}

@Preview
@Composable
private fun FilePreview() {
    TeamCityTheme { ChangedFileRow(previewDetails.files.first(), {}) }
}
