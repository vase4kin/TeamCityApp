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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.home.api.*
import teamcityapp.features.home.impl.tracker.HomeTracker

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: HomeRepository,
    private val tracker: HomeTracker
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
    private var lastData: HomeUiState? = null
    private val data = combine(repository.query, refresh) { query, _ -> query }.flatMapLatest { query ->
        flow {
            emit(lastData?.takeIf { it.query == query } ?: HomeUiState(query = query))
            val counts = repository.counts(query)
            val state = HomeUiState(
                query = query,
                badges = mapOf(
                    HomeTab.Favorites to counts.favorites.ui(),
                    HomeTab.Running to counts.running.ui(),
                    HomeTab.Queue to counts.queue.ui(),
                    HomeTab.Agents to counts.agents.ui()
                )
            )
            lastData = state
            emit(state)
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(HomeUiState(query = query, badges = HomeTab.entries.filter { it != HomeTab.Projects }.associateWith { HomeBadge.Failed() }))
        }
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
    val state = combine(data, selected, promptState, onboardingFailed) { data, selected, prompts, failed ->
        val tab = HomeTab.entries.getOrElse(selected) { HomeTab.Projects }
        val prompt = if (HomePrompt.Drawer in prompts) HomePrompt.Drawer else HomePrompt.entries.firstOrNull { it.name == tab.name && it in prompts }
        data.copy(selected = tab, prompt = prompt?.ui(), onboardingFailed = failed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), HomeUiState(selected = HomeTab.entries.getOrElse(selected.value) { HomeTab.Projects }))

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
