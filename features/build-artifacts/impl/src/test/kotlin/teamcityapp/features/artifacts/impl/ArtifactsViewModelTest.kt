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

package teamcityapp.features.artifacts.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import java.io.Serializable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.impl.tracker.ArtifactsTracker
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.list_state.ListUiState

@OptIn(ExperimentalCoroutinesApi::class)
class ArtifactsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val rows = listOf(Artifact("folder", "/folder", childrenHref = "/folder/children"), Artifact("build.zip", "/build.zip", 4096, "/content/build.zip"))
    private val file = ArtifactDownload("build.zip", "/content/build.zip")
    private val downloaded = DownloadedArtifact("/downloads/build.zip")
    private val build = BuildLaunchData("b1", "/build/b1", buildTypeId = "bt", artifacts = BuildCollectionLink("/artifacts", 2), properties = teamcityapp.libraries.builds.BuildProperties(emptyList()), tests = BuildTests("/tests", 5, 2, 1))
    private val repository = FakeRepository()
    private var shown = 0
    private val codec = object : BuildLaunchCodec {
        override fun encode(build: BuildLaunchData): Serializable = "legacy-build-payload"
        override fun decode(payload: Serializable): BuildLaunchData {
            assertEquals("legacy-build-payload", payload)
            return build
        }
    }

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun vm() = ArtifactsViewModel(
        SavedStateHandle(mapOf(ArtifactsNavigation.BUILD to codec.encode(build), ArtifactsNavigation.URL to "/artifacts", ArtifactsNavigation.NAME to "Artifacts")),
        repository,
        codec,
        object : ArtifactsTracker {
            override fun viewShown() {
                shown++
            }
        }
    ).also { store.put("artifacts", it) }
    private fun TestScope.observe(vm: ArtifactsViewModel) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    @Test fun firstCollectionLoadsOnceAndUsesTheCompleteBuildSnapshot() = runTest(dispatcher) {
        val vm = vm()
        runCurrent()
        assertTrue(repository.calls.isEmpty())
        assertEquals(build, vm.build)
        observe(vm)
        runCurrent()
        assertEquals(listOf("/artifacts" to false), repository.calls)
        assertEquals(ArtifactsUiState("Artifacts", ListUiState.Content(rows)), vm.state.value)
    }

    @Test fun resumedTrackingDoesNotLoadAgain() = runTest(dispatcher) {
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.onResumed()
        vm.onResumed()
        runCurrent()
        assertEquals(2, shown)
        assertEquals(1, repository.calls.size)
    }

    @Test fun emptyResponseIsExplicit() = runTest(dispatcher) {
        repository.load = { _, _ -> emptyList() }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Empty(), vm.state.value.list)
    }

    @Test fun initialErrorRetryBypassesCache() = runTest(dispatcher) {
        repository.load = { _, _ -> error("offline") }
        val vm = vm()
        observe(vm)
        runCurrent()
        assertEquals(ListUiState.Error, vm.state.value.list)
        repository.load = { _, _ -> rows }
        vm.retry()
        runCurrent()
        assertEquals(listOf("/artifacts" to false, "/artifacts" to true), repository.calls)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
    }

    @Test fun refreshFailureKeepsCompletedRows() = runTest(dispatcher) {
        val vm = vm()
        observe(vm)
        runCurrent()
        repository.load = { _, _ -> error("offline") }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Content(rows, refreshFailed = true), vm.state.value.list)
    }

    @Test fun failedRefreshRetainsCompletedEmptyResult() = runTest(dispatcher) {
        repository.load = { _, _ -> emptyList() }
        val vm = vm()
        observe(vm)
        runCurrent()
        repository.load = { _, _ -> error("offline") }
        vm.refresh()
        runCurrent()
        assertEquals(ListUiState.Empty(refreshFailed = true), vm.state.value.list)
    }

    @Test fun refreshingTheListDoesNotCancelAnExplicitDownload() = runTest(dispatcher) {
        val result = CompletableDeferred<DownloadedArtifact>()
        repository.save = { result.await() }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        vm.refresh()
        runCurrent()
        assertEquals(ArtifactDownloadState.Downloading(file), vm.state.value.download)
        result.complete(downloaded)
        runCurrent()
        assertTrue(vm.state.value.download is ArtifactDownloadState.Ready)
        assertEquals(listOf("/artifacts" to false, "/artifacts" to true), repository.calls)
    }

    @Test fun lastCollectorCancelsPendingListAndReturningRestartsIt() = runTest(dispatcher) {
        var cancelled = 0
        repository.load = { _, _ ->
            try {
                awaitCancellation()
            } finally {
                cancelled++
            }
        }
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        first.cancel()
        runCurrent()
        assertEquals(1, cancelled)
        observe(vm)
        runCurrent()
        assertEquals(2, repository.calls.size)
    }

    @Test fun completedListSurvivesAConfigurationSubscriptionGap() = runTest(dispatcher) {
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        first.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertEquals(1, repository.calls.size)
        assertEquals(ListUiState.Content(rows), vm.state.value.list)
    }

    @Test fun explicitDownloadContinuesAcrossAConfigurationSubscriptionGap() = runTest(dispatcher) {
        val result = CompletableDeferred<DownloadedArtifact>()
        repository.save = { result.await() }
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        assertEquals(ArtifactDownloadState.Downloading(file), vm.state.value.download)
        first.cancel()
        runCurrent()
        result.complete(downloaded)
        runCurrent()
        observe(vm)
        runCurrent()
        val ready = vm.state.value.download as ArtifactDownloadState.Ready
        assertEquals(downloaded, ready.file)
        assertEquals(listOf(file), repository.downloads)
    }

    @Test fun duplicateDownloadDuringPendingWorkIsIgnored() = runTest(dispatcher) {
        repository.save = { awaitCancellation() }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        vm.download(ArtifactDownload("other", "/other"))
        runCurrent()
        assertEquals(listOf(file), repository.downloads)
    }

    @Test fun downloadedFileCanBeClaimedOnlyOnceAcrossReplay() = runTest(dispatcher) {
        val vm = vm()
        val first = observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        val ready = vm.state.value.download as ArtifactDownloadState.Ready
        assertNull(vm.claimDownloadedFile(ready.token + 1))
        assertEquals(downloaded, vm.claimDownloadedFile(ready.token))
        first.cancel()
        runCurrent()
        observe(vm)
        runCurrent()
        assertNull(vm.claimDownloadedFile(ready.token))
        assertEquals(ArtifactDownloadState.Idle, vm.state.value.download)
    }

    @Test fun downloadFailureCanNotifyLegacyHostOnlyOnceAndRetryUsesTheSameOpaqueUrl() = runTest(dispatcher) {
        repository.save = { error("offline") }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        val failed = vm.state.value.download as ArtifactDownloadState.Failed
        assertTrue(vm.claimDownloadFailure(failed.token))
        assertFalse(vm.claimDownloadFailure(failed.token))
        repository.save = { downloaded }
        vm.retryDownload()
        runCurrent()
        assertEquals(listOf(file, file), repository.downloads)
        assertTrue(vm.state.value.download is ArtifactDownloadState.Ready)
    }

    @Test fun explicitCancelStopsWorkAndDoesNotBecomeFailure() = runTest(dispatcher) {
        var cancelled = false
        repository.save = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        vm.cancelDownload()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(ArtifactDownloadState.Idle, vm.state.value.download)
    }

    @Test fun destroyingTheViewModelCancelsPendingDownload() = runTest(dispatcher) {
        var cancelled = false
        repository.save = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(cancelled)
    }

    @Test fun obsoleteNoncancellableSuccessCannotOpenAFileAfterCancel() = runTest(dispatcher) {
        val result = CompletableDeferred<DownloadedArtifact>()
        repository.save = { withContext(NonCancellable) { result.await() } }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        vm.cancelDownload()
        runCurrent()
        result.complete(downloaded)
        runCurrent()
        assertEquals(ArtifactDownloadState.Idle, vm.state.value.download)
    }

    @Test fun obsoleteNoncancellableFailureCannotNotifyAfterCancel() = runTest(dispatcher) {
        val result = CompletableDeferred<Unit>()
        repository.save = {
            withContext(NonCancellable) {
                result.await()
                error("obsolete")
            }
        }
        val vm = vm()
        observe(vm)
        runCurrent()
        vm.download(file)
        runCurrent()
        vm.cancelDownload()
        runCurrent()
        result.complete(Unit)
        runCurrent()
        assertEquals(ArtifactDownloadState.Idle, vm.state.value.download)
    }
    private inner class FakeRepository : ArtifactsRepository {
        val calls = mutableListOf<Pair<String, Boolean>>()
        val downloads = mutableListOf<ArtifactDownload>()
        var load: suspend (String, Boolean) -> List<Artifact> = { _, _ -> rows }
        var save: suspend (ArtifactDownload) -> DownloadedArtifact = { downloaded }
        override suspend fun entries(url: String, forceRefresh: Boolean): List<Artifact> {
            calls += url to forceRefresh
            return load(url, forceRefresh)
        }
        override suspend fun download(file: ArtifactDownload): DownloadedArtifact {
            downloads += file
            return save(file)
        }
    }
}
