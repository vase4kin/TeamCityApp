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

import android.app.Application
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PagingListSnapshotTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var latest: PagingListSnapshot<Int>

    private fun render(pages: Flow<PagingData<Int>>) {
        compose.setContent {
            val items = pages.collectAsLazyPagingItems()
            val snapshot = PagingListSnapshot(items)
            SideEffect { latest = snapshot }
        }
    }

    @Test fun retainedCallbacksKeepTheirRowsAndKeysWhenRefreshShrinksToTenItems() {
        val pages = MutableStateFlow(PagingData.from((0 until 20).toList()))
        render(pages)
        compose.waitForIdle()
        compose.waitUntil(5_000) { ::latest.isInitialized && latest.itemCount == 20 }
        val outgoing = latest
        val key = { index: Int -> outgoing.peek(index) ?: "placeholder:$index" }
        compose.runOnIdle { pages.value = PagingData.from((100 until 110).toList()) }
        compose.waitForIdle()
        compose.waitUntil(5_000) { latest.itemCount == 10 }
        compose.runOnIdle {
            // The crash asked the previous lazy interval for index 10 after Paging shrank.
            assertEquals(20, outgoing.itemCount)
            assertEquals(10, key(10))
            assertEquals(10, outgoing[10])
            assertEquals(19, outgoing.peek(19))
            assertEquals(100, latest[0])
            assertNull(latest.peek(10))
            assertNull(latest[10])
            assertNull(latest.peek(-1))
            assertNull(latest[-1])
        }
    }

    @Test fun outgoingFilterRowsSurviveAnEmptyAndSameSizedReplacement() {
        val pages = MutableStateFlow(PagingData.from((0 until 10).toList()))
        render(pages)
        compose.waitForIdle()
        compose.waitUntil(5_000) { ::latest.isInitialized && latest.itemCount == 10 }
        val outgoing = latest
        compose.runOnIdle { pages.value = PagingData.from(emptyList()) }
        compose.waitForIdle()
        compose.waitUntil(5_000) { latest.itemCount == 0 }
        compose.runOnIdle {
            assertEquals(9, outgoing[9])
            assertEquals(9, outgoing.peek(9))
            assertNull(latest[0])
            pages.value = PagingData.from((100 until 110).toList())
        }
        compose.waitForIdle()
        compose.waitUntil(5_000) { latest.itemCount == 10 }
        compose.runOnIdle {
            assertEquals(0, outgoing[0])
            assertEquals(9, outgoing.peek(9))
            assertEquals(100, latest[0])
        }
    }

    @Test fun keyAndSectionPeeksDoNotPrefetchButVisibleCurrentRowsStillDo() {
        val loads = mutableListOf<Int>()
        val pages = Pager(PagingConfig(pageSize = 5, initialLoadSize = 5, prefetchDistance = 1, enablePlaceholders = false)) {
            object : PagingSource<Int, Int>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Int> {
                    val start = params.key ?: 0
                    loads += start
                    return LoadResult.Page((start until start + 5).toList(), null, if (start == 0) 5 else null)
                }
                override fun getRefreshKey(state: PagingState<Int, Int>): Int? = null
            }
        }.flow
        render(pages)
        compose.waitForIdle()
        compose.waitUntil(5_000) { ::latest.isInitialized && latest.itemCount == 5 }
        val outgoing = latest
        compose.runOnIdle {
            assertEquals(4, outgoing.peek(4))
            assertEquals(listOf(0), loads)
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(listOf(0), loads)
            assertEquals(4, outgoing[4])
        }
        compose.waitForIdle()
        compose.waitUntil(5_000) { latest.itemCount == 10 }
        compose.runOnIdle {
            assertEquals(listOf(0, 5), loads)
            assertEquals(4, outgoing[4])
            assertNull(outgoing[5])
            assertEquals(5, latest[5])
        }
    }
}
