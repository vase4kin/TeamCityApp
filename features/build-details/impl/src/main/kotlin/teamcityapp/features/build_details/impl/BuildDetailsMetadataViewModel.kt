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

internal data class BuildDetailsPromptUi(
    val prompt: BuildDetailsPrompt,
    val saving: Boolean = false,
    val saveFailed: Boolean = false
) {
    val action: BuildDetailsAction get() = when (prompt) {
        BuildDetailsPrompt.Stop, BuildDetailsPrompt.Remove -> BuildDetailsAction.Cancel
        BuildDetailsPrompt.Restart -> BuildDetailsAction.Restart
        BuildDetailsPrompt.Share -> BuildDetailsAction.Share
        BuildDetailsPrompt.Browser -> BuildDetailsAction.Browser
    }

    @get:StringRes val title: Int get() = when (prompt) {
        BuildDetailsPrompt.Stop -> R.string.title_onboarding_stop_build
        BuildDetailsPrompt.Remove -> R.string.title_onboarding_remove_build
        BuildDetailsPrompt.Restart -> R.string.title_onboarding_restart_build
        BuildDetailsPrompt.Share -> R.string.title_onboarding_share_build
        BuildDetailsPrompt.Browser -> R.string.title_onboarding_browser
    }

    @get:StringRes val description: Int get() = when (prompt) {
        BuildDetailsPrompt.Stop -> R.string.text_onboarding_stop_build
        BuildDetailsPrompt.Remove -> R.string.text_onboarding_remove_build_from_queue
        BuildDetailsPrompt.Restart -> R.string.text_onboarding_restart_build
        BuildDetailsPrompt.Share -> R.string.text_onboarding_share_build
        BuildDetailsPrompt.Browser -> R.string.text_onboarding_browser
    }
}
private data class PromptAcknowledgement(val prompt: BuildDetailsPrompt? = null, val saving: Boolean = false, val failed: Boolean = false)
internal data class BuildDetailsMetadataState(val counts: Map<BuildDetailsSection, BuildDetailsCount> = emptyMap(), val prompt: BuildDetailsPromptUi? = null, val onboardingFailed: Boolean = false)

/** Only collection starts reads. Completed results belong to this particular build visit. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class BuildDetailsMetadataViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: BuildDetailsMetadataRepository
) : ViewModel() {
    private val incoming = Json.decodeFromString<BuildLaunchData>(checkNotNull(savedState[BuildDetailsViewModel.BUILD]))
    private val current = MutableStateFlow(incoming)
    private val tabs = listOf(BuildDetailsSection.Changes, BuildDetailsSection.Tests).filter { it in incoming.detailsSections() }
    private val requests = tabs.associateWith { MutableStateFlow(0L) }
    private val initialCounts = tabs.associateWith { tab -> savedState.get<String>(countKey(tab))?.let { Json.decodeFromString<BuildDetailsCount>(it) } ?: BuildDetailsCount.Loading }
    private fun countKey(tab: BuildDetailsSection) = "details.metadata.count:${tab.name}"
    private fun count(tab: BuildDetailsSection): Flow<Map<BuildDetailsSection, BuildDetailsCount>> = requests.getValue(tab).flatMapLatest {
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
    private val counts = if (tabs.isEmpty()) flowOf(emptyMap()) else combine(tabs.map(::count)) { sections -> sections.fold(emptyMap<BuildDetailsSection, BuildDetailsCount>()) { all, section -> all + section } }
    private val promptRequests = MutableStateFlow(0L)
    private var completedPrompts: Set<BuildDetailsPrompt>? = null
    private val dismissed = MutableStateFlow(savedState.get<ArrayList<String>>(DISMISSED).orEmpty().mapNotNull { name -> BuildDetailsPrompt.entries.firstOrNull { it.name == name } }.toSet())
    private val acknowledgement = MutableStateFlow(PromptAcknowledgement())
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
    val state = combine(counts, education, current, dismissed, acknowledgement) { counts, education, build, dismissed, acknowledgement ->
        val kind = when {
            build.isRunning -> BuildDetailsPrompt.Stop
            build.isQueued -> BuildDetailsPrompt.Remove
            build.isFinished -> BuildDetailsPrompt.Restart
            else -> null
        }
        val available = buildList {
            kind?.let(::add)
            if (!build.webUrl.isNullOrBlank()) {
                add(BuildDetailsPrompt.Share)
                add(BuildDetailsPrompt.Browser)
            }
        }
        val prompt = available.firstOrNull { it in education.first && it !in dismissed }?.let {
            BuildDetailsPromptUi(it, saving = acknowledgement.saving, saveFailed = acknowledgement.prompt == it && acknowledgement.failed)
        }
        BuildDetailsMetadataState(counts, prompt, education.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), BuildDetailsMetadataState(initialCounts))

    fun loaded(build: BuildLaunchData) {
        if (build.id == incoming.id) current.value = build
    }
    fun retryCount(tab: BuildDetailsSection) {
        val request = requests[tab] ?: return
        savedState.remove<String>(countKey(tab))
        request.value++
    }
    fun retryFailed() {
        state.value.counts.filterValues { it == BuildDetailsCount.Unavailable }.keys.forEach(::retryCount)
        if (state.value.onboardingFailed) {
            completedPrompts = null
            promptRequests.value++
        }
    }
    fun dismissPrompt() {
        val prompt = state.value.prompt?.prompt ?: return
        if (prompt in dismissed.value || acknowledgement.value.saving) return
        acknowledgement.value = PromptAcknowledgement(prompt, saving = true)
        viewModelScope.launch {
            try {
                repository.acknowledgePrompt(prompt)
                dismissed.value += prompt
                savedState[DISMISSED] = ArrayList(dismissed.value.map { it.name })
                acknowledgement.value = PromptAcknowledgement()
            } catch (cancelled: CancellationException) {
                acknowledgement.value = PromptAcknowledgement()
                throw cancelled
            } catch (_: Exception) {
                acknowledgement.value = PromptAcknowledgement(prompt, failed = true)
            }
        }
    }
    companion object {
        private const val DISMISSED = "details.metadata.dismissed"
    }
}
