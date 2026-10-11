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

package teamcityapp.features.build_details.impl

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData

internal data class BuildDetailsPromptUi(val prompt: BuildDetailsPrompt, @StringRes val title: Int = R.string.title_onboarding_build_menu, @StringRes val description: Int)
internal data class BuildDetailsMetadataState(val counts: Map<BuildDetailsTab, BuildDetailsCount> = emptyMap(), val prompt: BuildDetailsPromptUi? = null, val onboardingFailed: Boolean = false)

/** Only collection starts reads. Completed results belong to this particular build visit. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class BuildDetailsMetadataViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: BuildDetailsMetadataRepository
) : ViewModel() {
    private val incoming = Json.decodeFromString<BuildLaunchData>(checkNotNull(savedState[BuildDetailsViewModel.BUILD]))
    private val current = MutableStateFlow(incoming)
    private val tabs = listOf(BuildDetailsTab.Changes, BuildDetailsTab.Tests).filter { it in incoming.detailsTabs() }
    private val requests = tabs.associateWith { MutableStateFlow(0L) }
    private val initialCounts = tabs.associateWith { tab -> savedState.get<String>(countKey(tab))?.let { Json.decodeFromString<BuildDetailsCount>(it) } ?: BuildDetailsCount.Loading }
    private fun countKey(tab: BuildDetailsTab) = "details.metadata.count:${tab.name}"
    private fun count(tab: BuildDetailsTab): Flow<Map<BuildDetailsTab, BuildDetailsCount>> = requests.getValue(tab).flatMapLatest {
        flow {
            val completed = savedState.get<String>(countKey(tab))?.let { Json.decodeFromString<BuildDetailsCount>(it) }
            if (completed != null) {
                emit(mapOf(tab to completed))
                return@flow
            }
            emit(mapOf(tab to BuildDetailsCount.Loading))
            val result = try {
                val count = repository.count(incoming, tab)
                require(count >= 0)
                BuildDetailsCount.Available(count)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                BuildDetailsCount.Unavailable
            }
            currentCoroutineContext().ensureActive()
            savedState[countKey(tab)] = Json.encodeToString<BuildDetailsCount>(result)
            emit(mapOf(tab to result))
        }
    }
    private val counts = if (tabs.isEmpty()) flowOf(emptyMap()) else combine(tabs.map(::count)) { sections -> sections.fold(emptyMap<BuildDetailsTab, BuildDetailsCount>()) { all, section -> all + section } }
    private val promptRequests = MutableStateFlow(0L)
    private var completedPrompts: Set<BuildDetailsPrompt>? = null
    private val dismissed = MutableStateFlow(savedState.get<ArrayList<String>>(DISMISSED).orEmpty().mapNotNull { name -> BuildDetailsPrompt.entries.firstOrNull { it.name == name } }.toSet())
    private val acknowledgementFailed = MutableStateFlow(false)
    private val education = promptRequests.flatMapLatest {
        flow {
            completedPrompts?.let {
                emit(it to false)
                return@flow
            }
            emit(emptySet<BuildDetailsPrompt>() to false)
            try {
                val prompts = repository.pendingPrompts()
                currentCoroutineContext().ensureActive()
                completedPrompts = prompts
                emit(prompts to false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                emit(emptySet<BuildDetailsPrompt>() to true)
            }
        }
    }
    val state = combine(counts, education, current, dismissed, acknowledgementFailed) { counts, education, build, dismissed, failed ->
        val kind = when {
            build.isRunning -> BuildDetailsPrompt.Stop
            build.isQueued -> BuildDetailsPrompt.Remove
            build.isFinished -> BuildDetailsPrompt.Restart
            else -> null
        }
        val prompt = kind?.takeIf { it in education.first && it !in dismissed }?.let {
            BuildDetailsPromptUi(
                it,
                description = when (it) {
                    BuildDetailsPrompt.Stop -> R.string.text_onboarding_stop_build
                    BuildDetailsPrompt.Remove -> R.string.text_onboarding_remove_build_from_queue
                    BuildDetailsPrompt.Restart -> R.string.text_onboarding_restart_build
                }
            )
        }
        BuildDetailsMetadataState(counts, prompt, education.second || failed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildDetailsMetadataState(initialCounts))

    fun loaded(build: BuildLaunchData) {
        if (build.id == incoming.id) current.value = build
    }
    fun retryCount(tab: BuildDetailsTab) {
        val request = requests[tab] ?: return
        savedState.remove<String>(countKey(tab))
        request.value++
    }
    fun retryFailed() {
        state.value.counts.filterValues { it == BuildDetailsCount.Unavailable }.keys.forEach(::retryCount)
        if (state.value.onboardingFailed) {
            acknowledgementFailed.value = false
            completedPrompts = null
            // A failed write retries through the normal explicit prompt dismissal.
            dismissed.value = emptySet()
            savedState[DISMISSED] = arrayListOf<String>()
            promptRequests.value++
        }
    }
    fun dismissPrompt() {
        val prompt = state.value.prompt?.prompt ?: return
        if (prompt in dismissed.value) return
        dismissed.value += prompt
        savedState[DISMISSED] = ArrayList(dismissed.value.map { it.name })
        viewModelScope.launch {
            try {
                repository.acknowledgePrompt(prompt)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                acknowledgementFailed.value = true
            }
        }
    }
    companion object {
        private const val DISMISSED = "details.metadata.dismissed"
    }
}
