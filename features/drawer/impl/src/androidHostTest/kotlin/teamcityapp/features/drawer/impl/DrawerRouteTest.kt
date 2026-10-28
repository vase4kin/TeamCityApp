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

package teamcityapp.features.drawer.impl
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
import teamcityapp.features.drawer.api.*
import teamcityapp.features.drawer.impl.router.DrawerRouter
import teamcityapp.features.drawer.impl.tracker.DrawerTracker
import teamcityapp.libraries.theme.TeamCityTheme
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DrawerRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private class Router : DrawerRouter {
        var attaches = 0
        var detaches = 0
        var homes = 0
        var enabled = true
        val opened = mutableListOf<String>()
        override fun attach() {
            attaches++
        }
        override fun detach() {
            detaches++
        }
        override fun setInteractionsEnabled(enabled: Boolean) {
            this.enabled = enabled
        }
        override fun openHome() {
            homes++
        }
        override fun openAbout() {
            opened += "about"
        }
        override fun openSettings() {
            opened += "settings"
        }
        override fun openAddAccount() {
            opened += "add"
        }
        override fun openManageAccounts() {
            opened += "manage"
        }
        override fun openPrivacy() {
            opened += "privacy"
        }
        override fun openRate() {
            opened += "rate"
        }
    }
    private class Tracker : DrawerTracker {
        var views = 0
        val opened = mutableListOf<String>()
        override fun trackView() {
            views++
        }
        override fun trackChangeAccount() {}
        override fun trackOpenPrivacy() {
            opened += "privacy"
        }
        override fun trackRateTheApp() {
            opened += "rate"
        }
        override fun trackOpenAbout() {
            opened += "about"
        }
        override fun trackOpenAddNewAccount() {
            opened += "add"
        }
        override fun trackOpenManageAccounts() {
            opened += "manage"
        }
        override fun trackOpenSettings() {
            opened += "settings"
        }
    }
    private fun vm(repo: DrawerRepository, tracker: Tracker = Tracker()) = DrawerViewModel(repo, tracker).also { store.put("drawer", it) }

    @After fun after() {
        compose.runOnIdle { store.clear() }
    }

    @Test fun compositionRecreatesAndCleansPlatformResourcesForEachView() {
        val router = Router()
        val visible = mutableStateOf(true)
        val tracker = Tracker()
        val viewModel = vm(
            object : DrawerRepository {
                override suspend fun accounts() = listOf(active)
                override suspend fun select(id: DrawerAccountId) = error("unexpected switch")
            },
            tracker
        )
        compose.setContent { if (visible.value) TeamCityTheme { DrawerRoute(router, viewModel) } }
        compose.waitForIdle()
        assertEquals("initial attachment", 1, router.attaches)
        assertEquals(1, tracker.views)
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        assertEquals("first cleanup", 1, router.detaches)
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        assertEquals("second attachment", 2, router.attaches)
        assertEquals(1, router.detaches)
        assertEquals(2, tracker.views)
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        assertEquals("second cleanup", 2, router.detaches)
    }

    @Test fun menuActionsTrackInViewModelAndKeepNavigationInUi() {
        val tracker = Tracker()
        val currentRouter = mutableStateOf(Router())
        val model = vm(
            object : DrawerRepository {
                override suspend fun accounts() = listOf(active, inactive)
                override suspend fun select(id: DrawerAccountId): AccountSelection = kotlinx.coroutines.awaitCancellation()
            },
            tracker
        )
        compose.setContent { TeamCityTheme { DrawerRoute(currentRouter.value, model) } }
        compose.onNodeWithTag(accountTag(inactive.id)).assertExists()
        assertEquals(1, tracker.views)
        compose.runOnIdle { currentRouter.value = Router() }
        compose.waitForIdle()
        assertEquals(1, tracker.views)
        val actions = listOf("add", "manage", "settings", "about", "privacy", "rate")
        for (action in actions) compose.onNodeWithTag("drawer:$action").performScrollTo().performClick()
        assertEquals(actions, tracker.opened)
        assertEquals(actions, currentRouter.value.opened)
        compose.onNodeWithTag(accountTag(inactive.id)).performScrollTo().performClick()
        compose.waitUntil { !currentRouter.value.enabled }
        compose.onNodeWithTag("drawer:about").performScrollTo().performClick()
        assertEquals(actions, tracker.opened)
        assertEquals(actions, currentRouter.value.opened)
    }

    @Test fun screenTrackingFollowsResumeWithTheSameViewModel() {
        val tracker = Tracker()
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        }
        val model = vm(
            object : DrawerRepository {
                override suspend fun accounts() = listOf(active)
                override suspend fun select(id: DrawerAccountId) = error("Unexpected switch")
            },
            tracker
        )
        val router = Router()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { DrawerRoute(router, model) } } }
        compose.waitForIdle()
        assertEquals(1, tracker.views)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.CREATED }
        compose.waitForIdle()
        assertEquals(1, tracker.views)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        assertEquals(2, tracker.views)
    }

    @Test fun navigationWaitsForResumedUiAndCannotReplayAfterAnotherResume() {
        val router = Router()
        val pending = CompletableDeferred<AccountSelection>()
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        val viewModel = vm(object : DrawerRepository {
            override suspend fun accounts() = listOf(active, inactive)
            override suspend fun select(id: DrawerAccountId) = pending.await()
        })
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { DrawerRoute(router, viewModel) } } }
        compose.waitUntil { compose.onAllNodesWithTag(accountTag(inactive.id)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag(accountTag(inactive.id)).performClick()
        compose.waitUntil { !router.enabled }
        compose.runOnIdle {
            owner.registry.currentState = Lifecycle.State.CREATED
            pending.complete(AccountSelection(listOf(inactive.copy(isActive = true)), SelectionOutcome.Changed))
        }
        compose.waitForIdle()
        assertEquals(0, router.homes)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil { router.homes == 1 }
        compose.runOnIdle {
            owner.registry.currentState = Lifecycle.State.CREATED
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.waitForIdle()
        assertEquals(1, router.homes)
    }
}
