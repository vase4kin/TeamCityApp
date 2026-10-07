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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import teamcityapp.features.run_build.api.*

data class RunBuildUiState(val branches: List<String>? = null, val branchesFailed: Boolean = false, val agents: List<BuildAgent>? = null, val agentsFailed: Boolean = false, val request: BuildRequest = BuildRequest(""), val queuing: Boolean = false, val queueError: QueueBuildResult? = null, val queuedHref: String? = null)

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
