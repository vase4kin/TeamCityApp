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

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.login.impl.tracker.LoginTrackerImpl
import teamcityapp.libraries.analytics.AnalyticsTracker
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SharedLoginRouteTest {
    @get:Rule val compose = createComposeRule()

    @Test fun recompositionDoesNotReplayViewsAndResumingTracksTheCurrentVisit() {
        val owner = Owner()
        val events = mutableListOf<String>()
        val model = mutableStateOf(viewModel("alice", events))
        compose.runOnUiThread { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                TeamCityTheme { SharedLoginRoute(model.value) }
            }
        }
        compose.runOnIdle {
            assertEquals(emptyList<String>(), events)
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.runOnIdle { assertEquals(listOf("alice"), events) }
        // A form update recomposes the real route without replaying screen telemetry.
        compose.runOnIdle { model.value.update(AuthenticationFormState(userName = "updated")) }
        compose.runOnIdle {
            assertEquals(listOf("alice"), events)
            owner.registry.currentState = Lifecycle.State.STARTED
        }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.runOnIdle {
            assertEquals(listOf("alice", "alice"), events)
            model.value = viewModel("bob", events)
        }
        compose.runOnIdle { assertEquals(listOf("alice", "alice", "bob"), events) }
    }

    private fun viewModel(name: String, events: MutableList<String>) = LoginViewModel(
        object : AuthenticationRepository {
            override suspend fun signIn(credentials: AccountCredentials, checkSecureConnection: Boolean, checkDuplicate: Boolean) = AuthenticationResult.Success
        },
        PublicDemoLoginRepository(),
        LoginTrackerImpl(AnalyticsTracker { if (it.name == "screen_first_login") events += name })
    )

    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
