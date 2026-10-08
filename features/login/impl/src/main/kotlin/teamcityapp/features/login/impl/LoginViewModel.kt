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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.login.api.*
import teamcityapp.libraries.authentication.*

data class LoginUiState(val form: AuthenticationFormState = AuthenticationFormState(), val demo: DemoServer? = null, val demoLoading: Boolean = true, val httpConfirmation: Boolean = false, val guestUnauthorized: Boolean = false, val signedIn: Boolean = false)

@HiltViewModel
class LoginViewModel @Inject constructor(private val authentication: AuthenticationRepository, private val repository: LoginRepository, private val tracker: LoginTracker) : ViewModel() {
    private val form = MutableStateFlow(LoginUiState())
    private var pending: AccountCredentials? = null
    private var demo: DemoServer? = null
    private val demoState = flow<DemoServer?> {
        if (demo == null) emit(null)
        val result = demo ?: try {
            repository.demoServer()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            DemoServer(false, "")
        }
        demo = result
        emit(result)
    }
    val state = combine(form, demoState) { state, server -> state.copy(demo = server, demoLoading = server == null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), form.value)

    fun update(value: AuthenticationFormState) {
        if (!form.value.form.busy && !form.value.signedIn && pending == null) form.update { it.copy(form = value.copy(busy = false)) }
    }
    fun viewed() = tracker.trackView()
    fun submit() {
        if (form.value.form.busy || form.value.signedIn || pending != null) return
        val input = form.value.form
        val error = input.validationError()
        form.update { it.copy(form = input.copy(error = error), guestUnauthorized = false) }
        if (error == null) authenticate(input.credentials(), true)
    }
    fun confirmHttp() {
        val input = pending ?: return
        pending = null
        form.update { it.copy(httpConfirmation = false) }
        authenticate(input, false)
    }
    fun declineHttp() {
        pending = null
        form.update { it.copy(httpConfirmation = false) }
    }
    fun dismissUnauthorized() {
        form.update { it.copy(guestUnauthorized = false) }
    }
    fun demoClicked() = tracker.trackUserClicksOnTryItOut()
    fun declineDemo() = tracker.trackUserDeclinesTryingTryItOut()
    fun tryDemo() {
        val server = demo ?: return
        if (form.value.form.busy || form.value.signedIn) return
        tracker.trackUserTriesTryItOut()
        authenticate(AccountCredentials(server.url, guest = true), false)
    }
    fun consumeSuccess(): Boolean {
        if (!form.value.signedIn) return false
        form.update { it.copy(signedIn = false) }
        return true
    }
    private fun authenticate(input: AccountCredentials, checkHttp: Boolean) {
        form.update { it.copy(form = it.form.copy(busy = true, error = null)) }
        viewModelScope.launch {
            val result = try {
                authentication.signIn(input, checkHttp, false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                AuthenticationResult.Failure(0, error.message.orEmpty())
            }
            var error: AuthenticationError? = null
            when (result) {
                AuthenticationResult.Success -> {
                    if (input.guest) tracker.trackGuestUserLoginSuccess(!input.sslDisabled) else tracker.trackUserLoginSuccess(!input.sslDisabled)
                    form.update { it.copy(signedIn = true, form = it.form.copy(password = "")) }
                }

                AuthenticationResult.SaveFailed -> {
                    error = AuthenticationError.SaveFailed
                    tracker.trackUserDataSaveFailed()
                }

                AuthenticationResult.DuplicateAccount -> error = AuthenticationError.DuplicateAccount

                is AuthenticationResult.Failure -> {
                    if (input.guest) tracker.trackGuestUserLoginFailed(result.message) else tracker.trackUserLoginFailed(result.message)
                    when {
                        result.statusCode == -1 -> {
                            pending = input
                            form.update { it.copy(httpConfirmation = true) }
                        }

                        result.statusCode == 401 && input.guest -> form.update { it.copy(guestUnauthorized = true) }

                        else -> error = AuthenticationError.Server(result.message)
                    }
                }
            }
            form.update { it.copy(form = it.form.copy(busy = false, error = error)) }
        }
    }
}
