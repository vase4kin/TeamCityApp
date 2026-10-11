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

package teamcityapp.features.build_history.impl

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import teamcityapp.features.build_history.api.BuildHistoryQuery
import teamcityapp.features.build_history.api.BuildHistoryRepository
import teamcityapp.libraries.builds.BuildLaunchData

internal class BuildHistoryPagingSource(
    private val repository: BuildHistoryRepository,
    private val query: BuildHistoryQuery,
    private val forceRefresh: Boolean
) : PagingSource<String, BuildLaunchData>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, BuildLaunchData> = try {
        val page = repository.page(query, params.key, forceRefresh || params is LoadParams.Append)
        currentCoroutineContext().ensureActive()
        // Stable partition keeps queued builds first and retains server order within either group.
        val builds = page.builds.sortedBy { !it.isQueued }
        LoadResult.Page(builds, prevKey = null, nextKey = page.nextHref?.takeUnless { it.isBlank() || builds.isEmpty() })
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        currentCoroutineContext().ensureActive()
        LoadResult.Error(error)
    }
    override fun getRefreshKey(state: PagingState<String, BuildLaunchData>): String? = null
}
