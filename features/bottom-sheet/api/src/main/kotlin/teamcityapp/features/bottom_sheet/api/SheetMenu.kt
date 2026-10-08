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

package teamcityapp.features.bottom_sheet.api

enum class SheetAction { Copy, Branch, ArtifactOpen, ArtifactDownload, ArtifactBrowser, BuildType, Project }
enum class SheetMenuType { Default, Branch, ArtifactDefault, ArtifactBrowser, ArtifactFolder, ArtifactFull, BuildType, Project }
data class SheetItem(val action: SheetAction, val description: String) {
    val fileName: String get() = description.trimEnd('/').substringAfterLast('/')
}

/** Menu ordering and each action's source value are part of the existing caller contract. */
fun sheetItems(type: SheetMenuType, descriptions: List<String>): List<SheetItem> {
    val first = descriptions.firstOrNull().orEmpty()
    val second = descriptions.getOrNull(1) ?: first
    return when (type) {
        SheetMenuType.Default -> listOf(SheetItem(SheetAction.Copy, first))
        SheetMenuType.Branch -> listOf(SheetItem(SheetAction.Copy, first), SheetItem(SheetAction.Branch, first))
        SheetMenuType.BuildType -> listOf(SheetItem(SheetAction.Copy, first), SheetItem(SheetAction.BuildType, first))
        SheetMenuType.Project -> listOf(SheetItem(SheetAction.Copy, first), SheetItem(SheetAction.Project, first))
        SheetMenuType.ArtifactDefault -> listOf(SheetItem(SheetAction.ArtifactDownload, first))
        SheetMenuType.ArtifactFolder -> listOf(SheetItem(SheetAction.ArtifactOpen, first))
        SheetMenuType.ArtifactFull -> listOf(SheetItem(SheetAction.ArtifactDownload, first), SheetItem(SheetAction.ArtifactOpen, second))
        SheetMenuType.ArtifactBrowser -> listOf(SheetItem(SheetAction.ArtifactDownload, first), SheetItem(SheetAction.ArtifactBrowser, second))
    }
}
