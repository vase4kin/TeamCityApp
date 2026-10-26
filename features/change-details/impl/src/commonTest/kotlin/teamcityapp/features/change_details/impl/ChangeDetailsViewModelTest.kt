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

package teamcityapp.features.change_details.impl

import androidx.lifecycle.SavedStateHandle
import kotlin.test.*
import kotlin.test.Test
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.impl.resources.*
import teamcityapp.features.change_details.impl.tracker.ChangeDetailsTracker

class ChangeDetailsViewModelTest {
    private val events = mutableListOf<String>()
    private val tracker = object : ChangeDetailsTracker {
        override fun trackView() {
            events += "view"
        }
        override fun trackMoreDetails() {
            events += "details"
        }
        override fun trackFileDiff() {
            events += "diff"
        }
    }

    @Test fun screenAndActionEventsUseTheInjectedTracker() {
        val model = model(fixture)
        assertTrue(events.isEmpty())
        model.onScreenViewed()
        model.onMoreDetailsClicked()
        model.onFileDiffClicked()
        model.onScreenViewed()
        assertEquals(listOf("view", "details", "diff", "view"), events)
    }

    @Test fun missingArgumentsAreInvalid() {
        assertEquals(ChangeDetailsUiState.InvalidInput, ChangeDetailsViewModel(SavedStateHandle(), tracker).state.value)
    }

    @Test fun trimsCommentAndKeepsOtherFieldsAndFileOrder() {
        val original = fixture.copy(comment = "  line one\n  line two  ", revision = " abc ", files = listOf(ChangedFile("a", "changed"), ChangedFile("a", "removed")))
        assertEquals(ChangeDetailsUiState.Content(original.copy(comment = "line one\n  line two")), model(original).state.value)
    }

    @Test fun noFilesIsValidContent() {
        val data = fixture.copy(files = emptyList())
        assertEquals(ChangeDetailsUiState.Content(data), model(data).state.value)
    }

    @Test fun emptyValuesRemainValidContent() {
        val data = ChangeDetails("", "", "", "", listOf(ChangedFile("", "")), "", "")
        assertEquals(ChangeDetailsUiState.Content(data), model(data).state.value)
    }

    @Test fun incompleteFileArgumentsAreRejected() {
        val arguments = arguments(fixture).toMutableMap()
        arguments.remove(ChangeDetailsKeys.FILE_TYPES)
        assertEquals(ChangeDetailsUiState.InvalidInput, ChangeDetailsViewModel(SavedStateHandle(arguments), tracker).state.value)
        arguments[ChangeDetailsKeys.FILE_TYPES] = arrayListOf("changed")
        assertEquals(ChangeDetailsUiState.InvalidInput, ChangeDetailsViewModel(SavedStateHandle(arguments), tracker).state.value)
    }

    @Test fun snapshotsMutableArgumentLists() {
        val arguments = arguments(fixture)
        val model = ChangeDetailsViewModel(SavedStateHandle(arguments), tracker)
        (arguments[ChangeDetailsKeys.FILE_NAMES] as ArrayList<String>).clear()
        (arguments[ChangeDetailsKeys.FILE_TYPES] as ArrayList<String>)[0] = "changed later"
        assertEquals(ChangeDetailsUiState.Content(fixture), model.state.value)
    }

    @Test fun freshViewModelRestoresSameSavedNavigationData() {
        val first = model(fixture)
        val restored = model(fixture)
        assertEquals(first.state.value, restored.state.value)
    }
    private fun model(data: ChangeDetails) = ChangeDetailsViewModel(SavedStateHandle(arguments(data)), tracker)
    private fun arguments(data: ChangeDetails): Map<String, Any> = mapOf(
        ChangeDetailsKeys.ID to data.id,
        ChangeDetailsKeys.COMMENT to data.comment,
        ChangeDetailsKeys.USER to data.userName,
        ChangeDetailsKeys.DATE to data.date,
        ChangeDetailsKeys.FILE_NAMES to ArrayList(data.files.map { it.name }),
        ChangeDetailsKeys.FILE_TYPES to ArrayList(data.files.map { it.type }),
        ChangeDetailsKeys.REVISION to data.revision,
        ChangeDetailsKeys.WEB_URL to data.webUrl
    )
}
