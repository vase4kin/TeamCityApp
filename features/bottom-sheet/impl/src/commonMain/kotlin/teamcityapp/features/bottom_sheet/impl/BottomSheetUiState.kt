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

import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.impl.resources.*

data class BottomSheetUiState(val title: String = "", val items: List<SheetItem> = emptyList()) {
    val menuItems: List<SheetMenuItemUiState> = items.map { item ->
        val label = when (item.action) {
            SheetAction.Copy -> Res.string.build_element_copy
            SheetAction.Branch -> Res.string.build_element_show_all_builds_built_branch
            SheetAction.BuildType -> Res.string.build_element_open_build_type
            SheetAction.Project -> Res.string.build_element_open_project
            SheetAction.ArtifactDownload -> Res.string.artifact_download
            SheetAction.ArtifactOpen -> Res.string.artifact_open
            SheetAction.ArtifactBrowser -> Res.string.artifact_open_in_browser
        }
        SheetMenuItemUiState(item, label)
    }
}

data class SheetMenuItemUiState(val item: SheetItem, val labelRes: StringResource)
