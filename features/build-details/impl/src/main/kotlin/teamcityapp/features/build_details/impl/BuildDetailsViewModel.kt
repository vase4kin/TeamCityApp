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

package teamcityapp.features.build_details.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.tracker.BuildDetailsTracker
import teamcityapp.libraries.builds.BuildLaunchData

@HiltViewModel
internal class BuildDetailsViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: BuildDetailsRepository,
    private val tracker: BuildDetailsTracker
) : ViewModel() {
    val incoming = Json.decodeFromString<BuildLaunchData>(checkNotNull(savedState[BUILD]))
    private val current = savedState.get<String>(CURRENT)?.let { Json.decodeFromString<BuildLaunchData>(it).takeIf { build -> build.id == incoming.id } } ?: incoming
    private var nextId = savedState.get<Long>(SEQUENCE) ?: 0L
    private var queuedHref = savedState.get<String>(QUEUED)
    private val mutableState = MutableStateFlow(
        BuildDetailsUiState(
            incoming = incoming,
            current = current,
            configurationName = savedState[NAME],
            confirmation = savedState.get<String>(CONFIRMATION)?.let { Json.decodeFromString<BuildConfirmation>(it) },
            overviewRefresh = savedState.get<Long>(OVERVIEW_REFRESH) ?: 0,
            notice = savedState.get<String>(NOTICE)?.let { Json.decodeFromString<BuildDetailsNotice>(it) },
            replacement = savedState.get<String>(REPLACEMENT)?.let { BuildDetailsReplacement(checkNotNull(savedState[REPLACEMENT_ID]), Json.decodeFromString<BuildLaunchData>(it), savedState.get<String>(REPLACEMENT_COMPLETION)?.let(BuildDetailsCompletion::valueOf)) }
        )
    )
    val state = mutableState.asStateFlow()

    init {
        if (!savedState.contains(NOTICE)) {
            savedState.get<String>(COMPLETION)?.let(BuildDetailsCompletion::valueOf)?.let { completion ->
                notice(if (completion == BuildDetailsCompletion.Stopped) R.string.text_build_is_stopped else R.string.text_build_is_removed_from_queue)
            }
        }
    }

    fun viewed() = tracker.viewed()
    fun loaded(build: BuildLaunchData) {
        if (build.id != incoming.id) return
        savedState[CURRENT] = Json.encodeToString(build)
        mutableState.value = state.value.copy(current = build)
    }
    fun requestCancellation() {
        val state = state.value
        if (state.busy || state.replacement != null || state.confirmation != null || (!state.current.isRunning && !state.current.isQueued)) return
        mutableState.value = state.copy(preparingConfirmation = true)
        viewModelScope.launch {
            try {
                val own = repository.triggeredByCurrentUser(state.current)
                setConfirmation(BuildConfirmation(state.current, if (state.current.isRunning) ConfirmationKind.Stop else ConfirmationKind.Remove, own))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                notice(if (state.current.isRunning) R.string.error_base_stop_build_error else R.string.error_base_remove_build_from_queue_error)
            } finally {
                mutableState.value = mutableState.value.copy(preparingConfirmation = false)
            }
        }
    }
    fun requestRestart() {
        if (!state.value.busy && state.value.replacement == null && state.value.confirmation == null && !state.value.current.isRunning && !state.value.current.isQueued) setConfirmation(BuildConfirmation(state.value.current, ConfirmationKind.Restart))
    }
    fun dismissConfirmation() {
        if (!state.value.busy) setConfirmation(null)
    }
    private fun setConfirmation(confirmation: BuildConfirmation?) {
        savedState[CONFIRMATION] = confirmation?.let { Json.encodeToString(it) }
        mutableState.value = state.value.copy(confirmation = confirmation)
    }
    fun confirm(reAdd: Boolean) {
        val confirmation = state.value.confirmation ?: return
        if (state.value.busy) return
        setConfirmation(null)
        val operation = when (confirmation.kind) {
            ConfirmationKind.Stop -> BuildDetailsOperation.Stop
            ConfirmationKind.Remove -> BuildDetailsOperation.Remove
            ConfirmationKind.Restart -> BuildDetailsOperation.Restart
        }
        mutableState.value = state.value.copy(operation = operation)
        if (operation != BuildDetailsOperation.Restart) tracker.confirmedCancel(reAdd && operation == BuildDetailsOperation.Stop)
        viewModelScope.launch {
            try {
                when (operation) {
                    BuildDetailsOperation.Restart -> when (val result = repository.restart(confirmation.build)) {
                        is BuildDetailsMutation.Success -> {
                            queuedHref = result.value
                            savedState[QUEUED] = result.value
                            tracker.restartSucceeded()
                            notice(R.string.text_build_is_restarted, openQueued = true)
                        }

                        BuildDetailsMutation.Forbidden -> {
                            tracker.restartFailed(true)
                            notice(R.string.error_restart_build_forbidden_error)
                        }

                        BuildDetailsMutation.Failed -> {
                            tracker.restartFailed(false)
                            notice(R.string.error_base_restart_build_error)
                        }
                    }

                    else -> when (val result = repository.cancel(confirmation.build, reAdd && operation == BuildDetailsOperation.Stop)) {
                        is BuildDetailsMutation.Success -> {
                            tracker.cancelSucceeded()
                            replace(result.value, if (operation == BuildDetailsOperation.Stop) BuildDetailsCompletion.Stopped else BuildDetailsCompletion.RemovedFromQueue)
                        }

                        BuildDetailsMutation.Forbidden -> {
                            tracker.cancelFailed(true)
                            notice(if (operation == BuildDetailsOperation.Stop) R.string.error_stop_build_forbidden_error else R.string.error_remove_build_from_queue_forbidden_error)
                        }

                        BuildDetailsMutation.Failed -> cancelFailed(operation)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (operation == BuildDetailsOperation.Restart) {
                    tracker.restartFailed(false)
                    notice(R.string.error_base_restart_build_error)
                } else {
                    cancelFailed(operation)
                }
            } finally {
                mutableState.value = state.value.copy(operation = null)
            }
        }
    }
    private fun cancelFailed(operation: BuildDetailsOperation) {
        tracker.cancelFailed(false)
        val version = state.value.overviewRefresh + 1
        savedState[OVERVIEW_REFRESH] = version
        mutableState.value = state.value.copy(overviewRefresh = version)
        notice(if (operation == BuildDetailsOperation.Stop) R.string.error_base_stop_build_error else R.string.error_base_remove_build_from_queue_error)
    }
    fun openQueued() {
        val href = queuedHref ?: return
        if (state.value.busy || state.value.replacement != null) return
        mutableState.value = state.value.copy(operation = BuildDetailsOperation.OpenQueued)
        viewModelScope.launch {
            try {
                val build = repository.queuedBuild(href)
                tracker.queuedOpened()
                replace(build)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                tracker.queuedFailed()
                notice(R.string.error_opening_build, openQueued = true)
            } finally {
                mutableState.value = state.value.copy(operation = null)
            }
        }
    }
    private fun id(): Long = (++nextId).also { savedState[SEQUENCE] = it }
    private fun notice(message: Int, openQueued: Boolean = false) {
        val notice = BuildDetailsNotice(id(), message, openQueued)
        savedState[NOTICE] = Json.encodeToString(notice)
        mutableState.value = state.value.copy(notice = notice)
    }
    fun consumeNotice(id: Long) {
        if (state.value.notice?.id == id) {
            savedState[NOTICE] = null
            mutableState.value = state.value.copy(notice = null)
        }
    }
    private fun replace(build: BuildLaunchData, completion: BuildDetailsCompletion? = null) {
        savedState[REPLACEMENT] = Json.encodeToString(build)
        savedState[REPLACEMENT_COMPLETION] = completion?.name
        val replacementId = id()
        savedState[REPLACEMENT_ID] = replacementId
        mutableState.value = state.value.copy(replacement = BuildDetailsReplacement(replacementId, build, completion))
    }
    fun consumeReplacement(id: Long) {
        if (state.value.replacement?.id == id) {
            savedState[REPLACEMENT] = null
            savedState[REPLACEMENT_COMPLETION] = null
            savedState[REPLACEMENT_ID] = null
            mutableState.value = state.value.copy(replacement = null)
        }
    }
    companion object {
        internal const val BUILD = "details.incoming"
        internal const val NAME = "details.name"
        internal const val COMPLETION = "details.completion"
        private const val REPLACEMENT_COMPLETION = "details.replacement-completion"
        private const val OVERVIEW_REFRESH = "details.overview.refresh"
        private const val CURRENT = "details.current"
        private const val CONFIRMATION = "details.confirmation"
        private const val QUEUED = "details.queued"
        private const val REPLACEMENT = "details.replacement"
        private const val NOTICE = "details.notice"
        private const val REPLACEMENT_ID = "details.replacement.id"
        private const val SEQUENCE = "details.sequence"
    }
}
