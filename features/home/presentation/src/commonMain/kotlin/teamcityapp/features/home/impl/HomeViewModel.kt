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

package teamcityapp.features.home.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterRepository
import teamcityapp.features.home.api.*
import teamcityapp.features.home.impl.tracker.HomeTracker
import teamcityapp.features.home.presentation.resources.*

@OptIn(ExperimentalCoroutinesApi::class)
open class HomeViewModel constructor(
    private val savedState: SavedStateHandle,
    private val repository: HomeRepository,
    private val tracker: HomeTracker,
    private val filters: QuickFilterRepository
) : ViewModel() {
    private val selected = savedState.getStateFlow(SELECTED_TAB, HomeTab.Projects.ordinal)
    init {
        HomeTab.entries.getOrNull(selected.value)?.takeIf { it in ACTIVITY_TABS }?.let {
            savedState[SELECTED_ACTIVITY_TAB] = it.ordinal
        }
    }
    private val refresh = MutableStateFlow(0L)
    private val prompts = MutableStateFlow<Set<HomePrompt>>(emptySet())
    private val onboardingFailed = MutableStateFlow(false)
    private data class FilterState(val accountKey: String? = null, val applying: QuickFilter? = null, val failed: Boolean = false)
    private val filterState = MutableStateFlow(FilterState())
    private var filterJob: Job? = null
    private val queries = repository.query.shareIn(viewModelScope, SharingStarted.WhileSubscribed(0), replay = 1)
    private data class CountKey(val accountKey: String, val filter: QuickFilter, val favoriteIds: List<String>)
    private data class CountRequest(val query: HomeQuery, val key: CountKey, val attempt: Long)
    private data class CountState(val key: CountKey, val badge: HomeBadge)
    private val completedCounts = mutableMapOf<HomeTab, CountState>()
    private fun HomeQuery.countKey(tab: HomeTab): CountKey {
        val filter = when (tab) {
            HomeTab.Running -> runningFilter
            HomeTab.Queue -> queueFilter
            else -> agentsFilter
        }
        val favorites = if (filter == QuickFilter.RunningFavorites || filter == QuickFilter.QueuedFavorites) favoriteIds.toList() else emptyList()
        return CountKey(accountKey, filter, favorites)
    }
    private fun count(tab: HomeTab): Flow<CountState> = combine(queries, refresh) { query, attempt ->
        CountRequest(query, query.countKey(tab), attempt)
    }.distinctUntilChanged { old, new -> old.key == new.key && old.attempt == new.attempt }.flatMapLatest { request ->
        flow {
            emit(completedCounts[tab]?.takeIf { it.key == request.key } ?: CountState(request.key, HomeBadge.Loading))
            val result = CountState(request.key, repository.count(request.query, tab).ui())
            completedCounts[tab] = result
            emit(result)
        }.catch { error ->
            if (error is CancellationException) throw error
            val result = CountState(request.key, HomeBadge.Failed())
            completedCounts[tab] = result
            emit(result)
        }
    }
    private val counts = combine(count(HomeTab.Running), count(HomeTab.Queue), count(HomeTab.Agents)) { running, queue, agents ->
        mapOf(HomeTab.Running to running, HomeTab.Queue to queue, HomeTab.Agents to agents)
    }
    private val data = combine(queries, counts) { query, counts ->
        HomeUiState(
            query = query,
            badges = counts.mapValues { (tab, count) ->
                count.badge.takeIf { count.key == query.countKey(tab) } ?: HomeBadge.Loading
            } + (HomeTab.Favorites to HomeBadge.Count(query.favoriteIds.size))
        )
    }
    private val promptState = refresh.flatMapLatest {
        flow {
            try {
                prompts.value = repository.pendingPrompts()
                onboardingFailed.value = false
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                onboardingFailed.value = true
            }
            emitAll(prompts)
        }
    }
    val state = combine(data, selected, promptState, onboardingFailed, refresh) { data, selected, prompts, failed, attempt ->
        val tab = HomeTab.entries.getOrElse(selected) { HomeTab.Projects }
        val prompt = if (HomePrompt.Drawer in prompts) HomePrompt.Drawer else HomePrompt.entries.firstOrNull { it.name == tab.name && it in prompts }
        data.copy(selected = tab, prompt = prompt?.ui(), onboardingFailed = failed, retryAttempt = attempt)
    }.combine(filterState) { state, filter ->
        if (filter.accountKey == state.query?.accountKey) state.copy(applyingFilter = filter.applying, filterFailed = filter.failed) else state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), HomeUiState(selected = HomeTab.entries.getOrElse(selected.value) { HomeTab.Projects }))

    fun selectFilter(filter: QuickFilter) {
        val current = state.value
        val query = current.query ?: return
        val option = current.filterOptions.firstOrNull { it.filter == filter } ?: return
        if (option.selected || (filterState.value.accountKey == query.accountKey && filterState.value.applying != null)) return
        filterJob?.cancel()
        filterState.value = FilterState(query.accountKey, applying = filter)
        tracker.fab(current.selected)
        filterJob = viewModelScope.launch {
            try {
                val latest = repository.query.first()
                if (latest.accountKey != query.accountKey) return@launch
                if (HomeUiState(selected = current.selected, query = latest).filterOptions.any { it.filter == filter && it.selected }) return@launch
                coroutineScope {
                    val scope = this
                    val accountGuard = launch(start = CoroutineStart.UNDISPATCHED) {
                        repository.query.first { it.accountKey != query.accountKey }
                        scope.cancel()
                    }
                    try {
                        ensureActive()
                        filters.apply(filter)
                    } finally {
                        accountGuard.cancel()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                filterState.value = FilterState(query.accountKey, failed = true)
            } finally {
                filterState.update { if (it.accountKey == query.accountKey) it.copy(applying = null) else it }
            }
        }
    }

    fun select(tab: HomeTab) {
        tracker.selected(tab)
        savedState[SELECTED_TAB] = tab.ordinal
        if (tab in ACTIVITY_TABS) savedState[SELECTED_ACTIVITY_TAB] = tab.ordinal
    }
    fun selectActivity() {
        val current = HomeTab.entries.getOrNull(selected.value)?.takeIf { it in ACTIVITY_TABS }
        val retained = savedState.get<Int>(SELECTED_ACTIVITY_TAB)?.let { HomeTab.entries.getOrNull(it) }?.takeIf { it in ACTIVITY_TABS }
        select(current ?: retained ?: HomeTab.Running)
    }
    fun retry() {
        refresh.value++
    }
    fun viewed() = tracker.viewed()
    fun fabClicked() = tracker.fab(state.value.selected)
    fun addProjects() {
        tracker.addProjects()
        select(HomeTab.Projects)
    }
    fun dismissPrompt() {
        val prompt = state.value.prompt?.prompt ?: return
        if (prompt !in prompts.value) return
        prompts.value = prompts.value - prompt
        viewModelScope.launch {
            try {
                repository.acknowledgePrompt(prompt)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                onboardingFailed.value = true
            }
        }
    }
    private fun HomeCount.ui(): HomeBadge = when (this) {
        is HomeCount.Available -> HomeBadge.Count(value)
        HomeCount.Unavailable -> HomeBadge.Failed()
    }
    companion object {
        const val SELECTED_TAB = "home.tab"
        const val SELECTED_ACTIVITY_TAB = "home.activity.tab"
        private val ACTIVITY_TABS = setOf(HomeTab.Running, HomeTab.Queue, HomeTab.Agents)
    }
}
