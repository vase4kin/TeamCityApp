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

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.home.api.HomeTabSelection
import teamcityapp.features.splash.api.navigation.AccountActivationDestination
import teamcityapp.libraries.navigation.DestinationRegistry
import teamcityapp.libraries.navigation.LocalNavigationContentLifecycle
import teamcityapp.libraries.navigation.NavigationHost
import teamcityapp.libraries.navigation.rememberNavigator
import teamcityapp.libraries.theme.TeamCitySystemBars
import teamcityapp.libraries.theme.TeamCityTheme

/** Application-owned Compose host and dispatcher for installed entry points. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var registry: DestinationRegistry

    @Inject lateinit var incoming: IncomingDestinationResolver

    @Inject lateinit var accounts: AppAccountFlowNavigation
    private val incomingRequest = mutableStateOf<DestinationRequest?>(null)

    private var launchAccountKey = "signed-out"
    private var activeNavigator: teamcityapp.libraries.navigation.Navigator? = null
    private lateinit var navigationLifecycle: RootNavigationLifecycle
    internal val navigationSnapshot get() = activeNavigator?.visibleSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(RootActivitySavedState.forRoot(savedInstanceState))
        navigationLifecycle = RootNavigationLifecycle(lifecycle)
        accounts.synchronize()
        val initialSessionKey = accounts.session.value.key
        val restoredLaunch = RootActivitySavedState.restoredLaunch(savedInstanceState, initialSessionKey)
        if (restoredLaunch != null) {
            // Saved payload and owner form one record; discard stale platform arguments/flags.
            setIntent(Intent(intent).replaceExtras(Bundle()))
        }
        var requestedInitial = if (RootActivitySavedState.canRestoreLaunch(savedInstanceState, initialSessionKey)) {
            if (restoredLaunch != null) restoredLaunch.payload?.let(registry::decodeDestination) else incoming.destination(intent)
        } else {
            // Reject old arguments before decoding them, including old build snapshots.
            // Replace them so another recreation cannot replay the rejected alias request.
            if (RootActivitySavedState.accountChanged(savedInstanceState, initialSessionKey)) {
                if (initialSessionKey == "signed-out") accounts.login() else accounts.home(resetSession = true)
            }
            setIntent(incoming.normalizedIntent(Intent(intent).replaceExtras(Bundle()), accounts.session.value.initial))
            null
        }
        if (requestedInitial is AccountActivationDestination && accounts.session.value.requestId != null) {
            // A retained coordinator may already have completed the saved preparation.
            requestedInitial = accounts.session.value.initial
        }
        val home = requestedInitial as? HomeDestination
        if (home != null && (incoming.requiresHomeReset(intent) || initialSessionKey == "signed-out")) {
            if (initialSessionKey == "signed-out") accounts.login() else accounts.activate(home, resetSession = true)
            setIntent(incoming.normalizedIntent(intent, accounts.session.value.initial))
            requestedInitial = null
        }
        requestedInitial?.let { setIntent(incoming.normalizedIntent(intent, it)) }
        if (restoredLaunch != null && requestedInitial == null) setIntent(incoming.normalizedIntent(intent, accounts.session.value.initial))
        launchAccountKey = accounts.session.value.key
        savedInstanceState?.getString(PENDING_ACCOUNT)?.takeIf { it == launchAccountKey }?.let { owner ->
            savedInstanceState.getString(PENDING_DESTINATION)?.let { payload ->
                incomingRequest.value = DestinationRequest(registry.decodeDestination(payload), owner)
                navigationLifecycle.hold()
            }
        }
        enableEdgeToEdge()
        setContent {
            TeamCityTheme {
                TeamCitySystemBars(window)
                val session by accounts.session.collectAsStateWithLifecycle()
                val initial = if (session.key == initialSessionKey) requestedInitial ?: session.initial else session.initial
                val navigator = rememberNavigator(initial, registry, session.key)
                DisposableEffect(navigator) {
                    activeNavigator = navigator
                    onDispose { if (activeNavigator === navigator) activeNavigator = null }
                }
                val handledRequest = rememberSaveable(
                    stateSaver = Saver<AccountRequestOwner, Any>(
                        save = { arrayListOf(it.accountKey, it.requestId) },
                        restore = { saved ->
                            when {
                                saved is List<*> && saved.size == 2 && saved[0] is String && (saved[1] == null || saved[1] is String) -> AccountRequestOwner(saved[0] as String, saved[1] as String?)

                                // Earlier root versions saved only the request ID.
                                saved is String -> AccountRequestOwner(savedInstanceState?.getString(RootActivitySavedState.ACCOUNT_KEY) ?: session.key, saved)

                                else -> null
                            }
                        }
                    )
                ) { mutableStateOf(AccountRequestOwner(session.key, session.requestId)) }
                if (handledRequest.value.accountKey != session.key) {
                    // rememberNavigator already replaces owners when the account changes.
                    handledRequest.value = AccountRequestOwner(session.key, session.requestId)
                }
                LaunchedEffect(session.key, session.requestId) {
                    if (session.requestId != null && handledRequest.value.requestId != session.requestId) {
                        navigator.reset(session.initial, session.key)
                        handledRequest.value = AccountRequestOwner(session.key, session.requestId)
                    } else if (session.requestId == null && navigator.current.destination is AccountActivationDestination) {
                        // A dead process cannot retain its preparation job. Resume the saved policy
                        // so an interrupted account switch still clears the previous session.
                        val activation = navigator.current.destination as AccountActivationDestination
                        if (!activation.failed) activation.destination?.let { accounts.activate(it, activation.resetSession) } ?: accounts.home(activation.resetSession)
                    }
                    if (session.requestId != null && session.initial !is AccountActivationDestination && launchAccountKey == session.key && incoming.destination(intent) is AccountActivationDestination) {
                        setIntent(incoming.normalizedIntent(intent, session.initial))
                    }
                }
                val request = incomingRequest.value
                LaunchedEffect(request, session.key) {
                    request?.takeIf { it.accountKey == session.key }?.let {
                        val home = it.destination as? HomeDestination
                        if (home == null) {
                            navigator.push(it.destination)
                        } else {
                            val existing = navigator.snapshot.entries.firstOrNull { entry -> entry.destination is HomeDestination }
                            if (existing == null) navigator.reset(home, session.key) else navigator.popTo(existing.id, HomeTabSelection(home.tab))
                        }
                    }
                    if (incomingRequest.value === request) incomingRequest.value = null
                }
                SideEffect {
                    if (incomingRequest.value == null && (session.requestId == null || handledRequest.value.requestId == session.requestId) && navigator.snapshot.sessionKey == session.key) {
                        navigationLifecycle.applied()
                    }
                }
                CompositionLocalProvider(LocalNavigationContentLifecycle provides navigationLifecycle.lifecycle) {
                    NavigationHost(navigator, registry, ::finish)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Record the account of the actual saved UI, including a not-yet-applied reset.
        outState.putString(RootActivitySavedState.ACCOUNT_KEY, navigationSnapshot?.sessionKey ?: accounts.session.value.key)
        outState.putString(RootActivitySavedState.LAUNCH_ACCOUNT_KEY, launchAccountKey)
        outState.putString(RootActivitySavedState.LAUNCH_DESTINATION, intent.getStringExtra(EXTRA_DESTINATION))
        incomingRequest.value?.takeIf { it.accountKey == accounts.session.value.key }?.let { request ->
            outState.putString(PENDING_ACCOUNT, request.accountKey)
            outState.putString(PENDING_DESTINATION, registry.encodeDestination(request.destination))
        }
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // A launcher/bring-to-front Intent has no destination. Keep the normalized
        // arguments of this activity's installed alias for later recreation.
        var destination = incoming.destination(intent) ?: return
        navigationLifecycle.hold()
        val home = destination as? HomeDestination
        if (home != null) {
            val previousKey = accounts.session.value.key
            accounts.synchronize()
            if (incoming.requiresHomeReset(intent) || previousKey != accounts.session.value.key || accounts.session.value.key == "signed-out") {
                incomingRequest.value = null
                if (accounts.session.value.key == "signed-out") accounts.login() else accounts.activate(home, resetSession = true)
                destination = accounts.session.value.initial
                launchAccountKey = accounts.session.value.key
                // Account preparation publishes the replacement stack; do not also push it.
                setIntent(incoming.normalizedIntent(Intent(intent).apply { component = this@MainActivity.intent.component }, destination))
                return
            }
        }
        launchAccountKey = accounts.session.value.key
        // Alias names have different Android component identities. Keep this host's launch
        // component stable while saving the normalized request for recreation.
        setIntent(
            incoming.normalizedIntent(intent, destination).apply {
                component = this@MainActivity.intent.component
            }
        )
        incomingRequest.value = DestinationRequest(destination, launchAccountKey)
    }

    private class DestinationRequest(val destination: androidx.navigation3.runtime.NavKey, val accountKey: String)
    private data class AccountRequestOwner(val accountKey: String, val requestId: String?)
    companion object {
        const val EXTRA_DESTINATION = "navigation.destination"
        private const val PENDING_ACCOUNT = "navigation.pending-account"
        private const val PENDING_DESTINATION = "navigation.pending-destination"
    }
}
