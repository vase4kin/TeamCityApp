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

package teamcityapp.libraries.list_ui

import androidx.paging.compose.LazyPagingItems

/**
 * Keeps lazy counts, keys and section boundaries on the same immutable Paging snapshot.
 * Outgoing content can still read its rows after a refresh or filter replaces the presenter.
 * Only access to the current snapshot sends Paging prefetch hints.
 */
class PagingListSnapshot<T : Any>(private val pagingItems: LazyPagingItems<T>) {
    private val snapshot = pagingItems.itemSnapshotList
    val itemCount: Int get() = snapshot.size
    val items: List<T> get() = snapshot.items

    fun peek(index: Int): T? = snapshot.getOrNull(index)

    operator fun get(index: Int): T? {
        if (index !in snapshot.indices) return null
        return if (pagingItems.itemSnapshotList === snapshot) pagingItems[index] else snapshot[index]
    }
}
