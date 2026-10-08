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

package teamcityapp.features.filter_builds.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import teamcityapp.features.filter_builds.api.*

data class FilterBuildsUiState(val branches: List<String>? = null, val branchesFailed: Boolean = false, val filter: BuildFilter = BuildFilter())

@HiltViewModel
class FilterBuildsViewModel @Inject constructor(private val repository: FilterBuildsRepository, savedStateHandle: SavedStateHandle, private val tracker: FilterBuildsTracker) : ViewModel() {
    private val buildTypeId = savedStateHandle.get<String>("BuildTypeId").orEmpty()
    private val form = MutableStateFlow(BuildFilter())
    private var branches: Pair<List<String>, Boolean>? = null
    private val data = flow<Pair<List<String>?, Boolean>> {
        val result = branches ?: try {
            repository.branches(buildTypeId) to false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyList<String>() to true
        }
        branches = result
        emit(result)
    }.onStart { emit(branches ?: (null to false)) }
    val state = combine(form, data) { form, data -> FilterBuildsUiState(data.first, data.second, form) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), FilterBuildsUiState())
    fun update(value: BuildFilter) {
        form.value = value
    }
    fun viewed() = tracker.trackView()
    fun apply(): BuildFilter {
        tracker.trackUserFilteredBuilds()
        return form.value
    }
}
