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

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import teamcityapp.features.bottom_sheet.api.*

data class BottomSheetUiState(val title: String = "", val items: List<SheetItem> = emptyList()) {
    val menuItems: List<SheetMenuItemUiState> = items.map { item ->
        val label = when (item.action) {
            SheetAction.Copy -> R.string.build_element_copy
            SheetAction.Branch -> R.string.build_element_show_all_builds_built_branch
            SheetAction.BuildType -> R.string.build_element_open_build_type
            SheetAction.Project -> R.string.build_element_open_project
            SheetAction.ArtifactDownload -> R.string.artifact_download
            SheetAction.ArtifactOpen -> R.string.artifact_open
            SheetAction.ArtifactBrowser -> R.string.artifact_open_in_browser
        }
        SheetMenuItemUiState(item, label)
    }
}

data class SheetMenuItemUiState(val item: SheetItem, @get:StringRes val labelRes: Int)

@HiltViewModel
class BottomSheetViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val type = SheetMenuType.entries.getOrElse(savedStateHandle.get<Int>("arg_bottom_sheet_type") ?: 0) { SheetMenuType.Default }
    val state = MutableStateFlow(BottomSheetUiState(savedStateHandle.get<String>("arg_title").orEmpty(), sheetItems(type, savedStateHandle.get<Array<String>>("arg_description")?.toList().orEmpty()))).asStateFlow()
}
