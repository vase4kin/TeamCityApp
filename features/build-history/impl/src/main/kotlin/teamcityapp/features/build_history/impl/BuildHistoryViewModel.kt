/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.build_history.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import teamcityapp.features.build_history.api.*
import teamcityapp.features.build_history.impl.tracker.BuildHistoryTracker
import teamcityapp.libraries.builds.BuildLaunchData

internal data class HistorySelection(val query: BuildHistoryQuery, val pages: Flow<PagingData<BuildLaunchData>>)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BuildHistoryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: BuildHistoryRepository,
    private val onboardingRepository: BuildHistoryOnboardingRepository,
    private val tracker: BuildHistoryTracker
) : ViewModel() {
    val configurationName: String = savedStateHandle[BuildHistoryNavigation.EXTRA_NAME] ?: ""
    val configurationId: String = savedStateHandle[BuildHistoryNavigation.EXTRA_ID] ?: ""
    private var generation = newGeneration(BuildHistoryQuery(configurationId, savedStateHandle[EFFECTIVE_LOCATOR] ?: savedStateHandle[BuildHistoryNavigation.EXTRA_LOCATOR] ?: BuildHistoryQuery.DEFAULT_LOCATOR))
    private val selected = MutableStateFlow(generation.selection)
    internal val selection = selected.asStateFlow()

    private val favorite = MutableStateFlow<FavoriteState>(FavoriteState.Loading)
    private val favoriteRequest = MutableStateFlow(0)
    private var favoriteLoaded = false
    private val onboarding = MutableStateFlow<OnboardingState>(OnboardingState.Loading)
    private val onboardingRequest = MutableStateFlow(0)
    private var onboardingLoaded = false
    private var pendingPrompts = emptyList<BuildHistoryPrompt>()
    private val queuedBuild = MutableStateFlow<QueuedBuildState>(QueuedBuildState.Idle)
    private var queuedHref: String? = null
    private val notice = MutableStateFlow<BuildHistoryNotice?>(null)
    private var noticeId = 0L
    private val refreshPending = MutableStateFlow(false)
    private var favoriteJob: Job? = null
    private var promptJob: Job? = null
    private var queuedJob: Job? = null
    private var paused = false

    private val favoriteSource = favoriteRequest.flatMapLatest {
        flow {
            emit(favorite.value)
            if (!favoriteLoaded) {
                val loaded = try {
                    FavoriteState.Available(repository.isFavorite(configurationId))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    FavoriteState.Unavailable
                }
                currentCoroutineContext().ensureActive()
                favorite.value = loaded
                favoriteLoaded = true
            }
            emitAll(favorite)
        }
    }
    private val onboardingSource = onboardingRequest.flatMapLatest {
        flow {
            emit(onboarding.value)
            if (!onboardingLoaded) {
                val loaded = try {
                    val prompts = onboardingRepository.pendingPrompts().distinct().sortedBy { it.ordinal }
                    currentCoroutineContext().ensureActive()
                    pendingPrompts = prompts
                    OnboardingState.Available(prompts.firstOrNull())
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    OnboardingState.Unavailable
                }
                currentCoroutineContext().ensureActive()
                onboarding.value = loaded
                onboardingLoaded = true
            }
            emitAll(onboarding)
        }
    }
    val controls = combine(favoriteSource, onboardingSource, queuedBuild, notice, refreshPending) { fav, prompt, opening, message, refresh ->
        BuildHistoryControls(fav, prompt, opening, message, refresh)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildHistoryControls())

    internal fun prepareRefresh() {
        generation.forceRefresh = true
        if (notice.value?.kind == BuildHistoryNoticeKind.FiltersApplied) notice.value = null
    }
    internal fun claimRefresh(): Boolean = refreshPending.compareAndSet(true, false)

    fun applyFilter(locator: String) {
        replaceQuery(BuildHistoryQuery(configurationId, locator))
        showNotice(BuildHistoryNoticeKind.FiltersApplied)
    }
    fun resetFilters() {
        replaceQuery(BuildHistoryQuery(configurationId))
        notice.value = null
    }
    private fun replaceQuery(query: BuildHistoryQuery) {
        generation.scope.cancel()
        generation = newGeneration(query, force = true)
        savedStateHandle[EFFECTIVE_LOCATOR] = query.locator
        selected.value = generation.selection
        refreshPending.value = false
    }
    private fun newGeneration(query: BuildHistoryQuery, force: Boolean = false): Generation {
        // Paging deliberately retains completed pages and pending initial/append requests
        // across lifecycle collector gaps. The resumed UI receives cached results/errors;
        // it executes no navigation while hidden. Query replacement and owner disposal
        // cancel this child scope; flowWithLifecycle only gates downstream rendering.
        val scope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob(viewModelScope.coroutineContext[Job]))
        val generation = Generation(scope, force)
        generation.selection = HistorySelection(
            query,
            Pager(
                PagingConfig(pageSize = 10, initialLoadSize = 10, prefetchDistance = 2, enablePlaceholders = false),
                pagingSourceFactory = { BuildHistoryPagingSource(repository, query, generation.forceRefresh) }
            ).flow.cachedIn(scope)
        )
        return generation
    }

    fun retryFavorite() {
        if (favoriteJob?.isActive == true) return
        favoriteLoaded = false
        favorite.value = FavoriteState.Loading
        favoriteRequest.value++
    }
    fun toggleFavorite() {
        val current = favorite.value as? FavoriteState.Available ?: return
        if (favoriteJob?.isActive == true) return
        favoriteJob = viewModelScope.launch {
            favorite.value = current.copy(updating = true, updateFailed = false)
            try {
                repository.setFavorite(configurationId, !current.favorite)
                currentCoroutineContext().ensureActive()
                favorite.value = FavoriteState.Available(!current.favorite)
                showNotice(if (current.favorite) BuildHistoryNoticeKind.FavoriteRemoved else BuildHistoryNoticeKind.FavoriteAdded)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                favorite.value = current.copy(updateFailed = true)
            }
        }
    }
    fun retryOnboarding() {
        if (promptJob?.isActive == true) return
        onboardingLoaded = false
        onboarding.value = OnboardingState.Loading
        onboardingRequest.value++
    }
    fun dismissPrompt() {
        val current = onboarding.value as? OnboardingState.Available ?: return
        val prompt = current.prompt ?: return
        if (promptJob?.isActive == true) return
        promptJob = viewModelScope.launch {
            onboarding.value = current.copy(saving = true, saveFailed = false)
            try {
                onboardingRepository.markShown(prompt)
                currentCoroutineContext().ensureActive()
                pendingPrompts = pendingPrompts.filterNot { it == prompt }
                onboarding.value = OnboardingState.Available(pendingPrompts.firstOrNull())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                onboarding.value = current.copy(saveFailed = true)
            }
        }
    }
    fun onRunBuildClicked() = tracker.runBuildPressed()
    fun onResumed() {
        if (paused) {
            // Global prompts and account favorites can change in another screen. Recheck
            // optional controls without discarding the current Paging generation or rows.
            if (favoriteJob?.isActive != true) {
                favoriteLoaded = false
                favoriteRequest.value++
            }
            if (promptJob?.isActive != true) {
                onboardingLoaded = false
                onboardingRequest.value++
            }
        }
        paused = false
        tracker.viewShown()
    }
    fun onPaused() {
        paused = true
    }
    fun onConfigurationRecreation() {
        paused = false
    }
    fun onQueuedBuildResult(href: String) {
        queuedJob?.cancel()
        queuedBuild.value = QueuedBuildState.Idle
        queuedHref = href
        savedStateHandle[QUEUED_HREF] = href
        showNotice(BuildHistoryNoticeKind.Queued)
        refreshPending.value = true
    }
    fun openQueuedBuild() {
        val href = queuedHref ?: savedStateHandle.get<String>(QUEUED_HREF) ?: return
        if (queuedJob?.isActive == true) return
        tracker.queuedBuildRequested()
        queuedJob = viewModelScope.launch {
            queuedBuild.value = QueuedBuildState.Loading
            queuedBuild.value = try {
                if (href.isBlank()) throw IllegalArgumentException("Queued build URL missing")
                val build = repository.queuedBuild(href)
                currentCoroutineContext().ensureActive()
                QueuedBuildState.Ready(build)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                QueuedBuildState.Failed
            }
        }
    }
    fun claimQueuedBuild(): BuildLaunchData? {
        val ready = queuedBuild.value as? QueuedBuildState.Ready ?: return null
        return if (queuedBuild.compareAndSet(ready, QueuedBuildState.Idle)) ready.build else null
    }
    fun dismissQueuedFailure() {
        queuedBuild.value = QueuedBuildState.Idle
    }
    fun dismissNotice(id: Long) {
        if (notice.value?.id == id) notice.value = null
    }
    private fun showNotice(kind: BuildHistoryNoticeKind) {
        notice.value = BuildHistoryNotice(++noticeId, kind)
    }

    private class Generation(val scope: CoroutineScope, var forceRefresh: Boolean) {
        lateinit var selection: HistorySelection
    }
    companion object {
        internal const val EFFECTIVE_LOCATOR = "effectiveBuildHistoryLocator"
        private const val QUEUED_HREF = "recentQueuedBuildHref"
    }
}
