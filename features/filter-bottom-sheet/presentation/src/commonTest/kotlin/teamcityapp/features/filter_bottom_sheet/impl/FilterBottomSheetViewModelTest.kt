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

package teamcityapp.features.filter_bottom_sheet.impl
import androidx.lifecycle.SavedStateHandle
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import teamcityapp.features.filter_bottom_sheet.api.*
import teamcityapp.features.filter_bottom_sheet.presentation.resources.*
@OptIn(ExperimentalCoroutinesApi::class)
class FilterBottomSheetViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test fun everyFilterAppliesItsOppositeAndOnlyClosesOnce() = runTest(dispatcher) {
        for (filter in QuickFilter.entries) {
            val calls = mutableListOf<QuickFilter>()
            val vm = FilterBottomSheetViewModel(
                object : QuickFilterRepository {
                    override suspend fun apply(filter: QuickFilter) {
                        calls += filter
                    }
                },
                SavedStateHandle(mapOf("arg_code" to filter.ordinal))
            )
            val expected = listOf(
                Triple(Res.string.title_filter_running_builds, Res.string.text_show_favorites, Res.string.selected_all),
                Triple(Res.string.title_filter_running_builds, Res.string.text_show_running, Res.string.selected_favorites),
                Triple(Res.string.title_filter_queued_builds, Res.string.text_show_favorites, Res.string.selected_all),
                Triple(Res.string.title_filter_queued_builds, Res.string.text_show_queued, Res.string.selected_favorites),
                Triple(Res.string.title_filter_agents, Res.string.text_show_disconnected, Res.string.selected_connected),
                Triple(Res.string.title_filter_agents, Res.string.text_show_connected, Res.string.selected_disconnected)
            )[filter.ordinal]
            assertEquals(expected, Triple(vm.state.value.titleRes, vm.state.value.descriptionRes, vm.state.value.selectedRes))
            vm.apply()
            vm.apply()
            runCurrent()
            assertEquals(listOf(filter.opposite()), calls)
            assertTrue(vm.state.value.applied)
            assertTrue(vm.consumeApplied())
            assertFalse(vm.consumeApplied())
            vm.apply()
            runCurrent()
            assertEquals(listOf(filter.opposite()), calls)
        }
    }

    @Test fun failureCanBeRetriedWithoutClosing() = runTest(dispatcher) {
        var fail = true
        val vm = FilterBottomSheetViewModel(
            object : QuickFilterRepository {
                override suspend fun apply(filter: QuickFilter) {
                    if (fail) error("Failed")
                }
            },
            SavedStateHandle()
        )
        vm.apply()
        runCurrent()
        assertTrue(vm.state.value.failed)
        assertFalse(vm.state.value.applied)
        fail = false
        vm.apply()
        runCurrent()
        assertFalse(vm.state.value.failed)
        assertTrue(vm.state.value.applied)
    }

    @Test fun invalidFilterArgumentsFallbackSafely() {
        assertEquals(
            QuickFilter.RunningAll,
            FilterBottomSheetViewModel(
                object : QuickFilterRepository {
                    override suspend fun apply(filter: QuickFilter) {}
                },
                SavedStateHandle(mapOf("arg_code" to 99))
            ).state.value.filter
        )
    }
}
