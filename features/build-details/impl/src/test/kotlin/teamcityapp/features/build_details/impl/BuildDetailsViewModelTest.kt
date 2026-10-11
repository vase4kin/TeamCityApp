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

package teamcityapp.features.build_details.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import org.junit.*
import org.junit.Assert.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.tracker.BuildDetailsTracker
import teamcityapp.libraries.builds.*

@OptIn(ExperimentalCoroutinesApi::class)
class BuildDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val incoming = BuildLaunchData("42", "/initial", number = "initial", state = "running", buildTypeId = "old", changes = BuildCollectionLink("/changes"), tests = BuildTests("/tests", 1, 2, 3, 6), properties = BuildProperties(null), snapshotDependencies = BuildCollectionLink(null))
    private val loaded = incoming.copy(href = "/loaded", number = "loaded", buildTypeId = "latest", configuration = BuildConfigurationData("latest", "Loaded configuration"), changes = null, tests = null, snapshotDependencies = null)
    private var own = true
    private var identity: suspend () -> Boolean = { own }
    private var cancel: suspend () -> BuildDetailsMutation<BuildLaunchData> = { BuildDetailsMutation.Success(incoming.copy(state = "finished")) }
    private var restart: suspend () -> BuildDetailsMutation<String> = { BuildDetailsMutation.Success("/queued") }
    private var queued: suspend () -> BuildLaunchData = { incoming.copy(id = "43", href = "/queued", state = "queued") }
    private val cancellations = mutableListOf<Pair<BuildLaunchData, Boolean>>()
    private val restarts = mutableListOf<BuildLaunchData>()
    private val queuedRequests = mutableListOf<String>()
    private val events = mutableListOf<String>()
    private val repository = object : BuildDetailsRepository {
        override suspend fun triggeredByCurrentUser(build: BuildLaunchData) = identity()
        override suspend fun cancel(build: BuildLaunchData, reAddToQueue: Boolean): BuildDetailsMutation<BuildLaunchData> {
            cancellations += build to reAddToQueue
            return cancel()
        }
        override suspend fun restart(build: BuildLaunchData): BuildDetailsMutation<String> {
            restarts += build
            return restart()
        }
        override suspend fun queuedBuild(href: String): BuildLaunchData {
            queuedRequests += href
            return queued()
        }
    }
    private val tracker = object : BuildDetailsTracker {
        override fun viewed() {
            events += "viewed"
        }
        override fun confirmedCancel(reAdd: Boolean) {
            events += "confirm:$reAdd"
        }
        override fun cancelSucceeded() {
            events += "cancel:success"
        }
        override fun cancelFailed(forbidden: Boolean) {
            events += "cancel:failed:$forbidden"
        }
        override fun restartSucceeded() {
            events += "restart:success"
        }
        override fun restartFailed(forbidden: Boolean) {
            events += "restart:failed:$forbidden"
        }
        override fun queuedOpened() {
            events += "queued:opened"
        }
        override fun queuedFailed() {
            events += "queued:failed"
        }
    }

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun handle(build: BuildLaunchData = incoming) = SavedStateHandle(mapOf(BuildDetailsViewModel.BUILD to Json.encodeToString(build), BuildDetailsViewModel.NAME to "Fallback"))
    private fun model(saved: SavedStateHandle = handle(), owner: String = "details") = BuildDetailsViewModel(saved, repository, tracker).also { store.put(owner, it) }
    private fun restored(saved: SavedStateHandle) = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })

    @Test fun refreshedActionsDoNotChangeIncomingTabsOrSelectedTab() = runTest(dispatcher) {
        val model = model()
        model.select(BuildDetailsTab.Tests)
        model.loaded(loaded)
        assertEquals(incoming, model.incoming)
        assertEquals(incoming.detailsTabs(), model.state.value.tabs)
        assertEquals(BuildDetailsTab.Tests, model.state.value.selected)
        assertEquals("#loaded", model.state.value.title)
        assertEquals("Loaded configuration", model.state.value.subtitle)
        model.loaded(loaded.copy(id = "99"))
        assertEquals(loaded, model.state.value.current)
    }

    @Test fun processRestorationRetainsLoadedActionsAndSelection() = runTest(dispatcher) {
        val saved = handle()
        model(saved).apply {
            loaded(loaded)
            select(BuildDetailsTab.Changes)
        }
        val recreated = model(restored(saved), "restored")
        assertEquals(loaded, recreated.state.value.current)
        assertEquals(incoming.detailsTabs(), recreated.state.value.tabs)
        assertEquals(BuildDetailsTab.Changes, recreated.state.value.selected)
    }

    @Test fun independentVisitsOfSameBuildKeepSeparateOwners() = runTest(dispatcher) {
        val first = model(handle(), "first")
        val second = model(handle(), "second")
        first.select(BuildDetailsTab.Tests)
        first.loaded(loaded)
        assertEquals(BuildDetailsTab.Overview, second.state.value.selected)
        assertEquals(incoming, second.state.value.current)
        assertTrue(second.state.value.counts.isEmpty())
    }

    @Test fun invalidSelectionCannotCreateAnAbsentTab() = runTest(dispatcher) {
        val model = model(handle(BuildLaunchData("1", "/b", state = "queued")))
        model.select(BuildDetailsTab.BuildLog)
        assertEquals(listOf(BuildDetailsTab.Overview, BuildDetailsTab.Parameters), model.state.value.tabs)
        assertEquals(BuildDetailsTab.Overview, model.state.value.selected)
        assertTrue(model.state.value.counts.isEmpty())
    }

    @Test fun cancellationConfirmsCurrentSnapshotAndIgnoresDuplicateSubmissions() = runTest(dispatcher) {
        val model = model()
        model.loaded(loaded)
        model.requestCancellation()
        runCurrent()
        assertEquals(loaded, model.state.value.confirmation?.build)
        model.loaded(loaded.copy(href = "/new-refresh"))
        cancel = { awaitCancellation() }
        model.confirm(true)
        model.confirm(false)
        model.requestCancellation()
        runCurrent()
        assertEquals(listOf(loaded to true), cancellations)
        assertEquals(BuildDetailsOperation.Stop, model.state.value.operation)
        assertEquals(listOf("confirm:true"), events)
    }

    @Test fun notYourBuildUsesCorrectStopAndRemoveMessagesAndRemoveNeverReAdds() = runTest(dispatcher) {
        own = false
        val model = model()
        model.requestCancellation()
        runCurrent()
        assertEquals(R.string.text_stop_the_build_2, model.state.value.confirmation?.message)
        model.dismissConfirmation()
        model.loaded(loaded.copy(state = "queued"))
        model.requestCancellation()
        runCurrent()
        assertEquals(R.string.text_remove_build_from_queue_2, model.state.value.confirmation?.message)
        model.confirm(true)
        runCurrent()
        assertFalse(cancellations.single().second)
    }

    @Test fun cancellationSuccessProducesOneRestorableReplacement() = runTest(dispatcher) {
        val saved = handle()
        val model = model(saved)
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        val replacement = requireNotNull(model.state.value.replacement)
        assertNull(model.state.value.operation)
        assertEquals("finished", replacement.build.state)
        val restoredSaved = restored(saved)
        val recreated = model(restoredSaved, "restored")
        assertEquals(replacement, recreated.state.value.replacement)
        recreated.consumeReplacement(replacement.id + 1)
        assertNotNull(recreated.state.value.replacement)
        recreated.consumeReplacement(replacement.id)
        assertNull(recreated.state.value.replacement)
        assertNull(model(restored(restoredSaved), "again").state.value.replacement)
        assertEquals(1, cancellations.size)
    }

    @Test fun forbiddenCancellationDoesNotRefreshAndServerFailureDoes() = runTest(dispatcher) {
        val saved = handle()
        val model = model(saved)
        cancel = { BuildDetailsMutation.Forbidden }
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        assertEquals(R.string.error_stop_build_forbidden_error, model.state.value.notice?.message)
        assertEquals(0L, model.state.value.overviewRefresh)
        cancel = { BuildDetailsMutation.Failed }
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        assertEquals(R.string.error_base_stop_build_error, model.state.value.notice?.message)
        assertEquals(1L, model.state.value.overviewRefresh)
        assertNull(model.state.value.operation)
        assertEquals(1L, model(restored(saved), "restored-refresh").state.value.overviewRefresh)
    }

    @Test fun restartUsesRefreshedConfigurationAndRestorableQueuedHref() = runTest(dispatcher) {
        val saved = handle()
        val model = model(saved)
        val finished = loaded.copy(state = "finished")
        model.loaded(finished)
        model.requestRestart()
        model.confirm(false)
        model.confirm(false)
        runCurrent()
        assertEquals(listOf(finished), restarts)
        assertEquals(R.string.text_build_is_restarted, model.state.value.notice?.message)
        assertTrue(model.state.value.notice?.openQueued == true)
        val recreated = model(restored(saved), "restored")
        assertEquals(model.state.value.notice, recreated.state.value.notice)
        recreated.openQueued()
        recreated.openQueued()
        runCurrent()
        assertEquals(listOf("/queued"), queuedRequests)
        assertEquals("43", recreated.state.value.replacement?.build?.id)
        recreated.openQueued()
        runCurrent()
        assertEquals(1, queuedRequests.size)
    }

    @Test fun queuedLoadFailureRetainsRetryAndDoesNotReplaceCurrentBuild() = runTest(dispatcher) {
        val model = model()
        model.loaded(loaded.copy(state = "finished"))
        model.requestRestart()
        model.confirm(false)
        runCurrent()
        queued = { error("offline") }
        model.openQueued()
        runCurrent()
        assertEquals(R.string.error_opening_build, model.state.value.notice?.message)
        assertTrue(model.state.value.notice?.openQueued == true)
        assertNull(model.state.value.replacement)
        queued = { loaded.copy(id = "next") }
        model.openQueued()
        runCurrent()
        assertEquals("next", model.state.value.replacement?.build?.id)
    }

    @Test fun forbiddenAndFailedRestartHaveDistinctMessagesAndNeverExposeQueuedAction() = runTest(dispatcher) {
        val model = model()
        model.loaded(loaded.copy(state = "finished"))
        restart = { BuildDetailsMutation.Forbidden }
        model.requestRestart()
        model.confirm(false)
        runCurrent()
        assertEquals(R.string.error_restart_build_forbidden_error, model.state.value.notice?.message)
        restart = { BuildDetailsMutation.Failed }
        model.requestRestart()
        model.confirm(false)
        runCurrent()
        assertEquals(R.string.error_base_restart_build_error, model.state.value.notice?.message)
        assertFalse(model.state.value.notice?.openQueued == true)
        model.openQueued()
        runCurrent()
        assertTrue(queuedRequests.isEmpty())
    }

    @Test fun removingOwnerCancelsPendingMutationWithoutAnErrorOrSuccess() = runTest(dispatcher) {
        var cancelled = false
        cancel = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val model = model()
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(cancelled)
        assertNull(model.state.value.notice)
        assertNull(model.state.value.replacement)
        assertEquals(listOf("confirm:false"), events)
    }

    @Test fun processRestorationDoesNotAutomaticallyRepeatPendingMutation() = runTest(dispatcher) {
        cancel = { awaitCancellation() }
        val saved = handle()
        val model = model(saved)
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        val recreated = model(restored(saved), "restored")
        runCurrent()
        assertFalse(recreated.state.value.busy)
        assertNull(recreated.state.value.confirmation)
        assertEquals(1, cancellations.size)
    }

    @Test fun confirmationSurvivesRestorationWithoutSubmittingUntilExplicitlyConfirmed() = runTest(dispatcher) {
        val saved = handle()
        val model = model(saved)
        model.requestCancellation()
        runCurrent()
        val recreated = model(restored(saved), "restored")
        runCurrent()
        assertEquals(model.state.value.confirmation, recreated.state.value.confirmation)
        assertTrue(cancellations.isEmpty())
        recreated.confirm(false)
        runCurrent()
        assertEquals(1, cancellations.size)
    }

    @Test fun noticeConsumptionCannotDismissANewerNotice() = runTest(dispatcher) {
        cancel = { BuildDetailsMutation.Failed }
        val model = model()
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        val first = requireNotNull(model.state.value.notice)
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        model.consumeNotice(first.id)
        assertNotNull(model.state.value.notice)
        model.consumeNotice(requireNotNull(model.state.value.notice).id)
        assertNull(model.state.value.notice)
    }

    @Test fun cancelledIdentityCheckNeverOpensAConfirmationOrError() = runTest(dispatcher) {
        identity = { awaitCancellation() }
        val model = model()
        model.requestCancellation()
        model.requestCancellation()
        runCurrent()
        assertTrue(model.state.value.preparingConfirmation)
        store.clear()
        runCurrent()
        assertNull(model.state.value.confirmation)
        assertNull(model.state.value.notice)
    }

    @Test fun losingCollectorsDuringRecreationDoesNotCancelOrRepeatASubmission() = runTest(dispatcher) {
        val response = CompletableDeferred<BuildDetailsMutation<String>>()
        restart = { response.await() }
        val model = model()
        model.loaded(loaded.copy(state = "finished"))
        val collection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect {} }
        model.requestRestart()
        model.confirm(false)
        runCurrent()
        collection.cancel()
        runCurrent()
        assertEquals(BuildDetailsOperation.Restart, model.state.value.operation)
        response.complete(BuildDetailsMutation.Success("/retained"))
        runCurrent()
        assertEquals(1, restarts.size)
        assertEquals(R.string.text_build_is_restarted, model.state.value.notice?.message)
    }
}
