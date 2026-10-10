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

package teamcityapp.features.tests.impl

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import teamcityapp.features.tests.api.TestOccurrence
import teamcityapp.features.tests.api.TestsFilter
import teamcityapp.features.tests.api.TestsRepository

/** The app adapter treats continuation tokens as opaque URLs and preserves server ordering. */
internal class TestsPagingSource(
    private val repository: TestsRepository,
    private val url: String,
    private val filter: TestsFilter,
    private val forceRefresh: Boolean
) : PagingSource<String, TestOccurrence>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, TestOccurrence> = try {
        // Legacy append always bypasses cached pages; initial/filter requests may use cache.
        val page = repository.tests(url, filter, params.key, params is LoadParams.Append || forceRefresh)
        currentCoroutineContext().ensureActive()
        LoadResult.Page(page.items.toList(), prevKey = null, nextKey = page.nextHref?.takeUnless { it.isBlank() })
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        currentCoroutineContext().ensureActive()
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<String, TestOccurrence>): String? = null
}
