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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.impl.tracker.ChangeDetailsTracker

@HiltViewModel
class ChangeDetailsViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val tracker: ChangeDetailsTracker
) : ViewModel() {
    // The source is a saved navigation snapshot, not a continuously observed repository.
    val state: StateFlow<ChangeDetailsUiState> = MutableStateFlow(readDetails(savedState)).asStateFlow()

    fun onScreenViewed() {
        tracker.trackView()
    }
    fun onMoreDetailsClicked() {
        tracker.trackMoreDetails()
    }
    fun onFileDiffClicked() {
        tracker.trackFileDiff()
    }
}

private fun readDetails(saved: SavedStateHandle): ChangeDetailsUiState {
    val id = saved.get<String>(ChangeDetailsArguments.ID) ?: return ChangeDetailsUiState.InvalidInput
    val names = saved.get<ArrayList<String>>(ChangeDetailsArguments.FILE_NAMES) ?: return ChangeDetailsUiState.InvalidInput
    val types = saved.get<ArrayList<String>>(ChangeDetailsArguments.FILE_TYPES) ?: return ChangeDetailsUiState.InvalidInput
    if (names.size != types.size) return ChangeDetailsUiState.InvalidInput
    fun text(key: String) = saved.get<String>(key) ?: ""
    return ChangeDetailsUiState.Content(
        ChangeDetails(
            id = id,
            comment = text(ChangeDetailsArguments.COMMENT).trim(),
            userName = text(ChangeDetailsArguments.USER),
            date = text(ChangeDetailsArguments.DATE),
            files = names.indices.map { ChangedFile(names[it], types[it]) },
            revision = text(ChangeDetailsArguments.REVISION),
            webUrl = text(ChangeDetailsArguments.WEB_URL)
        )
    )
}
