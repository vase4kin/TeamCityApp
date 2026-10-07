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

package teamcityapp.features.create_account.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.mock
import teamcityapp.features.create_account.api.CreateAccountTracker
import teamcityapp.libraries.authentication.*

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAccountViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var result: AuthenticationResult = AuthenticationResult.Success
    private var pending: CompletableDeferred<AuthenticationResult>? = null
    private val calls = mutableListOf<Triple<AccountCredentials, Boolean, Boolean>>()
    private val repository = object : AuthenticationRepository {
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
    private fun vm() = CreateAccountViewModel(repository, mock(CreateAccountTracker::class.java))

    @Test fun validatesBeforeTouchingRepository() = runTest(dispatcher) {
        val vm = vm()
        for ((form, error) in listOf(AuthenticationFormState(serverUrl = " ", guest = true) to AuthenticationError.EmptyUrl, AuthenticationFormState() to AuthenticationError.EmptyUserName, AuthenticationFormState(userName = "user") to AuthenticationError.EmptyPassword)) {
            vm.update(form)
            vm.submit()
            runCurrent()
            assertEquals(error, vm.state.value.form.error)
        }
        assertTrue(calls.isEmpty())
    }

    @Test fun checksDuplicateAccountAndPreservesCreateAccountHttpPolicy() = runTest(dispatcher) {
        val vm = vm()
        vm.update(AuthenticationFormState(" http://server.example ", " user ", " pass ", sslDisabled = true))
        vm.submit()
        runCurrent()
        assertEquals(Triple(AccountCredentials("http://server.example", "user", "pass", sslDisabled = true), false, true), calls.single())
        assertTrue(vm.state.value.created)
        assertEquals("", vm.state.value.form.password)
        assertTrue(vm.consumeSuccess())
        assertFalse(vm.consumeSuccess())
    }

    @Test fun guestModeDoesNotValidateHiddenFields() = runTest(dispatcher) {
        val vm = vm()
        vm.update(AuthenticationFormState("https://server.example", guest = true))
        vm.submit()
        runCurrent()
        assertTrue(calls.single().first.guest)
    }

    @Test fun duplicateAndSaveErrorsNeverNavigate() = runTest(dispatcher) {
        val vm = vm()
        vm.update(AuthenticationFormState("https://server.example", guest = true))
        result = AuthenticationResult.DuplicateAccount
        vm.submit()
        runCurrent()
        assertEquals(AuthenticationError.DuplicateAccount, vm.state.value.form.error)
        result = AuthenticationResult.SaveFailed
        vm.submit()
        runCurrent()
        assertEquals(AuthenticationError.SaveFailed, vm.state.value.form.error)
        assertFalse(vm.state.value.created)
    }

    @Test fun serverFailuresCanBeRetried() = runTest(dispatcher) {
        val vm = vm()
        vm.update(AuthenticationFormState("https://server.example", guest = true))
        result = AuthenticationResult.Failure(401, "Unauthorized")
        vm.submit()
        runCurrent()
        assertEquals(AuthenticationError.Server("Unauthorized"), vm.state.value.form.error)
        result = AuthenticationResult.Success
        vm.submit()
        runCurrent()
        assertTrue(vm.state.value.created)
    }

    @Test fun submissionFreezesCredentialsAndIgnoresRepeatedTaps() = runTest(dispatcher) {
        pending = CompletableDeferred()
        val vm = vm()
        vm.update(AuthenticationFormState("https://server.example", guest = true))
        vm.submit()
        vm.submit()
        runCurrent()
        vm.update(AuthenticationFormState("https://other.example"))
        assertEquals("https://server.example", vm.state.value.form.serverUrl)
        assertEquals(1, calls.size)
        pending!!.complete(AuthenticationResult.Success)
        runCurrent()
        assertTrue(vm.state.value.created)
    }
}
