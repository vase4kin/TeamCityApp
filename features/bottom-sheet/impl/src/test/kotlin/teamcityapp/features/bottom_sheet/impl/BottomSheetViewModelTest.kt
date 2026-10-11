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
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.bottom_sheet.api.*
class BottomSheetViewModelTest {
    @Test fun everyMenuPreservesOrderingAndEachActionsDescription() {
        val actions = listOf(listOf(SheetAction.Copy), listOf(SheetAction.Copy, SheetAction.Branch), listOf(SheetAction.ArtifactDownload), listOf(SheetAction.ArtifactDownload, SheetAction.ArtifactBrowser), listOf(SheetAction.ArtifactOpen), listOf(SheetAction.ArtifactDownload, SheetAction.ArtifactOpen), listOf(SheetAction.Copy, SheetAction.BuildType), listOf(SheetAction.Copy, SheetAction.Project))
        SheetMenuType.entries.forEach { type ->
            val items = sheetItems(type, listOf("/download/file.jar", "/open/file.jar"))
            assertEquals(actions[type.ordinal], items.map { it.action })
            assertEquals("/download/file.jar", items.first().description)
            if (type == SheetMenuType.ArtifactFull || type == SheetMenuType.ArtifactBrowser) assertEquals("/open/file.jar", items.last().description)
        }
    }

    @Test fun fragmentArgumentsCreatePlainStateAndDoNotRetainAndroidObjects() {
        val vm = BottomSheetViewModel(SavedStateHandle(mapOf("arg_title" to "Build branch", "arg_bottom_sheet_type" to 1, "arg_description" to arrayOf("main"))))
        assertEquals("Build branch", vm.state.value.title)
        assertEquals(sheetItems(SheetMenuType.Branch, listOf("main")), vm.state.value.items)
        assertEquals(listOf(R.string.build_element_copy, R.string.build_element_show_all_builds_built_branch), vm.state.value.menuItems.map { it.labelRes })
    }

    @Test fun missingOrInvalidArgumentsHaveASafeDefault() {
        val vm = BottomSheetViewModel(SavedStateHandle(mapOf("arg_bottom_sheet_type" to -10)))
        assertEquals(listOf(SheetItem(SheetAction.Copy, "")), vm.state.value.items)
        assertEquals(listOf(R.string.build_element_copy), vm.state.value.menuItems.map { it.labelRes })
    }

    @Test fun artifactNamesMatchLegacyTrailingSlashBehavior() {
        assertEquals("file.jar", SheetItem(SheetAction.ArtifactDownload, "/path/file.jar/").fileName)
    }
}
