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

package com.github.vase4kin.teamcityapp.navigation

import androidx.navigation3.runtime.NavKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.splash.api.navigation.AccountActivationDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.libraries.navigation.AccountFlowNavigation

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AccountNavigationScope
internal data class RootAccountSession(val key: String, val initial: NavKey, val requestId: String? = null)

/** No screen or navigator is retained while preparing an active account. */
@Singleton
class AppAccountFlowNavigation @Inject constructor(
    private val preparation: AccountSessionPreparation,
    @AccountNavigationScope private val scope: CoroutineScope
) : AccountFlowNavigation {
    private val root = MutableStateFlow(RootAccountSession(preparation.currentKey(), SplashNavigationDestination))
    private var pending: Job? = null
    internal val session = root.asStateFlow()
    internal fun synchronize() {
        val key = preparation.currentKey()
        if (root.value.key != key) {
            pending?.cancel()
            root.value = RootAccountSession(key, SplashNavigationDestination)
        }
    }
    private fun request(key: String, destination: NavKey) {
        root.value = RootAccountSession(key, destination, UUID.randomUUID().toString())
    }
    override fun home(resetSession: Boolean) {
        pending?.cancel()
        val key = preparation.currentKey()
        request(key, AccountActivationDestination(resetSession = resetSession))
        pending = scope.launch {
            try {
                preparation.prepare(key, resetSession)
                if (preparation.currentKey() != key) throw CancellationException("Account changed during activation")
                request(key, HomeDestination())
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                request(key, AccountActivationDestination(failed = true, resetSession = resetSession))
            }
        }
    }
    override fun login() {
        pending?.cancel()
        preparation.clear()
        request("signed-out", LoginDestination)
    }
}
