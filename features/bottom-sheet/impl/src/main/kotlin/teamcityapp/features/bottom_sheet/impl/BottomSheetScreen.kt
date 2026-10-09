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

package teamcityapp.features.bottom_sheet.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.libraries.theme.*

@Composable
fun BottomSheetScreen(state: BottomSheetUiState, onAction: (SheetItem) -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(Modifier.widthIn(max = TeamCityDimensions.paneMaxWidth).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).testTag("sheet:content")) {
                TeamCitySheetHeader(state.title)
                state.items.forEach { item ->
                    val label = when (item.action) {
                        SheetAction.Copy -> R.string.build_element_copy
                        SheetAction.Branch -> R.string.build_element_show_all_builds_built_branch
                        SheetAction.BuildType -> R.string.build_element_open_build_type
                        SheetAction.Project -> R.string.build_element_open_project
                        SheetAction.ArtifactDownload -> R.string.artifact_download
                        SheetAction.ArtifactOpen -> R.string.artifact_open
                        SheetAction.ArtifactBrowser -> R.string.artifact_open_in_browser
                    }
                    val icon = when (item.action) {
                        SheetAction.Copy -> R.drawable.ic_content_copy_black_24dp
                        SheetAction.Branch -> R.drawable.ic_list_black_24dp
                        SheetAction.ArtifactDownload -> R.drawable.ic_file_download_black_24dp
                        SheetAction.ArtifactBrowser -> R.drawable.ic_open_in_browser_black_24dp
                        else -> R.drawable.ic_open_in_new_black_24dp
                    }
                    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).clickable(role = androidx.compose.ui.semantics.Role.Button) { onAction(item) }.padding(horizontal = 32.dp, vertical = 4.dp).testTag("sheet:${item.action}"), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) { Icon(painterResource(icon), null, Modifier.padding(12.dp).size(24.dp)) }
                        Text(stringResource(label), Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Preview @Composable
private fun SheetPreview() {
    TeamCityTheme { BottomSheetScreen(BottomSheetUiState("Branch", sheetItems(SheetMenuType.Branch, listOf("main"))), {}) }
}
