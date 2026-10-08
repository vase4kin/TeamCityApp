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

package teamcityapp.features.filter_bottom_sheet.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.filter_bottom_sheet.api.*

data class FilterBottomSheetUiState(val filter: QuickFilter = QuickFilter.RunningAll, val applying: Boolean = false, val applied: Boolean = false, val failed: Boolean = false)

@HiltViewModel
class FilterBottomSheetViewModel @Inject constructor(private val repository: QuickFilterRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    private val mutableState = MutableStateFlow(FilterBottomSheetUiState(QuickFilter.entries.getOrElse(savedStateHandle.get<Int>("arg_code") ?: 0) { QuickFilter.RunningAll }))
    val state = mutableState.asStateFlow()
    fun apply() {
        if (state.value.applying || state.value.applied) return
        val filter = state.value.filter.opposite()
        mutableState.update { it.copy(applying = true, failed = false) }
        viewModelScope.launch {
            try {
                repository.apply(filter)
                mutableState.update { it.copy(applying = false, applied = true) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(applying = false, failed = true) }
            }
        }
    }
    fun consumeApplied(): Boolean {
        if (!state.value.applied) return false
        mutableState.update { it.copy(applied = false) }
        return true
    }
}
