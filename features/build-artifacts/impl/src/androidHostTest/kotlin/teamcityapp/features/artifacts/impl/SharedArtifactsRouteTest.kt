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
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.Json
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.impl.router.SharedArtifactsRouter
import teamcityapp.features.artifacts.impl.tracker.ArtifactsTracker
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SharedArtifactsRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val owner = Owner()
    private val router = Router()
    private val active = mutableStateOf(true)
    private val revision = mutableIntStateOf(0)
    private val recreation = mutableIntStateOf(0)

    @After fun cleanup() {
        store.clear()
    }

    private fun mount(download: suspend () -> DownloadedArtifact): ArtifactsViewModel {
        val model = ArtifactsViewModel(
            SavedStateHandle(mapOf(ArtifactsViewModel.JSON_BUILD to Json.encodeToString(BuildLaunchData("42", "/builds/42")), ArtifactsViewModel.URL to "/artifacts")),
            object : ArtifactsRepository {
                override suspend fun entries(url: String, forceRefresh: Boolean) = emptyList<Artifact>()
                override suspend fun download(file: ArtifactDownload) = download()
            },
            object : ArtifactsTracker {
                override fun viewShown() {}
            }
        )
        store.put("artifacts", model)
        compose.runOnUiThread { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                TeamCityTheme {
                    key(recreation.intValue) { SharedArtifactsRoute(model, router, owner.lifecycle, active.value, revision.intValue) }
                }
            }
        }
        compose.waitForIdle()
        return model
    }

    @Test fun completedFilesExportOnceAcrossResumeAndRecreationAndRetryWithoutRefetching() {
        var requests = 0
        val vm = mount {
            requests++
            DownloadedArtifact("/downloaded/report.txt")
        }
        router.acceptsFile = false
        compose.runOnIdle { vm.download(ArtifactDownload("report.txt", "/download")) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.files.size == 1
        }
        compose.runOnIdle { assertTrue((vm.state.value.download as ArtifactDownloadState.Ready).exportFailed) }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.runOnIdle { recreation.intValue++ }
        compose.waitForIdle()
        assertEquals(1, router.files.size)
        compose.runOnIdle {
            router.acceptsFile = true
            vm.openDownloadedFile()
        }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            router.files.size == 2
        }
        assertEquals(1, requests)
        assertEquals(listOf("/downloaded/report.txt", "/downloaded/report.txt"), router.files)
    }

    @Test fun departureAndNativeBackgroundCancelOnlyUnfinishedDownloads() {
        var starts = 0
        var cancellations = 0
        val vm = mount {
            starts++
            try {
                awaitCancellation()
            } finally {
                cancellations++
            }
        }
        compose.runOnIdle { vm.download(ArtifactDownload("report.txt", "/download")) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            starts == 1
        }
        compose.runOnIdle { active.value = false }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            cancellations == 1
        }
        compose.runOnIdle {
            active.value = true
            vm.download(ArtifactDownload("report.txt", "/download"))
        }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            starts == 2
        }
        compose.runOnIdle { router.background?.invoke() }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            cancellations == 2
        }
        assertTrue(router.files.isEmpty())
    }

    @Test fun unfinishedTransferSurvivesHostRecreationButCancelsOnRealBackgrounding() {
        var starts = 0
        var cancellations = 0
        val vm = mount {
            starts++
            try {
                awaitCancellation()
            } finally {
                cancellations++
            }
        }
        compose.runOnIdle { vm.download(ArtifactDownload("report.txt", "/download")) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            starts == 1
        }
        compose.runOnIdle {
            router.recreating = true
            owner.registry.currentState = Lifecycle.State.CREATED
            recreation.intValue++
        }
        compose.waitForIdle()
        assertEquals(0, cancellations)
        compose.runOnIdle {
            owner.registry.currentState = Lifecycle.State.RESUMED
            router.recreating = false
        }
        compose.waitForIdle()
        assertEquals(1, starts)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            cancellations == 1
        }
        assertTrue(router.files.isEmpty())
    }

    @Test fun sheetResultsWaitForResumeAndFolderNavigationCancelsTheTransferFirst() {
        var started = false
        var cancelled = false
        val vm = mount {
            started = true
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        compose.runOnIdle { vm.download(ArtifactDownload("report.txt", "/download")) }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            started
        }
        compose.runOnIdle {
            owner.registry.currentState = Lifecycle.State.STARTED
            router.pending += SheetItem(SheetAction.ArtifactOpen, "/children")
            revision.intValue++
        }
        compose.waitForIdle()
        assertTrue(router.folders.isEmpty())
        assertFalse(cancelled)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) {
            compose.waitForIdle()
            cancelled && router.folders.size == 1
        }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        assertEquals(1, router.folders.size)
        assertTrue(router.pending.isEmpty())
    }

    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
    private class Router : SharedArtifactsRouter {
        var recreating = false
        override fun isRecreatingHost() = recreating
        var background: (() -> Unit)? = null
        var acceptsFile = true
        val files = mutableListOf<String>()
        val folders = mutableListOf<String>()
        val pending = mutableListOf<SheetItem>()
        override fun close() {}
        override fun openFolder(name: String, href: String) {
            folders += href
        }
        override fun openActions(title: String, descriptions: List<String>, menu: SheetMenuType) {}
        override fun openBrowser(href: String) {}
        override fun openFile(file: DownloadedArtifact): Boolean {
            files += file.path
            return acceptsFile
        }
        override fun onBackground(cancel: () -> Unit): () -> Unit {
            background = cancel
            return { background = null }
        }
        override fun consumeAction() = if (pending.isEmpty()) null else pending.removeAt(0)
    }
}
