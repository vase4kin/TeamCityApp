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

package teamcityapp.features.favorites.impl

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.favorites.api.FavoriteConfigurations
import teamcityapp.features.favorites.api.FavoritesRepository
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val row = BuildConfigurationSummary("1", "Android", null, ProjectReference("project", "Project"))
    private val batch = FavoriteConfigurations(listOf("1"), listOf(row))
    private val calls = mutableListOf<Boolean>()
    private var load: suspend () -> FavoriteConfigurations = { batch }
    private val repository = object : FavoritesRepository {
        override suspend fun favorites(forceRefresh: Boolean): FavoriteConfigurations {
            calls += forceRefresh
            return load()
        }
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun viewModel() = FavoritesViewModel(repository).also { store.put("favorites", it) }
    private fun TestScope.observe(vm: FavoritesViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun collectionOwnsInitialLoadAndFirstResumeDoesNotDuplicateIt() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        runCurrent()
        assertTrue(calls.isEmpty())
        observe(vm)
        runCurrent()
        assertEquals(listOf(false), calls)
        assertEquals(FavoritesUiState(ListUiState.Content(listOf(row)), savedIds = listOf("1")), vm.state.value)
    }

    @Test fun noSavedFavoritesIsEmptyWithoutFailure() = runTest(dispatcher) {
        load = { FavoriteConfigurations(emptyList(), emptyList()) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(FavoritesUiState(ListUiState.Empty()), vm.state.value)
    }

    @Test fun partialFetchKeepsRowsUnavailableIdsAndSavedIdsTogether() = runTest(dispatcher) {
        load = { FavoriteConfigurations(listOf("1", "missing"), listOf(row), listOf("missing")) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(FavoritesUiState(ListUiState.Content(listOf(row)), FavoritesFailure.Partial(listOf("missing")), listOf("1", "missing")), vm.state.value)
    }

    @Test fun configurationsSortByProjectIdIgnoringCaseWithStableOrderWithinProject() = runTest(dispatcher) {
        val first = row.copy(id = "2", project = ProjectReference("z", "Same title"))
        val second = row.copy(id = "3", project = ProjectReference("A", "Same title"))
        val third = row.copy(id = "4", project = second.project)
        load = { FavoriteConfigurations(listOf("2", "3", "4"), listOf(first, second, third)) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Content(listOf(second, third, first)), vm.state.value.list)
    }

    @Test fun allSavedFavoritesFailAsErrorRatherThanEmptyAndKeepSavedIds() = runTest(dispatcher) {
        load = { FavoriteConfigurations(listOf("missing"), emptyList(), listOf("missing")) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        assertEquals(FavoritesUiState(ListUiState.Error, FavoritesFailure.AllFailed, listOf("missing")), vm.state.value)
    }

    @Test fun retryAfterAllFailedUsesForceRefreshAndRestoresContent() = runTest(dispatcher) {
        load = { FavoriteConfigurations(listOf("1"), emptyList(), listOf("1")) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        load = { batch }
        vm.retry()
        runCurrent()
        assertEquals(listOf(false, true), calls)
        assertEquals(FavoritesFailure.None, vm.state.value.failure)
        assertNull(vm.state.value.refreshFailureMessageRes)
        assertEquals(ListUiState.Content(listOf(row)), vm.state.value.list)
    }

    @Test fun pendingRetryAfterAllFailedRetainsSavedIds() = runTest(dispatcher) {
        load = { FavoriteConfigurations(listOf("missing"), emptyList(), listOf("missing")) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        load = { awaitCancellation() }
        vm.retry()
        runCurrent()
        assertEquals(ListUiState.Loading, vm.state.value.list)
        assertEquals(listOf("missing"), vm.state.value.savedIds)
        assertEquals(listOf(false, true), calls)
    }

    @Test fun refreshFailureKeepsLastRowsAndLatestFailedSavedSnapshot() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        load = { FavoriteConfigurations(listOf("1", "2"), emptyList(), listOf("1", "2")) }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(listOf(row), refreshFailed = true), vm.state.value.list)
        assertEquals(FavoritesFailure.AllFailed, vm.state.value.failure)
        assertEquals(R.string.favorites_all_failed, vm.state.value.refreshFailureMessageRes)
        assertEquals(listOf("1", "2"), vm.state.value.savedIds)
    }

    @Test fun genericRefreshFailureAlsoKeepsPriorRowsAndSavedIds() = runTest(dispatcher) {
        val vm = viewModel()
        observe(vm)
        runCurrent()
        load = { error("storage unavailable") }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(listOf(row), refreshFailed = true), vm.state.value.list)
        assertEquals(listOf("1"), vm.state.value.savedIds)
        assertEquals(FavoritesFailure.AllFailed, vm.state.value.failure)
        assertEquals(R.string.favorites_all_failed, vm.state.value.refreshFailureMessageRes)
    }

    @Test fun refreshingPartialRowsRetainsMetadataUntilResultCompletes() = runTest(dispatcher) {
        load = { batch.copy(savedIds = listOf("1", "missing"), unavailableIds = listOf("missing")) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        val result = CompletableDeferred<FavoriteConfigurations>()
        load = { result.await() }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(listOf(row), isRefreshing = true), vm.state.value.list)
        assertEquals(FavoritesFailure.Partial(listOf("missing")), vm.state.value.failure)
        result.complete(batch)
        runCurrent()
        assertEquals(FavoritesFailure.None, vm.state.value.failure)
        assertNull(vm.state.value.refreshFailureMessageRes)
        assertEquals(listOf("1"), vm.state.value.savedIds)
    }

    @Test fun emptyRefreshFailureStaysEmptyWithExplicitFailure() = runTest(dispatcher) {
        load = { FavoriteConfigurations(emptyList(), emptyList()) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        load = { error("offline") }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Empty(refreshFailed = true), vm.state.value.list)
        assertEquals(FavoritesFailure.AllFailed, vm.state.value.failure)
        assertEquals(R.string.favorites_all_failed, vm.state.value.refreshFailureMessageRes)
    }

    @Test fun allFailedAfterEmptyRetainsEmptySurfaceAndNewSavedIds() = runTest(dispatcher) {
        load = { FavoriteConfigurations(emptyList(), emptyList()) }
        val vm = viewModel()
        observe(vm)
        runCurrent()
        load = { FavoriteConfigurations(listOf("new"), emptyList(), listOf("new")) }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Empty(refreshFailed = true), vm.state.value.list)
        assertEquals(FavoritesFailure.AllFailed, vm.state.value.failure)
        assertEquals(R.string.favorites_all_failed, vm.state.value.refreshFailureMessageRes)
        assertEquals(listOf("new"), vm.state.value.savedIds)
    }

    @Test fun lastCollectorLeavingCancelsPendingLoadWithoutUnavailableFailure() = runTest(dispatcher) {
        var cancelled = false
        load = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        collector.cancel()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(FavoritesUiState(), vm.state.value)
        load = { batch }
        observe(vm)
        runCurrent()
        assertEquals(2, calls.size)
        assertEquals(ListUiState.Content(listOf(row)), vm.state.value.list)
    }

    @Test fun cancelledRepositoryReturningFailedIdsCannotPublishFailureMetadata() = runTest(dispatcher) {
        val vm = viewModel()
        val collector = observe(vm)
        runCurrent()
        load = {
            try {
                awaitCancellation()
            } catch (_: CancellationException) {
                FavoriteConfigurations(listOf("obsolete"), emptyList(), listOf("obsolete"))
            }
        }
        vm.refresh()
        runCurrent()
        collector.cancel()
        runCurrent()
        load = { error("offline") }
        observe(vm)
        runCurrent()
        assertEquals(listOf("1"), vm.state.value.savedIds)
        assertEquals(ListUiState.Content(listOf(row), refreshFailed = true), vm.state.value.list)
    }

    @Test fun visibleConfigurationRecreationRetainsCompletedRowsWithoutReload() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation(wasVisible = true)
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(listOf(false), calls)
    }

    @Test fun hiddenConfigurationRecreationStillReloadsOnReturn() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        vm.onConfigurationRecreation(wasVisible = false)
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(listOf(false, false), calls)
    }

    @Test fun realReturnReloadsCurrentFavoriteSnapshotUsingNormalCachePolicy() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onResumed()
        val collector = observe(vm)
        runCurrent()
        vm.onPaused()
        collector.cancel()
        runCurrent()
        val another = row.copy(id = "other")
        load = { FavoriteConfigurations(listOf("other"), listOf(another)) }
        vm.onResumed()
        observe(vm)
        runCurrent()
        assertEquals(listOf(false, false), calls)
        assertEquals(FavoritesUiState(ListUiState.Content(listOf(another)), savedIds = listOf("other")), vm.state.value)
    }
}
