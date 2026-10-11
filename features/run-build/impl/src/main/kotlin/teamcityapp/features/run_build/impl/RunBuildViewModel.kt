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

package teamcityapp.features.run_build.impl

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import teamcityapp.features.run_build.api.*

data class RunBuildUiState(val branches: List<String>? = null, val branchesFailed: Boolean = false, val agents: List<BuildAgent>? = null, val agentsFailed: Boolean = false, val request: BuildRequest = BuildRequest(""), val queuing: Boolean = false, val queueError: QueueBuildResult? = null, val queuedHref: String? = null) {
    @get:StringRes val queueLabelRes: Int = if (queuing) R.string.text_queueing_build else R.string.title_run_build

    @get:StringRes val queueErrorMessageRes: Int? = when (queueError) {
        QueueBuildResult.Forbidden -> R.string.error_forbidden_error
        null -> null
        else -> R.string.error_base_error
    }

    @get:StringRes val branchesMessageRes: Int = if (branchesFailed) R.string.branches_unavailable else R.string.text_no_branches_available

    @get:StringRes val personalLabelRes: Int = if (request.personal) R.string.option_on else R.string.option_off

    @get:StringRes val priorityLabelRes: Int = if (request.queueAtTop) R.string.priority_top else R.string.priority_normal

    @get:StringRes val cleanSourcesLabelRes: Int = if (request.cleanSources) R.string.option_on else R.string.option_off

    @get:StringRes val agentFallbackRes: Int = if (agents.isNullOrEmpty()) R.string.text_no_agents_available else R.string.hint_default_filter_agent
    val selectedAgentName: String? = request.agent?.name?.takeIf { !agents.isNullOrEmpty() }
}

@HiltViewModel
class RunBuildViewModel @Inject constructor(private val repository: RunBuildRepository, savedStateHandle: SavedStateHandle, private val tracker: RunBuildTracker) : ViewModel() {
    private val buildTypeId = savedStateHandle.get<String>(BUILD_TYPE_ID).orEmpty()
    private val form = MutableStateFlow(RunBuildUiState(request = BuildRequest(buildTypeId)))
    private var branches: Pair<List<String>, Boolean>? = null
    private var agents: Pair<List<BuildAgent>, Boolean>? = null
    private val branchFlow = flow<Pair<List<String>?, Boolean>> {
        val data = branches ?: try {
            repository.branches(buildTypeId) to false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyList<String>() to true
        }
        branches = data
        if (data.first.size == 1 && form.value.request.branch.isEmpty()) form.update { it.copy(request = it.request.copy(branch = data.first.single())) }
        emit(data)
    }.onStart { emit(branches ?: (null to false)) }
    private val agentFlow = flow<Pair<List<BuildAgent>?, Boolean>> {
        val data = agents ?: try {
            repository.agents(buildTypeId) to false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyList<BuildAgent>() to true
        }
        agents = data
        emit(data)
    }.onStart { emit(agents ?: (null to false)) }
    val state = combine(form, branchFlow, agentFlow) { state, branches, agents -> state.copy(branches = branches.first, branchesFailed = branches.second, agents = agents.first, agentsFailed = agents.second) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), form.value)
    fun update(request: BuildRequest) {
        if (!form.value.queuing && form.value.queuedHref == null) form.update { it.copy(request = request.copy(buildTypeId = buildTypeId)) }
    }
    fun viewed() = tracker.trackView()
    fun addingParameter() = tracker.trackUserClicksOnAddNewBuildParamButton()
    fun addParameter(name: String, value: String): Boolean {
        if (name.isEmpty() || form.value.queuing) return false
        update(form.value.request.copy(parameters = form.value.request.parameters + BuildParameter(name, value)))
        tracker.trackUserAddsBuildParam()
        return true
    }
    fun clearParameters() {
        update(form.value.request.copy(parameters = emptyList()))
        tracker.trackUserClicksOnClearAllBuildParamsButton()
    }
    fun consumeSuccess(href: String): Boolean {
        if (form.value.queuedHref != href) return false
        form.update { it.copy(queuedHref = null) }
        return true
    }
    fun clearError() {
        form.update { it.copy(queueError = null) }
    }
    fun queue() {
        if (form.value.queuing || form.value.queuedHref != null) return
        val request = form.value.request
        form.update { it.copy(queuing = true, queueError = null) }
        viewModelScope.launch {
            val result = try {
                repository.queue(request)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                QueueBuildResult.Error
            }
            when (result) {
                is QueueBuildResult.Success -> {
                    if (request.parameters.isEmpty()) tracker.trackUserRunBuildSuccess() else tracker.trackUserRunBuildWithCustomParamsSuccess()
                    form.update { it.copy(queuing = false, queuedHref = result.href) }
                }

                QueueBuildResult.Forbidden -> {
                    tracker.trackUserRunBuildFailedForbidden()
                    form.update { it.copy(queuing = false, queueError = result) }
                }

                QueueBuildResult.Error -> {
                    tracker.trackUserRunBuildFailed()
                    form.update { it.copy(queuing = false, queueError = result) }
                }
            }
        }
    }
}
