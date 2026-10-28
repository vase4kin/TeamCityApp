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

package teamcityapp.features.build_overview.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.features.build_overview.api.BuildReferenceTracker
import teamcityapp.libraries.list_state.*

/** Resolve a reference through the same full-snapshot repository before exposing Overview rows. */
@OptIn(ExperimentalCoroutinesApi::class)
class BuildReferenceViewModel(href: String, repository: BuildOverviewRepository, private val tracker: BuildReferenceTracker, private val metadata: BuildDetailsMetadataRepository? = null) : ViewModel() {
    private val loader = RefreshableListLoader(flowOf(href)) { reference, force ->
        val build = try {
            repository.build(reference, force)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            currentCoroutineContext().ensureActive()
            tracker.failed()
            throw failure
        }
        currentCoroutineContext().ensureActive()
        tracker.loaded()
        listOf(build)
    }
    private val retries = MutableStateFlow(emptyMap<BuildDetailsSection, Long>())
    private val completedCounts = mutableMapOf<Pair<String, BuildDetailsSection>, BuildDetailsCount>()
    val state = loader.state.flatMapLatest { builds ->
        val build = (builds as? ListUiState.Content)?.items?.singleOrNull()
        val sections = build?.detailsSections().orEmpty().filter { it == BuildDetailsSection.Changes || it == BuildDetailsSection.Tests }
        if (metadata == null || build == null || sections.isEmpty()) {
            flowOf(buildOverviewState(builds))
        } else {
            combine(
                sections.map { section ->
                    retries.map { it[section] ?: 0 }.distinctUntilChanged().flatMapLatest {
                        flow<Map<BuildDetailsSection, BuildDetailsCount>> {
                            val key = build.id to section
                            val cached = completedCounts[key]
                            if (cached != null) {
                                emit(mapOf(section to cached))
                            } else {
                                emit(mapOf(section to BuildDetailsCount.Loading))
                                val result = try {
                                    val count = metadata.count(build, section)
                                    require(count >= 0)
                                    BuildDetailsCount.Available(count)
                                } catch (c: CancellationException) {
                                    throw c
                                } catch (_: Exception) {
                                    BuildDetailsCount.Unavailable
                                }
                                completedCounts[key] = result
                                emit(mapOf(section to result))
                            }
                        }
                    }
                }
            ) { counts -> buildOverviewState(builds, counts = counts.fold(emptyMap<BuildDetailsSection, BuildDetailsCount>()) { all, item -> all + item }) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildOverviewUiState())
    fun refresh() {
        completedCounts.clear()
        loader.refresh()
    }
    fun updateCount(section: BuildDetailsSection, count: Int) {
        require(count >= 0)
        state.value.build?.let { completedCounts[it.id to section] = BuildDetailsCount.Available(count) }
        retries.value = retries.value + (section to ((retries.value[section] ?: 0) + 1))
    }
    fun retryCount(section: BuildDetailsSection) {
        state.value.build?.let { completedCounts.remove(it.id to section) }
        retries.value = retries.value + (section to ((retries.value[section] ?: 0) + 1))
    }
    fun retry() = loader.retry()
}
