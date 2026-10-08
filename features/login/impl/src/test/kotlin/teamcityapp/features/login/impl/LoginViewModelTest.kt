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

package teamcityapp.features.login.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.mock
import teamcityapp.features.login.api.*
import teamcityapp.libraries.authentication.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val tracker = mock(LoginTracker::class.java)
    private val calls = mutableListOf<Triple<AccountCredentials, Boolean, Boolean>>()
    private var result: AuthenticationResult = AuthenticationResult.Success
    private var pending: CompletableDeferred<AuthenticationResult>? = null
    private val authentication = object : AuthenticationRepository {
        override suspend fun signIn(credentials: AccountCredentials, checkSecureConnection: Boolean, checkDuplicate: Boolean): AuthenticationResult {
            calls += Triple(credentials, checkSecureConnection, checkDuplicate)
            return pending?.await() ?: result
        }
    }

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun cleanup() {
        Dispatchers.resetMain()
    }
    private fun viewModel(
        scope: TestScope,
        demo: LoginRepository = object : LoginRepository {
            override suspend fun demoServer() = DemoServer(true, "https://demo.example")
        }
    ): LoginViewModel {
        val vm = LoginViewModel(authentication, demo, tracker)
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { vm.state.collect() }
        return vm
    }

    @Test fun validatesEveryRequiredFieldBeforeAuthentication() = runTest(dispatcher) {
        val vm = viewModel(this)
        runCurrent()
        for ((form, error) in listOf(AuthenticationFormState(serverUrl = " ", guest = true) to AuthenticationError.EmptyUrl, AuthenticationFormState() to AuthenticationError.EmptyUserName, AuthenticationFormState(userName = "user") to AuthenticationError.EmptyPassword)) {
            vm.update(form)
            vm.submit()
            runCurrent()
            assertEquals(error, vm.state.value.form.error)
        }
        assertTrue(calls.isEmpty())
    }

    @Test fun trimsCredentialsAndChecksHttpOnFirstLogin() = runTest(dispatcher) {
        val vm = viewModel(this)
        runCurrent()
        vm.update(AuthenticationFormState(" https://server.example/path ", " user ", " pass ", sslDisabled = true))
        vm.submit()
        runCurrent()
        assertEquals(Triple(AccountCredentials("https://server.example/path", "user", "pass", sslDisabled = true), true, false), calls.single())
        assertTrue(vm.state.value.signedIn)
        assertEquals("", vm.state.value.form.password)
        assertTrue(vm.consumeSuccess())
        assertFalse(vm.consumeSuccess())
    }

    @Test fun guestLoginDoesNotRequireUserNameOrPassword() = runTest(dispatcher) {
        val vm = viewModel(this)
        runCurrent()
        vm.update(AuthenticationFormState("https://server.example", guest = true))
        vm.submit()
        runCurrent()
        assertTrue(calls.single().first.guest)
        assertTrue(vm.state.value.signedIn)
    }

    @Test fun httpConfirmationRetainsOriginalRequestAndDeclineDoesNotRetry() = runTest(dispatcher) {
        result = AuthenticationResult.Failure(-1, "HTTP")
        val vm = viewModel(this)
        runCurrent()
        vm.update(AuthenticationFormState("http://server.example", "user", "pass"))
        vm.submit()
        runCurrent()
        assertTrue(vm.state.value.httpConfirmation)
        assertFalse(vm.state.value.form.busy)
        vm.update(AuthenticationFormState("https://other.example", guest = true))
        runCurrent()
        result = AuthenticationResult.Success
        vm.confirmHttp()
        runCurrent()
        assertEquals("http://server.example", calls.last().first.serverUrl)
        assertFalse(calls.last().second)
        vm.declineHttp()
        vm.confirmHttp()
        assertEquals(2, calls.size)
    }

    @Test fun decliningHttpClearsPendingCredentials() = runTest(dispatcher) {
        result = AuthenticationResult.Failure(-1, "HTTP")
        val vm = viewModel(this)
        runCurrent()
        vm.update(AuthenticationFormState("http://server.example", guest = true))
        vm.submit()
        runCurrent()
        vm.declineHttp()
        vm.confirmHttp()
        runCurrent()
        assertEquals(1, calls.size)
        assertFalse(vm.state.value.httpConfirmation)
    }

    @Test fun guestUnauthorizedIsASeparateState() = runTest(dispatcher) {
        result = AuthenticationResult.Failure(401, "Unauthorized")
        val vm = viewModel(this)
        runCurrent()
        vm.update(AuthenticationFormState("https://server.example", guest = true))
        vm.submit()
        runCurrent()
        assertTrue(vm.state.value.guestUnauthorized)
        assertNull(vm.state.value.form.error)
        vm.dismissUnauthorized()
        runCurrent()
        assertFalse(vm.state.value.guestUnauthorized)
    }

    @Test fun authenticatedUnauthorizedAndSaveFailuresStayOnForm() = runTest(dispatcher) {
        val vm = viewModel(this)
        runCurrent()
        vm.update(AuthenticationFormState("https://server.example", "user", "pass"))
        result = AuthenticationResult.Failure(401, "Unauthorized")
        vm.submit()
        runCurrent()
        assertEquals(AuthenticationError.Server("Unauthorized"), vm.state.value.form.error)
        result = AuthenticationResult.SaveFailed
        vm.submit()
        runCurrent()
        assertEquals(AuthenticationError.SaveFailed, vm.state.value.form.error)
        assertFalse(vm.state.value.signedIn)
    }

    @Test fun duplicateTapsCannotStartConcurrentAuthentication() = runTest(dispatcher) {
        pending = CompletableDeferred()
        val vm = viewModel(this)
        runCurrent()
        vm.update(AuthenticationFormState("https://server.example", guest = true))
        vm.submit()
        vm.submit()
        runCurrent()
        assertEquals(1, calls.size)
        assertTrue(vm.state.value.form.busy)
        vm.update(AuthenticationFormState("https://other.example"))
        runCurrent()
        assertEquals("https://server.example", vm.state.value.form.serverUrl)
        pending!!.complete(AuthenticationResult.Success)
        runCurrent()
        assertTrue(vm.state.value.signedIn)
    }

    @Test fun formRemainsInteractiveWhileDemoConfigurationLoads() = runTest(dispatcher) {
        val demo = CompletableDeferred<DemoServer>()
        val vm = viewModel(
            this,
            object : LoginRepository {
                override suspend fun demoServer() = demo.await()
            }
        )
        runCurrent()
        vm.update(AuthenticationFormState(userName = "user"))
        runCurrent()
        assertEquals("user", vm.state.value.form.userName)
        vm.submit()
        runCurrent()
        assertEquals(AuthenticationError.EmptyPassword, vm.state.value.form.error)
        demo.complete(DemoServer(false, ""))
        runCurrent()
    }

    @Test fun demoUsesConfiguredGuestAccountWithSslEnabled() = runTest(dispatcher) {
        val vm = viewModel(this)
        runCurrent()
        vm.tryDemo()
        runCurrent()
        assertEquals(Triple(AccountCredentials("https://demo.example", guest = true), false, false), calls.single())
    }

    @Test fun lastCollectorCancelsDemoLoadingAndResubscriptionRetries() = runTest(dispatcher) {
        var started = 0
        var cancelled = 0
        val vm = LoginViewModel(
            authentication,
            object : LoginRepository {
                override suspend fun demoServer(): DemoServer {
                    started++
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled++
                    }
                }
            },
            tracker
        )
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(1, started)
        collector.cancel()
        runCurrent()
        assertEquals(1, cancelled)
        val again = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(2, started)
        again.cancel()
        runCurrent()
    }

    @Test fun completedDemoSurvivesCollectorRecreation() = runTest(dispatcher) {
        var calls = 0
        val vm = LoginViewModel(
            authentication,
            object : LoginRepository {
                override suspend fun demoServer(): DemoServer {
                    calls++
                    return DemoServer(true, "https://demo.example")
                }
            },
            tracker
        )
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        collector.cancel()
        runCurrent()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
        runCurrent()
        assertEquals(1, calls)
        assertFalse(vm.state.value.demoLoading)
    }
}
