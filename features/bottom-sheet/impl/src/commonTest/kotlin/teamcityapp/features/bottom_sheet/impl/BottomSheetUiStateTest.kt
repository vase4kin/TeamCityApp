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
import kotlin.test.*
import kotlin.test.Test
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.impl.resources.*
class BottomSheetUiStateTest {
    @Test fun everyMenuPreservesOrderingAndEachActionsDescription() {
        val actions = listOf(listOf(SheetAction.Copy), listOf(SheetAction.Copy, SheetAction.Branch), listOf(SheetAction.ArtifactDownload), listOf(SheetAction.ArtifactDownload, SheetAction.ArtifactBrowser), listOf(SheetAction.ArtifactOpen), listOf(SheetAction.ArtifactDownload, SheetAction.ArtifactOpen), listOf(SheetAction.Copy, SheetAction.BuildType), listOf(SheetAction.Copy, SheetAction.Project))
        SheetMenuType.entries.forEach { type ->
            val items = sheetItems(type, listOf("/download/file.jar", "/open/file.jar"))
            assertEquals(actions[type.ordinal], items.map { it.action })
            assertEquals("/download/file.jar", items.first().description)
            if (type == SheetMenuType.ArtifactFull || type == SheetMenuType.ArtifactBrowser) assertEquals("/open/file.jar", items.last().description)
        }
    }

    @Test fun typedMenuCreatesPlainStateWithFeatureOwnedLabels() {
        val state = BottomSheetUiState("Build branch", sheetItems(SheetMenuType.Branch, listOf("main")))
        assertEquals("Build branch", state.title)
        assertEquals(sheetItems(SheetMenuType.Branch, listOf("main")), state.items)
        assertEquals(listOf(Res.string.build_element_copy, Res.string.build_element_show_all_builds_built_branch), state.menuItems.map { it.labelRes })
    }

    @Test fun emptyDescriptionsHaveASafeDefault() {
        val state = BottomSheetUiState(items = sheetItems(SheetMenuType.Default, emptyList()))
        assertEquals(listOf(SheetItem(SheetAction.Copy, "")), state.items)
        assertEquals(listOf(Res.string.build_element_copy), state.menuItems.map { it.labelRes })
    }

    @Test fun artifactNamesMatchLegacyTrailingSlashBehavior() {
        assertEquals("file.jar", SheetItem(SheetAction.ArtifactDownload, "/path/file.jar/").fileName)
    }
}
