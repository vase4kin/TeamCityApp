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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import teamcityapp.features.create_account.api.CreateAccountTracker
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.UiText

data class CreateAccountUiState(val form: AuthenticationFormState = AuthenticationFormState(), val created: Boolean = false) {
    val formErrorMessage: UiText? = if (form.error == AuthenticationError.DuplicateAccount) UiText.Resource(R.string.add_new_account_dialog_account_exist_error_message) else form.errorMessage
}

@HiltViewModel
class CreateAccountViewModel @Inject constructor(private val repository: AuthenticationRepository, private val tracker: CreateAccountTracker) : ViewModel() {
    private val mutableState = MutableStateFlow(CreateAccountUiState())
    val state = mutableState.asStateFlow()
    fun update(value: AuthenticationFormState) {
        if (!state.value.form.busy && !state.value.created) mutableState.update { it.copy(form = value.copy(busy = false)) }
    }
    fun viewed() = tracker.trackView()
    fun consumeSuccess(): Boolean {
        if (!state.value.created) return false
        mutableState.update { it.copy(created = false) }
        return true
    }
    fun submit() {
        if (state.value.form.busy || state.value.created) return
        val form = state.value.form
        val error = form.validationError()
        mutableState.update { it.copy(form = form.copy(error = error)) }
        if (error != null) return
        val input = form.credentials()
        mutableState.update { it.copy(form = it.form.copy(busy = true)) }
        viewModelScope.launch {
            val result = try {
                repository.signIn(input, false, true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                AuthenticationResult.Failure(0, error.message.orEmpty())
            }
            val error = when (result) {
                AuthenticationResult.Success -> {
                    if (input.guest) tracker.trackGuestUserLoginSuccess(!input.sslDisabled) else tracker.trackUserLoginSuccess(!input.sslDisabled)
                    mutableState.update { it.copy(created = true, form = it.form.copy(password = "")) }
                    null
                }

                AuthenticationResult.DuplicateAccount -> AuthenticationError.DuplicateAccount

                AuthenticationResult.SaveFailed -> {
                    tracker.trackUserDataSaveFailed()
                    AuthenticationError.SaveFailed
                }

                is AuthenticationResult.Failure -> {
                    if (input.guest) tracker.trackGuestUserLoginFailed(result.message) else tracker.trackUserLoginFailed(result.message)
                    AuthenticationError.Server(result.message)
                }
            }
            mutableState.update { it.copy(form = it.form.copy(busy = false, error = error)) }
        }
    }
}
