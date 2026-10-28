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

package teamcityapp.features.changes.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.changes.api.ChangesRepository
import teamcityapp.features.changes.impl.resources.*
import teamcityapp.libraries.design_system.SharedUiText

@OptIn(ExperimentalCoroutinesApi::class)
open class ChangesViewModel constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ChangesRepository
) : ViewModel() {
    private val url: String = checkNotNull(savedStateHandle[ChangesViewModel.ARG_URL])
    private var forceRefresh = false

    /**
     * Paging retains pages and in-flight requests for this destination's ViewModel lifetime.
     * View recreation resubscribes to the same generation without duplicate page requests;
     * removing the destination clears the scope and cancels repository work.
     */
    val changes = Pager(
        config = PagingConfig(pageSize = 10, initialLoadSize = 10, prefetchDistance = 2, enablePlaceholders = false),
        pagingSourceFactory = { ChangesPagingSource(repository, url, forceRefresh) }
    ).flow.cachedIn(viewModelScope)

    val rows = changes.map { page -> page.map(::ChangeRowUiState) }

    private var completedCount: ChangesCountState? = null
    private val countRequest = MutableStateFlow(0)
    val count = countRequest.flatMapLatest {
        flow {
            completedCount?.let {
                emit(it)
                return@flow
            }
            emit(ChangesCountState.Loading)
            val result = try {
                ChangesCountState.Available(repository.count(url))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ChangesCountState.Unavailable
            }
            currentCoroutineContext().ensureActive()
            completedCount = result
            emit(result)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChangesCountState.Loading)

    /** Called before the UI requests a Paging refresh; app cache must be bypassed. */
    fun prepareRefresh() {
        forceRefresh = true
    }

    fun retryCount() {
        completedCount = null
        countRequest.value++
    }
    companion object {
        const val ARG_URL = "url"
    }
}

data class ChangeRowUiState(val change: ChangeDetails) {
    val filesBadge: SharedUiText = if (change.files.size > 9) SharedUiText.Resource(Res.string.changes_many_files) else SharedUiText.Dynamic(change.files.size.toString())
}
