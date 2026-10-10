/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.changes.impl

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.changes.api.ChangesRepository

/** Pagination follows nextHref without interpreting its query or recreating an offset. */
internal class ChangesPagingSource(
    private val repository: ChangesRepository,
    private val url: String,
    private val forceRefresh: Boolean
) : PagingSource<String, ChangeDetails>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, ChangeDetails> = try {
        val page = repository.changes(url, params.key, forceRefresh && params is LoadParams.Refresh)
        LoadResult.Page(page.items, prevKey = null, nextKey = page.nextHref?.takeUnless { it.isBlank() })
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    // Refresh always restarts from the first page, rather than from an old continuation token.
    override fun getRefreshKey(state: PagingState<String, ChangeDetails>): String? = null
}
