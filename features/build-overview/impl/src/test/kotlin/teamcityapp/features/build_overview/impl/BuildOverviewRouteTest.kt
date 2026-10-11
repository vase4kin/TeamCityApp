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

package teamcityapp.features.build_overview.impl

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.Serializable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.impl.router.BuildOverviewRouter
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildOverviewRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()

    // Routes collect only while RESUMED; keep lifecycle ownership explicit in local tests.
    private val resumedOwner = object : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle = registry
    }
    private val router = FakeRouter()
    private val codec = object : BuildLaunchCodec {
        override fun encode(build: BuildLaunchData): Serializable = "legacy-build"
        override fun decode(payload: Serializable) = BuildOverviewFixtures.finished
    }

    @After fun after() {
        store.clear()
    }
    private fun viewModel(load: suspend (Boolean) -> BuildLaunchData) = BuildOverviewViewModel(
        SavedStateHandle(mapOf(BuildOverviewNavigation.BUILD to "legacy-build")),
        object : BuildOverviewRepository {
            override suspend fun build(href: String, forceRefresh: Boolean) = load(forceRefresh)
        },
        codec
    ).also { store.put("overview", it) }

    @Test fun hidingOverviewCancelsPendingLoadAndReleasesUiResourcesBeforeResuming() {
        var calls = 0
        var cancelled = false
        val vm = viewModel {
            calls++
            if (calls == 1) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            BuildOverviewFixtures.finished
        }
        val visible = mutableStateOf(true)
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildOverviewRoute(router, visible.value, {}, vm) } } }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            calls == 1
        }
        val disposed = router.disposals
        compose.runOnIdle { visible.value = false }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            cancelled
        }
        assertTrue(router.disposals > disposed)
        compose.runOnIdle { visible.value = true }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            calls == 2 && router.resumes.isNotEmpty()
        }
        assertEquals(BuildOverviewFixtures.finished, router.resumes.last())
    }

    @Test fun completedOverviewReleasesPromptsWhileHiddenAndReturnsWithoutAnotherLoad() {
        var calls = 0
        val vm = viewModel {
            calls++
            BuildOverviewFixtures.finished
        }
        val visible = mutableStateOf(true)
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildOverviewRoute(router, visible.value, {}, vm) } } }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.resumes.isNotEmpty()
        }
        val disposed = router.disposals
        val resumes = router.resumes.size
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        assertTrue(router.disposals > disposed)
        compose.runOnIdle { visible.value = true }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.resumes.size > resumes
        }
        assertEquals(1, calls)
    }

    @Test fun hiddenTabKeepsRefreshRequestsButNeverExecutesActionSheetNavigation() {
        val calls = mutableListOf<Boolean>()
        val vm = viewModel {
            calls.add(it)
            BuildOverviewFixtures.finished
        }
        val visible = mutableStateOf(true)
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { BuildOverviewRoute(router, visible.value, {}, vm) } } }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.resumes.isNotEmpty() && router.requests.subscriptionCount.value > 0
        }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle {
            router.requests.tryEmit(BuildOverviewRequest.Project)
            router.requests.tryEmit(BuildOverviewRequest.Refresh)
        }
        compose.waitForIdle()
        assertTrue(router.actions.isEmpty())
        assertEquals(listOf(false), calls)
        compose.runOnIdle { visible.value = true }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            calls.size == 2
        }
        assertEquals(listOf(false, true), calls)
    }

    @Test fun pausedHostCleansUpAndOnlyResumedOwnerCanExecuteNavigation() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        val vm = viewModel { BuildOverviewFixtures.finished }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                TeamCityTheme { BuildOverviewRoute(router, true, {}, vm) }
            }
        }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.resumes.isNotEmpty() && router.requests.subscriptionCount.value > 0
        }
        val disposed = router.disposals
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        assertTrue(router.disposals > disposed)
        compose.runOnIdle { router.requests.tryEmit(BuildOverviewRequest.Project) }
        compose.waitForIdle()
        assertTrue(router.actions.isEmpty())
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        compose.runOnIdle { router.requests.tryEmit(BuildOverviewRequest.Branch("raw-branch")) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.size == 1
        }
        assertEquals(Triple(BuildOverviewAction.Configuration, BuildOverviewFixtures.finished, "raw-branch"), router.actions.single())
    }

    private class FakeRouter : BuildOverviewRouter {
        override val requests = MutableSharedFlow<BuildOverviewRequest>(extraBufferCapacity = 4)
        val resumes = mutableListOf<BuildLaunchData>()
        val actions = mutableListOf<Triple<BuildOverviewAction, BuildLaunchData, String?>>()
        var disposals = 0
        override fun loaded(build: BuildLaunchData) {}
        override fun resumed(build: BuildLaunchData) {
            resumes.add(build)
        }
        override fun row(row: OverviewRow) {}
        override fun dispatch(action: BuildOverviewAction, build: BuildLaunchData, branch: String?) {
            actions.add(Triple(action, build, branch))
        }
        override fun dispose() {
            disposals++
        }
    }
}
