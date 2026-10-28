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

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.impl.resources.artifacts_title
import teamcityapp.features.artifacts.impl.router.SharedArtifactsRouter
import teamcityapp.features.artifacts.impl.router.artifactMenu
import teamcityapp.features.bottom_sheet.api.*

@Composable
fun SharedArtifactsRoute(vm: ArtifactsViewModel, router: SharedArtifactsRouter, lifecycle: Lifecycle, screenActive: Boolean, actionRevision: Any) {
    val state by vm.state.collectAsStateWithLifecycle()
    DisposableEffect(router, vm) {
        val unsubscribe = router.onBackground(vm::cancelUnfinishedDownload)
        onDispose { unsubscribe() }
    }
    ArtifactDownloadLifetime(lifecycle, screenActive, router::isRecreatingHost, vm::cancelUnfinishedDownload)
    LifecycleResumeEffect(vm) {
        vm.onResumed()
        onPauseOrDispose {}
    }
    LifecycleResumeEffect(actionRevision, vm) {
        while (true) {
            val item = router.consumeAction() ?: break
            when (item.action) {
                SheetAction.ArtifactDownload -> vm.download(ArtifactDownload(item.fileName, item.description))

                SheetAction.ArtifactOpen -> {
                    vm.cancelUnfinishedDownload()
                    router.openFolder(item.fileName, item.description)
                }

                SheetAction.ArtifactBrowser -> router.openBrowser(item.description)

                else -> Unit
            }
        }
        onPauseOrDispose {}
    }
    LifecycleResumeEffect(state.download, vm) {
        (state.download as? ArtifactDownloadState.Ready)?.let { ready ->
            vm.claimDownloadedFile(ready.token)?.let { file ->
                if (!router.openFile(file)) vm.exportFailed(ready.token)
            }
        }
        onPauseOrDispose {}
    }
    fun menu(file: Artifact) {
        val (menu, values) = artifactMenu(file)
        router.openActions(file.name, values.toList(), menu)
    }
    ArtifactsScreen(state, true, null, vm::refresh, vm::retry, {
        vm.cancelUnfinishedDownload()
        router.close()
    }, { file ->
        if (file.isFolder && file.childrenHref != null) {
            vm.cancelUnfinishedDownload()
            router.openFolder(file.name, checkNotNull(file.childrenHref))
        } else {
            menu(file)
        }
    }, ::menu, vm::retryDownload, vm::cancelDownload, {}, onOpenDownloadedFile = vm::openDownloadedFile)
}

@Composable
fun artifactScreenTitle(name: String): String = name.ifEmpty { org.jetbrains.compose.resources.stringResource(teamcityapp.features.artifacts.impl.resources.Res.string.artifacts_title) }
