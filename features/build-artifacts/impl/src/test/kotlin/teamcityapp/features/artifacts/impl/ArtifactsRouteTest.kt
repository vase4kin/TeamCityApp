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

package teamcityapp.features.artifacts.impl

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.Serializable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.impl.router.ArtifactsRouter
import teamcityapp.features.artifacts.impl.tracker.ArtifactsTracker
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtifactsRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()

    // Routes collect only while RESUMED; keep lifecycle ownership explicit in local tests.
    private val resumedOwner = object : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle = registry
    }
    private val router = FakeRouter()
    private val build = BuildLaunchData("42", "/builds/42", buildTypeId = "configuration")
    private val codec = object : BuildLaunchCodec {
        override fun encode(build: BuildLaunchData): Serializable = "legacy-build"
        override fun decode(payload: Serializable) = build
    }

    @After fun after() {
        store.clear()
    }
    private fun viewModel(load: suspend () -> List<Artifact> = { emptyList() }, downloadResult: suspend () -> DownloadedArtifact = { DownloadedArtifact("/downloaded") }) = ArtifactsViewModel(
        SavedStateHandle(mapOf(ArtifactsNavigation.BUILD to "legacy-build", ArtifactsNavigation.URL to "/artifacts")),
        object : ArtifactsRepository {
            override suspend fun entries(url: String, forceRefresh: Boolean) = load()
            override suspend fun download(file: ArtifactDownload) = downloadResult()
        },
        codec,
        object : ArtifactsTracker {
            override fun viewShown() {}
        }
    ).also { store.put("artifacts", it) }

    @Test fun hiddenArtifactTabDoesNotLoadOrConsumeActionSheetEventsUntilVisible() {
        var calls = 0
        val vm = viewModel(load = {
            calls++
            emptyList()
        })
        val visible = mutableStateOf(false)
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { ArtifactsRoute(router, false, visible.value, vm) } } }
        compose.waitForIdle()
        assertEquals(0, calls)
        assertEquals(0, router.actions.subscriptionCount.value)
        compose.runOnIdle {
            router.actions.tryEmit(ArtifactAction.Open("hidden", "/hidden"))
            visible.value = true
        }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            calls == 1 && router.actions.subscriptionCount.value == 1
        }
        assertTrue(router.folders.isEmpty())
        compose.runOnIdle { router.actions.tryEmit(ArtifactAction.Open("folder", "opaque/archive!/children")) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.folders.size == 1
        }
        assertEquals(Triple("folder", build, "opaque/archive!/children"), router.folders.single())
    }

    @Test fun hidingOrPausingTheHostUnsubscribesActionsAndDisposesCustomTabBindings() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        val visible = mutableStateOf(true)
        val vm = viewModel()
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                TeamCityTheme { ArtifactsRoute(router, false, visible.value, vm) }
            }
        }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 1
        }
        val first = router.disposals
        compose.runOnIdle { visible.value = false }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 0
        }
        assertTrue(router.disposals > first)
        compose.runOnIdle { visible.value = true }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 1
        }
        val second = router.disposals
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 0
        }
        assertTrue(router.disposals > second)
    }

    @Test fun explicitDownloadSurvivesHiddenTabAndLaunchesFileOnlyOnceAfterResuming() {
        val result = CompletableDeferred<DownloadedArtifact>()
        var started = false
        val vm = viewModel(downloadResult = {
            started = true
            result.await()
        })
        val visible = mutableStateOf(true)
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { ArtifactsRoute(router, false, visible.value, vm) } } }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 1
        }
        compose.runOnIdle { router.actions.tryEmit(ArtifactAction.Download(ArtifactDownload("file.zip", "opaque/download"))) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            started
        }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.onNodeWithText(RuntimeEnvironment.getApplication().getString(R.string.artifacts_downloading_title)).assertDoesNotExist()
        compose.runOnIdle { result.complete(DownloadedArtifact("/completed/file.zip")) }
        compose.waitForIdle()
        assertTrue(router.files.isEmpty())
        compose.runOnIdle { visible.value = true }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.files.size == 1
        }
        assertEquals(DownloadedArtifact("/completed/file.zip"), router.files.single())
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        assertEquals(1, router.files.size)
    }

    @Test fun backgroundDownloadFailureNotifiesBuildDetailsOnlyOnceWhenTheArtifactTabReturns() {
        val result = CompletableDeferred<DownloadedArtifact>()
        var started = false
        val vm = viewModel(downloadResult = {
            started = true
            result.await()
        })
        val visible = mutableStateOf(true)
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { ArtifactsRoute(router, false, visible.value, vm) } } }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 1
        }
        compose.runOnIdle { router.actions.tryEmit(ArtifactAction.Download(ArtifactDownload("file.zip", "opaque/download"))) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            started
        }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle { result.completeExceptionally(IllegalStateException("offline")) }
        compose.waitForIdle()
        assertEquals(0, router.failures)
        compose.runOnIdle { visible.value = true }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.failures == 1
        }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        assertEquals(1, router.failures)
    }

    @Test fun pausedHostDismissesDownloadDialogButRetainsTheExplicitOperation() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this)
            override val lifecycle: Lifecycle = registry
        }
        owner.registry.currentState = Lifecycle.State.RESUMED
        val result = CompletableDeferred<DownloadedArtifact>()
        var started = false
        val vm = viewModel(downloadResult = {
            started = true
            result.await()
        })
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                TeamCityTheme { ArtifactsRoute(router, false, true, vm) }
            }
        }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 1
        }
        compose.runOnIdle { router.actions.tryEmit(ArtifactAction.Download(ArtifactDownload("file.zip", "opaque/download"))) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            started
        }
        val title = RuntimeEnvironment.getApplication().getString(R.string.artifacts_downloading_title)
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        compose.onNodeWithText(title).assertDoesNotExist()
        assertFalse(result.isCompleted)
        assertTrue(router.files.isEmpty())
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.runOnIdle { vm.cancelDownload() }
        compose.waitForIdle()
        compose.onNodeWithText(title).assertDoesNotExist()
    }

    @Test fun platformErrorDialogBelongsOnlyToTheVisibleArtifactPage() {
        router.browserFails = true
        val visible = mutableStateOf(true)
        val vm = viewModel()
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides resumedOwner) { TeamCityTheme { ArtifactsRoute(router, false, visible.value, vm) } } }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.actions.subscriptionCount.value == 1
        }
        compose.runOnIdle { router.actions.tryEmit(ArtifactAction.Browser("/metadata/index.html")) }
        compose.waitForIdle()
        val error = RuntimeEnvironment.getApplication().getString(R.string.artifacts_browser_unavailable)
        compose.onNodeWithText(error).assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.onNodeWithText(error).assertDoesNotExist()
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.onNodeWithText(error).assertIsDisplayed()
    }

    private class FakeRouter : ArtifactsRouter {
        override val actions = MutableSharedFlow<ArtifactAction>(extraBufferCapacity = 4)
        val folders = mutableListOf<Triple<String, BuildLaunchData, String>>()
        val files = mutableListOf<DownloadedArtifact>()
        var failures = 0
        var disposals = 0
        var browserFails = false
        override fun navigateUp() {}
        override fun openFolder(name: String, build: BuildLaunchData, href: String) {
            folders.add(Triple(name, build, href))
        }
        override fun openActions(file: Artifact) {}
        override fun permission(file: ArtifactDownload) = ArtifactPermission.Allowed
        override fun openFile(file: DownloadedArtifact) {
            files.add(file)
        }
        override fun openBrowser(build: BuildLaunchData, href: String) {
            if (browserFails) throw IllegalStateException("No browser installed")
        }
        override fun downloadFailed() {
            failures++
        }
        override fun dispose() {
            disposals++
        }
    }
}
