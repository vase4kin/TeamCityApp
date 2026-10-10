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

package teamcityapp.libraries.list_state

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Loads the latest query while preserving completed results across subscription gaps.
 *
 * Share [state] once with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(0),
 * ListUiState.Loading)`, or share [queryState] when the query is part of the screen state.
 * The owner controls the scope and subscriptions; this helper owns
 * no coroutine scope. Stopping collection cancels work, and resubscribing resumes an
 * unfinished load. A changed query discards the previous query's rows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RefreshableListLoader<Q, T>(
    private val queries: Flow<Q>,
    private val load: suspend (query: Q, forceRefresh: Boolean) -> List<T>
) {
    private val lock = Any()
    private val requests = MutableStateFlow(Request(0, false))
    private var retained: Session<Q, T>? = null

    /** Tags every state with its producing query, keeping filters and rows consistent. */
    val queryState: Flow<Pair<Q, ListUiState<T>>> = channelFlow {
        queries.distinctUntilChanged().collectLatest { query ->
            val session = synchronized(lock) {
                retained?.takeIf { it.query == query } ?: Session<Q, T>(
                    query = query,
                    revision = requests.value.revision,
                    forceRefresh = retained == null && requests.value.forceRefresh
                ).also { retained = it }
            }
            requests.collectLatest { request -> load(session, request) }
        }
    }.distinctUntilChanged()

    val state: Flow<ListUiState<T>> = queryState.map { it.second }.distinctUntilChanged()

    /** Replaces any pending request and refreshes cached content when possible. */
    fun refresh() = request(forceRefresh = true)

    /** Reloads through the repository's normal cache policy, retaining completed rows. */
    fun reload() = request(forceRefresh = false)

    private fun request(forceRefresh: Boolean) {
        synchronized(lock) {
            requests.value = Request(requests.value.revision + 1, forceRefresh)
        }
    }

    /** Retries using fresh data, including after an initial or refresh failure. */
    fun retry() = refresh()

    private suspend fun ProducerScope<Pair<Q, ListUiState<T>>>.load(session: Session<Q, T>, request: Request) {
        val attempt = synchronized(lock) {
            if (session.revision != request.revision) {
                session.revision = request.revision
                session.completed = false
                session.forceRefresh = request.forceRefresh
            }
            if (!session.completed) session.state = session.state.refreshing()
            Attempt(session.state, session.completed, session.forceRefresh)
        }
        send(session.query to attempt.state)
        if (attempt.completed) return

        val result = try {
            val items = load(session.query, attempt.forceRefresh).toList()
            currentCoroutineContext().ensureActive()
            if (items.isEmpty()) ListUiState.Empty() else ListUiState.Content(items)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            attempt.state.failed()
        }
        currentCoroutineContext().ensureActive()
        val isCurrent = synchronized(lock) {
            if (retained !== session || requests.value != request) {
                false
            } else {
                session.state = result
                session.completed = true
                true
            }
        }
        if (isCurrent) send(session.query to result)
    }

    private fun ListUiState<T>.refreshing(): ListUiState<T> = when (this) {
        is ListUiState.Content -> copy(isRefreshing = true, refreshFailed = false)
        is ListUiState.Empty -> copy(isRefreshing = true, refreshFailed = false)
        else -> ListUiState.Loading
    }

    private fun ListUiState<T>.failed(): ListUiState<T> = when (this) {
        is ListUiState.Content -> copy(isRefreshing = false, refreshFailed = true)
        is ListUiState.Empty -> copy(isRefreshing = false, refreshFailed = true)
        else -> ListUiState.Error
    }

    private class Session<Q, T>(
        val query: Q,
        var revision: Long,
        var forceRefresh: Boolean
    ) {
        var state: ListUiState<T> = ListUiState.Loading
        var completed = false
    }

    private data class Request(val revision: Long, val forceRefresh: Boolean)

    private data class Attempt<T>(
        val state: ListUiState<T>,
        val completed: Boolean,
        val forceRefresh: Boolean
    )
}
