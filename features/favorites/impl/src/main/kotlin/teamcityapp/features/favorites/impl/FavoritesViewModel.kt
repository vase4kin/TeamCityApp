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

package teamcityapp.features.favorites.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import teamcityapp.features.favorites.api.FavoriteConfigurations
import teamcityapp.features.favorites.api.FavoritesRepository
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_state.RefreshableListLoader

@HiltViewModel
class FavoritesViewModel @Inject constructor(repository: FavoritesRepository) : ViewModel() {
    private var lastGood: FavoriteConfigurations? = null

    // Each envelope carries its IDs, partial failures and retained rows atomically.
    private val loader = RefreshableListLoader(flowOf(Unit)) { _, force ->
        val result = repository.favorites(force)
        currentCoroutineContext().ensureActive()
        val snapshot = result.copy(
            savedIds = result.savedIds.toList(),
            configurations = result.configurations.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.project.id }),
            unavailableIds = result.unavailableIds.toList()
        )
        val envelope = if (snapshot.allFailed) {
            FavoritesBatch(snapshot, lastGood)
        } else {
            FavoritesBatch(snapshot).also { lastGood = snapshot }
        }
        listOf(envelope)
    }
    val state = loader.state.map { result ->
        when (result) {
            ListUiState.Loading -> FavoritesUiState()
            ListUiState.Error -> FavoritesUiState(ListUiState.Error, FavoritesFailure.AllFailed)
            is ListUiState.Empty -> FavoritesUiState(ListUiState.Empty(result.isRefreshing, result.refreshFailed))
            is ListUiState.Content -> result.toFavoritesState()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), FavoritesUiState())

    private var paused = false

    fun refresh() = loader.refresh()
    fun retry() = loader.retry()

    /** The first load is collection-driven; a real return allows a cache-eligible reload. */
    fun onResumed() {
        if (paused) loader.reload()
        paused = false
    }

    fun onPaused() {
        paused = true
    }

    fun onConfigurationRecreation(wasVisible: Boolean) {
        if (wasVisible) paused = false
    }
}

private data class FavoritesBatch(val result: FavoriteConfigurations, val retained: FavoriteConfigurations? = null)

private val FavoriteConfigurations.allFailed: Boolean
    get() = savedIds.isNotEmpty() && configurations.isEmpty()

private fun ListUiState.Content<FavoritesBatch>.toFavoritesState(): FavoritesUiState {
    val envelope = items.single()
    val result = envelope.result
    val displayed = if (result.allFailed) envelope.retained else result
    val failed = refreshFailed || (result.allFailed && !isRefreshing)
    val list = when {
        displayed == null -> if (isRefreshing) ListUiState.Loading else ListUiState.Error
        displayed.configurations.isEmpty() -> ListUiState.Empty(isRefreshing, failed)
        else -> ListUiState.Content(displayed.configurations, isRefreshing, failed)
    }
    val failure = when {
        failed -> FavoritesFailure.AllFailed
        displayed != null && displayed.unavailableIds.isNotEmpty() -> FavoritesFailure.Partial(displayed.unavailableIds)
        else -> FavoritesFailure.None
    }
    return FavoritesUiState(list, failure, result.savedIds)
}
