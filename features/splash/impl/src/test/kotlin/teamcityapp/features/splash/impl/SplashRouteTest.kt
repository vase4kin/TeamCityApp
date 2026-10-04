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

package teamcityapp.features.splash.impl

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.splash.api.*
import teamcityapp.features.splash.impl.router.SplashRouter
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SplashRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
    private class Router : SplashRouter {
        val destinations = mutableListOf<SplashDestination>()
        override fun navigate(destination: SplashDestination) {
            destinations += destination
        }
    }

    @After fun after() {
        compose.runOnIdle { store.clear() }
    }

    @Test fun readyAtStartedWaitsForResumedThenCannotReplayOnResumeOrRecomposition() {
        val owner = Owner()
        val router = Router()
        val visible = mutableStateOf(true)
        val vm = SplashViewModel(object : SplashRepository {
            override suspend fun hasAccounts() = true
        })
        store.put("splash", vm)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.setContent { if (visible.value) CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { SplashRoute(router, vm) } } }
        compose.waitUntil { compose.onAllNodesWithTag("splash:ready:Home").fetchSemanticsNodes().isNotEmpty() }
        assertTrue(router.destinations.isEmpty())
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil { router.destinations.size == 1 }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.runOnIdle {
            owner.registry.currentState = Lifecycle.State.RESUMED
            visible.value = false
        }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        assertEquals(listOf(SplashDestination.Home), router.destinations)
    }

    @Test fun retryRechecksTheRepositoryAndRoutesAfterRecovery() {
        var reads = 0
        val router = Router()
        val vm = SplashViewModel(object : SplashRepository {
            override suspend fun hasAccounts(): Boolean {
                reads++
                if (reads == 1) throw IllegalStateException()
                return false
            }
        })
        store.put("splash", vm)
        compose.setContent { TeamCityTheme { SplashRoute(router, vm) } }
        compose.waitUntil { compose.onAllNodesWithTag("splash:error").fetchSemanticsNodes().isNotEmpty() }
        assertTrue(router.destinations.isEmpty())
        compose.onNodeWithText("Try again").performClick()
        compose.waitUntil { router.destinations.size == 1 }
        assertEquals(listOf(SplashDestination.Login), router.destinations)
        assertEquals(2, reads)
    }
}
