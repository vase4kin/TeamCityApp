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

package teamcityapp.features.tests.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import teamcityapp.features.tests.api.*

/** A filter and its generation travel together so old rows cannot acquire a new filter label. */
internal data class TestsSelection(val filter: TestsFilter, val pages: Flow<PagingData<TestOccurrence>>)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TestsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TestsRepository
) : ViewModel() {
    private val url: String = checkNotNull(savedStateHandle[TestsFragment.ARG_URL])
    internal val counts = TestsCounts(
        passed = savedStateHandle[TestsFragment.ARG_PASSED] ?: 0,
        failed = savedStateHandle[TestsFragment.ARG_FAILED] ?: 0,
        ignored = savedStateHandle[TestsFragment.ARG_IGNORED] ?: 0
    )
    private var generation = newGeneration(
        savedStateHandle.get<String>(ARG_FILTER)?.let { name -> TestsFilter.entries.find { it.name == name } } ?: TestsFilter.Failed
    )
    private val selected = MutableStateFlow(generation.selection)
    internal val selection = selected.asStateFlow()

    /** Switching a filter cancels its old page requests and starts again at the first page. */
    internal fun selectFilter(filter: TestsFilter) {
        if (filter == selection.value.filter || counts.count(filter) <= 0) return
        generation.scope.cancel()
        generation = newGeneration(filter)
        savedStateHandle[ARG_FILTER] = filter.name
        selected.value = generation.selection
    }

    internal fun prepareRefresh() {
        generation.forceRefresh = true
    }

    private fun newGeneration(filter: TestsFilter): Generation {
        val scope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob(viewModelScope.coroutineContext[Job]))
        val generation = Generation(scope)
        // cachedIn retains pages and pending loads across configuration gaps. Only a filter
        // replacement or clearing this ViewModel cancels the paging generation's scope.
        val pages = Pager(
            PagingConfig(pageSize = 10, initialLoadSize = 10, prefetchDistance = 2, enablePlaceholders = false),
            pagingSourceFactory = { TestsPagingSource(repository, url, filter, generation.forceRefresh) }
        ).flow.cachedIn(scope)
        generation.selection = TestsSelection(filter, pages)
        return generation
    }

    private class Generation(val scope: CoroutineScope) {
        var forceRefresh = false
        lateinit var selection: TestsSelection
    }

    private var completedCount: TestsCountState? = null
    private val countRequest = MutableStateFlow(0)
    internal val count = countRequest.flatMapLatest {
        flow {
            completedCount?.let {
                emit(it)
                return@flow
            }
            emit(TestsCountState.Loading)
            val result = try {
                TestsCountState.Available(repository.count(url))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                TestsCountState.Unavailable
            }
            currentCoroutineContext().ensureActive()
            completedCount = result
            emit(result)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TestsCountState.Loading)

    internal fun retryCount() {
        completedCount = null
        countRequest.value++
    }

    companion object {
        internal const val ARG_FILTER = "selectedTestsFilter"
    }
}
