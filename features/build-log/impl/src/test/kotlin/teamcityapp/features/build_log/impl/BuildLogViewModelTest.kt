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

package teamcityapp.features.build_log.impl
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import teamcityapp.features.build_log.api.*
@OptIn(ExperimentalCoroutinesApi::class)
class BuildLogViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var session = BuildLogSession("https://server.example/log", needsAuthentication = true)
    private var failLoad = false
    private var failAck = false
    private var calls = 0
    private var acknowledgements = 0
    private val repo = object : BuildLogRepository {
        override suspend fun session(buildId: String): BuildLogSession {
            calls++
            if (failLoad) error("session")
            return session
        }
        override suspend fun acknowledgeAuthentication() {
            acknowledgements++
            if (failAck) error("consent")
        }
    }

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun cleanup() {
        Dispatchers.resetMain()
    }
    private fun vm(scope: TestScope): BuildLogViewModel {
        val vm = BuildLogViewModel(repo, SavedStateHandle(mapOf("buildId" to "123")))
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { vm.state.collect() }
        return vm
    }

    @Test fun consentIsAcknowledgedOnceThenTheWebPageLoads() = runTest(dispatcher) {
        val vm = vm(this)
        runCurrent()
        assertTrue((vm.state.value as BuildLogUiState.Session).session.needsAuthentication)
        assertEquals(R.string.text_login_again, (vm.state.value as BuildLogUiState.Session).messageRes)
        assertEquals(R.string.text_button_login, (vm.state.value as BuildLogUiState.Session).actionLabelRes)
        vm.authenticate()
        vm.authenticate()
        runCurrent()
        assertEquals(1, acknowledgements)
        assertFalse((vm.state.value as BuildLogUiState.Session).session.needsAuthentication)
        assertNull((vm.state.value as BuildLogUiState.Session).messageRes)
        assertNull((vm.state.value as BuildLogUiState.Session).actionLabelRes)
    }

    @Test fun consentSaveFailureIsExplicitAndCanBeRetried() = runTest(dispatcher) {
        failAck = true
        val vm = vm(this)
        runCurrent()
        vm.authenticate()
        runCurrent()
        assertTrue((vm.state.value as BuildLogUiState.Session).authenticationFailed)
        assertEquals(R.string.log_authentication_error, (vm.state.value as BuildLogUiState.Session).messageRes)
        failAck = false
        vm.authenticate()
        runCurrent()
        assertFalse((vm.state.value as BuildLogUiState.Session).authenticationFailed)
        assertFalse((vm.state.value as BuildLogUiState.Session).session.needsAuthentication)
        assertNull((vm.state.value as BuildLogUiState.Session).messageRes)
        assertNull((vm.state.value as BuildLogUiState.Session).actionLabelRes)
    }

    @Test fun pageFinishedNeverHidesAnErrorAndRetryStartsANewAttempt() = runTest(dispatcher) {
        session = session.copy(needsAuthentication = false)
        val vm = vm(this)
        runCurrent()
        vm.pageStarted()
        vm.pageFailed()
        vm.pageFinished()
        runCurrent()
        assertEquals(BuildLogPage.Error, (vm.state.value as BuildLogUiState.Session).page)
        vm.retry()
        runCurrent()
        assertEquals(1, (vm.state.value as BuildLogUiState.Session).attempt)
        assertEquals(BuildLogPage.Loading, (vm.state.value as BuildLogUiState.Session).page)
        vm.pageFinished()
        runCurrent()
        assertEquals(BuildLogPage.Content, (vm.state.value as BuildLogUiState.Session).page)
    }

    @Test fun failedSessionCanBeRetried() = runTest(dispatcher) {
        failLoad = true
        val vm = vm(this)
        runCurrent()
        assertEquals(BuildLogUiState.Error, vm.state.value)
        failLoad = false
        vm.retry()
        runCurrent()
        assertTrue(vm.state.value is BuildLogUiState.Session)
    }

    @Test fun completedPolicySurvivesCollectorRecreation() = runTest(dispatcher) {
        val vm = BuildLogViewModel(repo, SavedStateHandle())
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        collector.cancel()
        runCurrent()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(1, calls)
    }

    @Test fun pendingPolicyCancelsWithTheLastCollectorAndRestarts() = runTest(dispatcher) {
        var started = 0
        var cancelled = 0
        val vm = BuildLogViewModel(
            object : BuildLogRepository {
                override suspend fun session(buildId: String): BuildLogSession {
                    started++
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled++
                    }
                }
                override suspend fun acknowledgeAuthentication() {}
            },
            SavedStateHandle()
        )
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        collector.cancel()
        runCurrent()
        assertEquals(1, cancelled)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(2, started)
    }
}
