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

package teamcityapp.libraries.navigation

import androidx.compose.runtime.MutableState
import androidx.navigation3.runtime.NavKey
import java.util.UUID
import kotlin.reflect.KClass
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

/** A visit owns state independently of other visits to the same destination. */
@Serializable
data class NavigationEntry(val id: String, @Polymorphic val destination: NavKey)

/** Features define immutable, serializable result payloads and register their serializers. */
interface NavigationResult

@Serializable
data class PendingNavigationResult(val entryId: String, @Polymorphic val value: NavigationResult)

@Serializable
data class NavigationSnapshot(
    val entries: List<NavigationEntry>,
    val results: List<PendingNavigationResult> = emptyList(),
    val sessionKey: String = "initial"
) {
    init {
        require(entries.isNotEmpty()) { "Navigation must have a root destination" }
        require(entries.map { it.id }.distinct().size == entries.size) { "Navigation entry IDs must be unique" }
        require(results.all { result -> entries.any { it.id == result.entryId } }) { "Results must belong to a live entry" }
    }
}

/** UI-owned navigation state; no Activity, Fragment or business operations are retained. */
class Navigator(
    private val state: MutableState<NavigationSnapshot>,
    private val nextId: () -> String = { UUID.randomUUID().toString() }
) {
    val snapshot: NavigationSnapshot get() = state.value
    val current: NavigationEntry get() = snapshot.entries.last()
    val canGoBack: Boolean get() = snapshot.entries.size > 1

    fun push(destination: NavKey): String {
        val entry = NavigationEntry(nextId(), destination)
        state.value = snapshot.copy(entries = snapshot.entries + entry)
        return entry.id
    }

    /** Return false at the root so the host can delegate to the platform's Back behavior. */
    fun pop(): Boolean {
        if (!canGoBack) return false
        removeCurrent()
        return true
    }

    fun replace(destination: NavKey) {
        val previous = current.id
        state.value = snapshot.copy(
            entries = snapshot.entries.dropLast(1) + NavigationEntry(nextId(), destination),
            results = snapshot.results.filterNot { it.entryId == previous }
        )
    }

    /** A new account/session replaces every owner and discards all old-session results. */
    fun reset(destination: NavKey, sessionKey: String = snapshot.sessionKey) {
        state.value = NavigationSnapshot(listOf(NavigationEntry(nextId(), destination)), sessionKey = sessionKey)
    }

    /** Results survive restoration and remain available until the originating entry consumes them. */
    fun finish(result: NavigationResult, recipientId: String = snapshot.entries.getOrNull(snapshot.entries.lastIndex - 1)?.id.orEmpty()): Boolean {
        if (!canGoBack || snapshot.entries.dropLast(1).none { it.id == recipientId }) return false
        val previous = current.id
        state.value = snapshot.copy(
            entries = snapshot.entries.dropLast(1),
            results = snapshot.results.filterNot { it.entryId == previous } + PendingNavigationResult(recipientId, result)
        )
        return true
    }

    /** Returns to a live visit without replacing its owners, optionally delivering a typed result. */
    fun popTo(entryId: String, result: NavigationResult? = null): Boolean {
        val index = snapshot.entries.indexOfFirst { it.id == entryId }
        if (index < 0) return false
        val entries = snapshot.entries.take(index + 1)
        val liveIds = entries.map { it.id }.toSet()
        state.value = snapshot.copy(
            entries = entries,
            results = snapshot.results.filter { it.entryId in liveIds } +
                if (result == null) emptyList() else listOf(PendingNavigationResult(entryId, result))
        )
        return true
    }

    fun <T : NavigationResult> consumeResult(entryId: String, type: KClass<T>): T? {
        val index = snapshot.results.indexOfFirst { it.entryId == entryId && type.isInstance(it.value) }
        if (index == -1) return null
        val result = type.java.cast(snapshot.results[index].value)
        state.value = snapshot.copy(results = snapshot.results.filterIndexed { i, _ -> i != index })
        return result
    }

    private fun removeCurrent() {
        val previous = current.id
        state.value = snapshot.copy(entries = snapshot.entries.dropLast(1), results = snapshot.results.filterNot { it.entryId == previous })
    }
}
