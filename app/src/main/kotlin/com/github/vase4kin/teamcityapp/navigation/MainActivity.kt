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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.splash.api.navigation.AccountActivationDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.libraries.accounts.AccountSessionState
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.app_theme.ThemePreferencesRepository
import teamcityapp.libraries.navigation.Navigator
import teamcityapp.libraries.theme.TeamCitySystemBars
import teamcityapp.shared.*

/** Android decodes installed Intents; the common root owns all app routes and repositories. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var incoming: IncomingDestinationResolver

    @Inject lateinit var theme: ThemePreferencesRepository

    @Inject lateinit var rating: AppRating
    private val model: AndroidSharedAppViewModel by viewModels()
    private val ready = mutableStateOf(false)
    private val launch = mutableStateOf<SharedAppLaunch?>(null)
    private var activeNavigator: Navigator? = null
    internal val navigationSnapshot get() = activeNavigator?.visibleSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(RootActivitySavedState.forSharedRoot(savedInstanceState))
        val launchSplash = LaunchSplashState()
        splash.setKeepOnScreenCondition(launchSplash::keepOnScreen)
        enableEdgeToEdge()
        val platform = AndroidAppPlatform(this, lifecycle, theme, rating)
        lifecycleScope.launch {
            combine(model.sessions.state, model.restoreFailed) { state, failed -> state != AccountSessionState.Loading || failed }.first { it }
            model.synchronizeSession()
            val owner = accountKey()
            if (savedInstanceState == null) {
                receive(intent)
            } else {
                // Validate ownership before decoding any persisted launch arguments.
                RootActivitySavedState.restoredLaunch(savedInstanceState, owner)?.payload?.let { encoded ->
                    launch.value = SharedAppLaunch(savedInstanceState.getString(PENDING_ID) ?: kotlin.uuid.Uuid.random().toString(), incoming.destination(Intent().putExtra(EXTRA_DESTINATION, encoded)) ?: return@let, owner)
                }
                setIntent(Intent(intent).replaceExtras(Bundle()))
            }
            ready.value = true
        }
        setContent {
            TeamCityApp(model, platform, launch.value, onNavigator = {
                activeNavigator = it
                if (it != null) launchSplash.release()
            }, onExit = ::finish, ready = ready.value, onLaunchHandled = { id ->
                if (launch.value?.id == id) launch.value = null
            }) {
                TeamCitySystemBars(window)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(RootActivitySavedState.ACCOUNT_KEY, navigationSnapshot?.sessionKey ?: accountKey())
        outState.putBoolean(RootActivitySavedState.SHARED_ROOT, true)
        outState.putString(RootActivitySavedState.LAUNCH_ACCOUNT_KEY, launch.value?.accountKey ?: accountKey())
        outState.putString(RootActivitySavedState.LAUNCH_DESTINATION, launch.value?.let { incoming.normalizedIntent(Intent(), it.destination).getStringExtra(EXTRA_DESTINATION) })
        outState.putString(PENDING_ID, launch.value?.id)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receive(intent)
    }

    private fun accountKey() = (model.sessions.state.value as? AccountSessionState.Active)?.scope?.identity?.key ?: "signed-out"

    private fun receive(intent: Intent) {
        lifecycleScope.launch {
            model.sessions.state.first { it != AccountSessionState.Loading }
            val decoded = incoming.destination(intent) ?: return@launch
            if (decoded is HomeDestination) model.synchronizeSession(incoming.requiresHomeReset(intent))
            val owner = accountKey()
            // Launcher/splash entry resumes the common saved journey, rather than pushing a host.
            if (decoded is SplashNavigationDestination || decoded is AccountActivationDestination) return@launch
            setIntent(incoming.normalizedIntent(Intent(intent).apply { component = this@MainActivity.intent.component }, decoded))
            launch.value = SharedAppLaunch(kotlin.uuid.Uuid.random().toString(), decoded, owner)
        }
    }

    companion object {
        private const val PENDING_ID = "navigation.pending-id"
        const val EXTRA_DESTINATION = "navigation.destination"
    }
}
