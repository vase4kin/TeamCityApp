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
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.tracker.BuildDetailsTracker
import teamcityapp.features.build_details.presentation.resources.*
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

    @BeforeTest fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun handle(build: BuildLaunchData = incoming) = SavedStateHandle(mapOf(BuildDetailsViewModel.BUILD to Json.encodeToString(build), BuildDetailsViewModel.NAME to "Fallback"))
    private fun model(saved: SavedStateHandle = handle(), owner: String = "details") = BuildDetailsViewModel(saved, repository, tracker).also { store.put(owner, it) }
    private fun restored(saved: SavedStateHandle) = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })

    @Test fun legacyIntegerResourceNoticeDoesNotCrashRestoration() = runTest(dispatcher) {
        val saved = handle()
        saved["details.notice"] = """{"id":1,"message":2131950000,"openQueued":false}"""
        val model = model(saved)
        assertNull(model.state.value.notice)
        assertEquals(incoming, model.state.value.current)
    }

    @Test fun refreshedActionsDoNotChangeTheIncomingSectionSnapshot() = runTest(dispatcher) {
        val model = model()
        model.loaded(loaded)
        assertEquals(incoming, model.incoming)
        assertEquals("#loaded", model.state.value.title)
        assertEquals("Loaded configuration", model.state.value.subtitle)
        model.loaded(loaded.copy(id = "99"))
        assertEquals(loaded, model.state.value.current)
    }

    @Test fun processRestorationRetainsLoadedActions() = runTest(dispatcher) {
        val saved = handle()
        model(saved).apply {
            loaded(loaded)
        }
        val recreated = model(restored(saved), "restored")
        assertEquals(loaded, recreated.state.value.current)
    }

    @Test fun independentVisitsOfSameBuildKeepSeparateOwners() = runTest(dispatcher) {
        val first = model(handle(), "first")
        val second = model(handle(), "second")
        first.loaded(loaded)
        assertEquals(incoming, second.state.value.current)
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
        assertEquals(Res.string.text_stop_the_build_2, model.state.value.confirmation?.message)
        model.dismissConfirmation()
        model.loaded(loaded.copy(state = "queued"))
        model.requestCancellation()
        runCurrent()
        assertEquals(Res.string.text_remove_build_from_queue_2, model.state.value.confirmation?.message)
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
        assertEquals(BuildDetailsCompletion.Stopped, replacement.completion)
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

    @Test fun replacementCancellationFeedbackRestoresUntilConsumedAndDoesNotReplay() {
        BuildDetailsCompletion.entries.forEach { completion ->
            val saved = handle().apply { this[BuildDetailsViewModel.COMPLETION] = completion.name }
            val completed = model(saved, "completed-${completion.name}")
            val notice = requireNotNull(completed.state.value.notice)
            assertEquals(if (completion == BuildDetailsCompletion.Stopped) Res.string.text_build_is_stopped else Res.string.text_build_is_removed_from_queue, notice.message)
            assertNull(notice.actionLabel)
            val restoredSaved = restored(saved)
            val restoredModel = model(restoredSaved, "restored-${completion.name}")
            assertEquals(notice, restoredModel.state.value.notice)
            restoredModel.consumeNotice(notice.id)
            assertNull(model(restored(restoredSaved), "consumed-${completion.name}").state.value.notice)
        }
        assertTrue(cancellations.isEmpty())
    }

    @Test fun forbiddenCancellationDoesNotRefreshAndServerFailureDoes() = runTest(dispatcher) {
        val saved = handle()
        val model = model(saved)
        cancel = { BuildDetailsMutation.Forbidden }
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        assertEquals(Res.string.error_stop_build_forbidden_error, model.state.value.notice?.message)
        assertEquals(0L, model.state.value.overviewRefresh)
        cancel = { BuildDetailsMutation.Failed }
        model.requestCancellation()
        runCurrent()
        model.confirm(false)
        runCurrent()
        assertEquals(Res.string.error_base_stop_build_error, model.state.value.notice?.message)
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
        assertEquals(Res.string.text_build_is_restarted, model.state.value.notice?.message)
        assertEquals(Res.string.text_show_build, model.state.value.notice?.actionLabel)
        assertTrue(model.state.value.notice?.openQueued == true)
        val recreated = model(restored(saved), "restored")
        assertEquals(model.state.value.notice, recreated.state.value.notice)
        assertEquals("/queued", recreated.queuedBuildReference())
        assertTrue(queuedRequests.isEmpty())
        assertNull(recreated.state.value.replacement)
    }

    @Test fun forbiddenAndFailedRestartHaveDistinctMessagesAndNeverExposeQueuedAction() = runTest(dispatcher) {
        val model = model()
        model.loaded(loaded.copy(state = "finished"))
        restart = { BuildDetailsMutation.Forbidden }
        model.requestRestart()
        model.confirm(false)
        runCurrent()
        assertEquals(Res.string.error_restart_build_forbidden_error, model.state.value.notice?.message)
        restart = { BuildDetailsMutation.Failed }
        model.requestRestart()
        model.confirm(false)
        runCurrent()
        assertEquals(Res.string.error_base_restart_build_error, model.state.value.notice?.message)
        assertFalse(model.state.value.notice?.openQueued == true)
        assertNull(model.queuedBuildReference())
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
        assertEquals(Res.string.text_build_is_restarted, model.state.value.notice?.message)
        assertEquals(Res.string.text_show_build, model.state.value.notice?.actionLabel)
    }
}
