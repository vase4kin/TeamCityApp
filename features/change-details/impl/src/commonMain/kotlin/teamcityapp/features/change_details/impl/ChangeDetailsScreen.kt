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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.impl.resources.*
import teamcityapp.libraries.theme.SharedTeamCityScreen
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityMonospace
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.rememberTeamCityLazyListState
import teamcityapp.libraries.theme.teamCityStatusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeDetailsScreen(
    state: ChangeDetailsUiState,
    onOpenUrl: (String) -> Unit,
    onOpenDiff: (String, String) -> Unit,
    onClose: () -> Unit,
    platformActionsAvailable: Boolean = true
) {
    SharedTeamCityScreen(
        title = stringResource(Res.string.title_activity),
        onClose = onClose
    ) { modifier ->
        val listState = rememberTeamCityLazyListState()
        if (state is ChangeDetailsUiState.Content) {
            val details = state.details
            LazyColumn(
                state = listState,
                modifier = modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).testTag("change_details:list"),
                contentPadding = PaddingValues(TeamCityDimensions.smallSpacing)
            ) {
                item("details") {
                    ChangeCard(details, { onOpenUrl(details.webUrl) }, Modifier.padding(TeamCityDimensions.smallSpacing), platformActionsAvailable)
                }
                item("files_header") { ChangedFilesHeader(details.files.size, showDiffHint = platformActionsAvailable) }
                // File names can occur more than once; their position preserves the source ordering.
                itemsIndexed(details.files) { index, file ->
                    ChangedFileRow(file, { onOpenDiff(details.id, file.name) }, Modifier.testTag("change_details:file:$index"), platformActionsAvailable)
                }
            }
        }
    }
}

@Composable
private fun ChangeCard(details: ChangeDetails, onOpenUrl: () -> Unit, modifier: Modifier = Modifier, platformActionsAvailable: Boolean = true) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(TeamCityDimensions.sectionSpacing)) {
            ChangeComment(details.id, details.comment)
            HorizontalDivider(Modifier.padding(top = TeamCityDimensions.smallSpacing, bottom = TeamCityDimensions.smallSpacing), color = MaterialTheme.colorScheme.outlineVariant)
            SelectionContainer { DetailField(stringResource(Res.string.text_revision), details.revision, TeamCityMonospace) }
            HorizontalDivider(Modifier.padding(top = TeamCityDimensions.smallSpacing, bottom = TeamCityDimensions.smallSpacing), color = MaterialTheme.colorScheme.outlineVariant)
            DetailField(stringResource(Res.string.text_user), stringResource(Res.string.text_description, details.userName, details.date), MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(TeamCityDimensions.smallSpacing))
            if (platformActionsAvailable) {
                Button(onClick = onOpenUrl) {
                    Icon(painterResource(Res.drawable.ic_web_black_24dp), null, Modifier.size(TeamCityDimensions.iconSize))
                    Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
                    Text(stringResource(Res.string.text_button_open_in_browser))
                }
            }
        }
    }
}

@Composable
private fun ChangeComment(changeId: String, comment: String) {
    var expanded by rememberSaveable(changeId, comment) { mutableStateOf(false) }
    var overflows by remember(changeId, comment) { mutableStateOf(false) }
    val expandLabel = stringResource(if (expanded) Res.string.text_show_less else Res.string.text_show_more)
    Column {
        DetailField(
            stringResource(Res.string.text_comment),
            comment,
            MaterialTheme.typography.headlineSmall,
            valueModifier = if (expanded || overflows) Modifier.clickable(role = Role.Button, onClickLabel = expandLabel) { expanded = !expanded } else Modifier,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow }
        )
        if (expanded || overflows) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(expandLabel)
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String, style: TextStyle, valueModifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE, onTextLayout: (TextLayoutResult) -> Unit = {}) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(TeamCityDimensions.extraSmallSpacing))
        Text(
            value,
            valueModifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            style = style,
            maxLines = maxLines,
            overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = onTextLayout
        )
    }
}

@Composable
private fun ChangedFilesHeader(count: Int, modifier: Modifier = Modifier, showDiffHint: Boolean = true) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing)
    ) {
        Text(stringResource(Res.string.title_changed_files, count.toString()), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
        if (count > 0 && showDiffHint) Text(stringResource(Res.string.text_info), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ChangedFileRow(file: ChangedFile, onClick: () -> Unit, modifier: Modifier = Modifier, platformActionsAvailable: Boolean = true) {
    val statuses = teamCityStatusColors()
    val status = when (file.type.lowercase()) {
        "added" -> statuses.success
        "removed", "deleted" -> teamcityapp.libraries.theme.TeamCityStatusColor(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        else -> statuses.info
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = TeamCityDimensions.controlMinHeight).then(if (platformActionsAvailable) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = TeamCityDimensions.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = MaterialTheme.shapes.small, color = status.container) {
            Text(
                file.type,
                Modifier
                    .padding(TeamCityDimensions.extraSmallSpacing)
                    .widthIn(min = TeamCityDimensions.minimumTouchTarget),
                color = status.onContainer,
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
