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

package teamcityapp.features.run_build.impl

import androidx.lifecycle.SavedStateHandle
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import teamcityapp.features.run_build.api.*
import teamcityapp.features.run_build.impl.resources.*
import teamcityapp.features.run_build.impl.tracker.RunBuildTracker
@OptIn(ExperimentalCoroutinesApi::class)
class RunBuildViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var branchValues = listOf("main")
    private var agentValues = listOf(BuildAgent("1", "Linux"))
    private var failBranches = false
    private var failAgents = false
    private var result: QueueBuildResult = QueueBuildResult.Success("/queue/1")
    private var pending: CompletableDeferred<QueueBuildResult>? = null
    private val queued = mutableListOf<BuildRequest>()
    private val repository = object : RunBuildRepository {
        override suspend fun branches(buildTypeId: String): List<String> {
            if (failBranches) error("branches")
            return branchValues
        }
        override suspend fun agents(buildTypeId: String): List<BuildAgent> {
            if (failAgents) error("agents")
            return agentValues
        }
        override suspend fun queue(request: BuildRequest): QueueBuildResult {
            queued += request
            return pending?.await() ?: result
        }
    }

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest fun cleanup() {
        Dispatchers.resetMain()
    }
    private fun vm(scope: TestScope): RunBuildViewModel {
        val vm = RunBuildViewModel(repository, SavedStateHandle(mapOf(BUILD_TYPE_ID to "Build")), TestRunBuildTracker)
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { vm.state.collect() }
        return vm
    }

    @Test fun singleBranchIsSelectedAndDefaultFlagsArePreserved() = runTest(dispatcher) {
        val vm = vm(this)
        runCurrent()
        assertEquals("main", vm.state.value.request.branch)
        assertTrue(vm.state.value.request.cleanSources)
        assertFalse(vm.state.value.request.personal)
        assertFalse(vm.state.value.request.queueAtTop)
        assertEquals(agentValues, vm.state.value.agents)
        assertEquals(Res.string.hint_default_filter_agent, vm.state.value.agentFallbackRes)
    }

    @Test fun multipleBranchesLeaveTheDefaultAndDoNotForceAgentSelection() = runTest(dispatcher) {
        branchValues = listOf("main", "release")
        val vm = vm(this)
        runCurrent()
        assertEquals("", vm.state.value.request.branch)
        assertNull(vm.state.value.request.agent)
    }

    @Test fun optionalSectionFailuresAreIndependent() = runTest(dispatcher) {
        failBranches = true
        val vm = vm(this)
        runCurrent()
        assertTrue(vm.state.value.branchesFailed)
        assertEquals(Res.string.branches_unavailable, vm.state.value.branchesMessageRes)
        assertEquals(agentValues, vm.state.value.agents)
        assertEquals(Res.string.hint_default_filter_agent, vm.state.value.agentFallbackRes)
        assertFalse(vm.state.value.agentsFailed)
    }

    @Test fun agentFailuresLeaveBranchContentAvailable() = runTest(dispatcher) {
        failAgents = true
        val vm = vm(this)
        runCurrent()
        assertTrue(vm.state.value.agentsFailed)
        assertEquals(branchValues, vm.state.value.branches)
    }

    @Test fun queuePreservesEveryOptionAndParameterAndConsumesResultOnce() = runTest(dispatcher) {
        val vm = vm(this)
        runCurrent()
        val request = BuildRequest("Wrong", "release", agentValues.single(), true, true, false, listOf(BuildParameter("env", "prod")))
        vm.update(request)
        vm.queue()
        runCurrent()
        assertEquals(request.copy(buildTypeId = "Build"), queued.single())
        assertEquals(Res.string.option_on, vm.state.value.personalLabelRes)
        assertEquals(Res.string.priority_top, vm.state.value.priorityLabelRes)
        assertEquals(Res.string.option_off, vm.state.value.cleanSourcesLabelRes)
        assertEquals(agentValues.single().name, vm.state.value.selectedAgentName)
        assertEquals("/queue/1", vm.state.value.queuedHref)
        assertTrue(vm.consumeSuccess("/queue/1"))
        assertFalse(vm.consumeSuccess("/queue/1"))
        vm.queue()
        runCurrent()
        assertEquals(1, queued.size)
    }

    @Test fun concurrentSubmissionsAreIgnoredAndInputsAreFrozen() = runTest(dispatcher) {
        pending = CompletableDeferred()
        val vm = vm(this)
        runCurrent()
        vm.queue()
        vm.queue()
        runCurrent()
        vm.update(BuildRequest("Build", "changed"))
        runCurrent()
        assertEquals(1, queued.size)
        assertEquals(Res.string.text_queueing_build, vm.state.value.queueLabelRes)
        assertEquals("main", vm.state.value.request.branch)
        pending!!.complete(result)
        runCurrent()
    }

    @Test fun forbiddenAndGeneralFailuresStayOnScreenAndAllowRetry() = runTest(dispatcher) {
        val vm = vm(this)
        runCurrent()
        result = QueueBuildResult.Forbidden
        vm.queue()
        runCurrent()
        assertEquals(result, vm.state.value.queueError)
        assertEquals(Res.string.error_forbidden_error, vm.state.value.queueErrorMessageRes)
        assertFalse(vm.state.value.queuing)
        result = QueueBuildResult.Error
        vm.queue()
        runCurrent()
        assertEquals(result, vm.state.value.queueError)
        assertEquals(Res.string.error_base_error, vm.state.value.queueErrorMessageRes)
        result = QueueBuildResult.Success("/queue/2")
        vm.queue()
        runCurrent()
        assertEquals("/queue/2", vm.state.value.queuedHref)
        assertNull(vm.state.value.queueErrorMessageRes)
        assertEquals(Res.string.title_run_build, vm.state.value.queueLabelRes)
    }

    @Test fun dismissingAnEarlierSnackbarDoesNotClearARepeatedFailure() = runTest(dispatcher) {
        result = QueueBuildResult.Forbidden
        val vm = vm(this)
        runCurrent()
        vm.queue()
        runCurrent()
        val firstErrorId = vm.state.value.queueErrorId
        vm.queue()
        runCurrent()
        val secondErrorId = vm.state.value.queueErrorId
        assertTrue(secondErrorId > firstErrorId)
        vm.clearError(firstErrorId)
        runCurrent()
        assertEquals(QueueBuildResult.Forbidden, vm.state.value.queueError)
        vm.clearError(secondErrorId)
        runCurrent()
        assertNull(vm.state.value.queueError)
        assertEquals("main", vm.state.value.request.branch)
    }

    @Test fun parametersAllowEmptyValuesAndDuplicateNamesAndClearTogether() = runTest(dispatcher) {
        val vm = vm(this)
        runCurrent()
        assertFalse(vm.addParameter("", "value"))
        assertTrue(vm.addParameter("env", ""))
        assertTrue(vm.addParameter("env", "prod"))
        runCurrent()
        assertEquals(listOf(BuildParameter("env", ""), BuildParameter("env", "prod")), vm.state.value.request.parameters)
        vm.clearParameters()
        runCurrent()
        assertTrue(vm.state.value.request.parameters.isEmpty())
    }

    @Test fun subscriptionLossCancelsBothLoadsAndRetriesOnCollection() = runTest(dispatcher) {
        var started = 0
        var cancelled = 0
        val repo = object : RunBuildRepository {
            override suspend fun branches(buildTypeId: String): List<String> {
                started++
                try {
                    awaitCancellation()
                } finally {
                    cancelled++
                }
            }
            override suspend fun agents(buildTypeId: String): List<BuildAgent> {
                started++
                try {
                    awaitCancellation()
                } finally {
                    cancelled++
                }
            }
            override suspend fun queue(request: BuildRequest) = QueueBuildResult.Error
        }
        val vm = RunBuildViewModel(repo, SavedStateHandle(), TestRunBuildTracker)
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(2, started)
        collector.cancel()
        runCurrent()
        assertEquals(2, cancelled)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(4, started)
    }
}

private object TestRunBuildTracker : RunBuildTracker {
    override fun trackView() {}
    override fun trackUserRunBuildSuccess() {}
    override fun trackUserRunBuildWithCustomParamsSuccess() {}
    override fun trackUserRunBuildFailed() {}
    override fun trackUserRunBuildFailedForbidden() {}
    override fun trackUserClicksOnAddNewBuildParamButton() {}
    override fun trackUserClicksOnClearAllBuildParamsButton() {}
    override fun trackUserAddsBuildParam() {}
}
