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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.filter_bottom_sheet.api.*
import teamcityapp.features.filter_bottom_sheet.impl.resources.*

data class FilterBottomSheetUiState(val filter: QuickFilter = QuickFilter.RunningAll, val applying: Boolean = false, val applied: Boolean = false, val failed: Boolean = false) {
    val titleRes: StringResource = when (filter) {
        QuickFilter.RunningAll, QuickFilter.RunningFavorites -> Res.string.title_filter_running_builds
        QuickFilter.QueuedAll, QuickFilter.QueuedFavorites -> Res.string.title_filter_queued_builds
        else -> Res.string.title_filter_agents
    }

    val descriptionRes: StringResource = when (filter) {
        QuickFilter.RunningAll, QuickFilter.QueuedAll -> Res.string.text_show_favorites
        QuickFilter.RunningFavorites -> Res.string.text_show_running
        QuickFilter.QueuedFavorites -> Res.string.text_show_queued
        QuickFilter.AgentsConnected -> Res.string.text_show_disconnected
        QuickFilter.AgentsDisconnected -> Res.string.text_show_connected
    }

    val selectedRes: StringResource = when (filter) {
        QuickFilter.RunningAll, QuickFilter.QueuedAll -> Res.string.selected_all
        QuickFilter.RunningFavorites, QuickFilter.QueuedFavorites -> Res.string.selected_favorites
        QuickFilter.AgentsConnected -> Res.string.selected_connected
        QuickFilter.AgentsDisconnected -> Res.string.selected_disconnected
    }
}

open class FilterBottomSheetViewModel constructor(private val repository: QuickFilterRepository, savedStateHandle: SavedStateHandle, private val tracker: QuickFilterTracker) : ViewModel() {
    private val mutableState = MutableStateFlow(FilterBottomSheetUiState(QuickFilter.entries.getOrElse(savedStateHandle.get<Int>("arg_code") ?: 0) { QuickFilter.RunningAll }))
    val state = mutableState.asStateFlow()
    private var completed = false
    fun apply() {
        if (completed || state.value.applying || state.value.applied) return
        val filter = state.value.filter.opposite()
        mutableState.update { it.copy(applying = true, failed = false) }
        viewModelScope.launch {
            try {
                repository.apply(filter)
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                tracker.selected(filter)
                completed = true
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
