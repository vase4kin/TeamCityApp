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

package teamcityapp.features.navigation.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.api.NavigationRatingRepository
import teamcityapp.features.navigation.api.NavigationRepository
import teamcityapp.features.navigation.impl.tracker.NavigationTracker
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.list_state.RefreshableListLoader

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NavigationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: NavigationRepository,
    private val ratingRepository: NavigationRatingRepository,
    private val tracker: NavigationTracker
) : ViewModel() {
    private val project = ProjectReference(
        savedStateHandle[NavigationNavigation.PROJECT_ID] ?: NavigationNavigation.ROOT_PROJECT_ID,
        savedStateHandle[NavigationNavigation.PROJECT_NAME] ?: ""
    )
    private val root: Boolean = savedStateHandle[ROOT_SCREEN] ?: (project.id == NavigationNavigation.ROOT_PROJECT_ID)
    private val rating = MutableStateFlow<RatingPromptState>(RatingPromptState.Hidden)
    private val openRating = MutableStateFlow(false)
    private var ratingHandled = false
    private var saving: Job? = null
    private var paused = false
    private val ratingRevision = MutableStateFlow(0L)
    private var completedRating: RatingQuery? = null
    private val loader = RefreshableListLoader(flowOf(project.id)) { id, force ->
        val entries = repository.entries(id, force)
        currentCoroutineContext().ensureActive()
        entries
    }
    private val list = loader.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), ListUiState.Loading)
    private val ratingSource = combine(list, ratingRevision) { nodes, revision ->
        RatingQuery(nodes is ListUiState.Content && nodes.items.isNotEmpty(), revision)
    }.distinctUntilChanged().flatMapLatest { query ->
        flow {
            emit(rating.value)
            if (completedRating != query) {
                if (!query.hasEntries || ratingHandled) {
                    rating.value = RatingPromptState.Hidden
                } else if (saving?.isActive != true) {
                    val prompt = try {
                        if (ratingRepository.isEligible()) RatingPromptState.Available() else RatingPromptState.Hidden
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        RatingPromptState.Unavailable
                    }
                    currentCoroutineContext().ensureActive()
                    if (!ratingHandled && saving?.isActive != true) {
                        val wasShown = rating.value is RatingPromptState.Available
                        rating.value = prompt
                        if (prompt is RatingPromptState.Available && !wasShown) tracker.ratingShown()
                    }
                }
                currentCoroutineContext().ensureActive()
                completedRating = query
            }
            emitAll(rating)
        }
    }
    val state = combine(list, ratingSource, openRating) { list, prompt, launch ->
        NavigationUiState(project, list, prompt, launch, root)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), NavigationUiState(project, root = root))

    fun refresh() {
        loader.refresh()
        retryRating()
    }
    fun retry() {
        loader.retry()
        retryRating()
    }
    fun retryRating() {
        if (!ratingHandled && saving?.isActive != true) ratingRevision.value++
    }
    fun onResumed() {
        if (paused) retryRating()
        paused = false
        tracker.viewShown()
    }
    fun onPaused() {
        paused = true
    }
    fun onConfigurationRecreation(wasVisible: Boolean) {
        if (wasVisible) paused = false
    }
    fun onRateCancel() = saveChoice(openStore = false)
    fun onRateNow() = saveChoice(openStore = true)

    /** Claims a pending UI launch once, even if stateIn replays an older snapshot on return. */
    fun consumeRatingRequest(): Boolean = openRating.compareAndSet(expect = true, update = false)

    private fun saveChoice(openStore: Boolean) {
        if (rating.value !is RatingPromptState.Available || saving?.isActive == true) return
        saving = viewModelScope.launch {
            rating.value = RatingPromptState.Available(isSaving = true)
            if (openStore) tracker.ratingSelected() else tracker.ratingCancelled()
            try {
                ratingRepository.markHandled()
                currentCoroutineContext().ensureActive()
                ratingHandled = true
                rating.value = RatingPromptState.Hidden
                openRating.value = openStore
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                rating.value = RatingPromptState.Available(saveFailed = true)
            }
        }
    }
    private data class RatingQuery(val hasEntries: Boolean, val revision: Long)
    companion object {
        internal const val ROOT_SCREEN = "navigationRootScreen"
    }
}
