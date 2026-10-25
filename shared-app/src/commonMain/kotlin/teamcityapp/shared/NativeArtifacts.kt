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

package teamcityapp.shared

import androidx.compose.runtime.*
import androidx.lifecycle.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.json.Json
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.artifacts.impl.*
import teamcityapp.features.artifacts.impl.router.artifactMenu
import teamcityapp.features.artifacts.impl.tracker.ArtifactsTracker
import teamcityapp.features.artifacts.presentation.resources.*
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.api.navigation.*
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph

internal fun nativeArtifactEntries(graph: TeamCityAccountGraph?): Set<DestinationRegistration> {
    val repository = graph?.artifacts ?: return emptySet()
    return setOf(
        SharedEntry(ArtifactsDestination::class, ArtifactsDestination.serializer()) { destination, nav, id ->
            val platform = LocalNativePlatform.current
            val diagnostics = platform.diagnostics
            val title = destination.name.ifEmpty { org.jetbrains.compose.resources.stringResource(teamcityapp.features.artifacts.presentation.resources.Res.string.artifacts_title) }
            val vm = viewModel {
                ArtifactsViewModel(
                    destinationState(mapOf(ArtifactsViewModel.JSON_BUILD to Json.encodeToString(destination.build), ArtifactsViewModel.URL to destination.href, ArtifactsViewModel.NAME to title)),
                    repository,
                    object : ArtifactsTracker {
                        override fun viewShown() {
                            diagnostics.event("screen_artifacts")
                        }
                    }
                )
            }
            val state by vm.state.collectAsStateWithLifecycle()
            DisposableEffect(platform, vm) {
                val unsubscribe = platform.onBackground(vm::cancelUnfinishedDownload)
                onDispose { unsubscribe() }
            }
            val owner = nav.snapshot.entries.lastOrNull { it.destination !is ActionSheetDestination }?.id
            ArtifactDownloadLifetime(LocalNativeAppLifecycle.current, owner == id, { false }, vm::cancelUnfinishedDownload)
            LifecycleResumeEffect(vm) {
                vm.onResumed()
                onPauseOrDispose {}
            }
            LifecycleResumeEffect(nav.snapshot.results, vm) {
                while (true) {
                    val result = nav.consumeResult(id, ActionSheetResult::class) { it.consumerId == id } ?: break
                    val item = result.item
                    when (item.action) {
                        SheetAction.ArtifactDownload -> vm.download(ArtifactDownload(item.fileName, item.description))

                        SheetAction.ArtifactOpen -> {
                            vm.cancelUnfinishedDownload()
                            nav.push(destination.copy(href = item.description, name = item.fileName))
                        }

                        SheetAction.ArtifactBrowser -> runCatching { artifactBrowserUrl(graph.account.identity.serverUrl, destination.build, item.description) }.onSuccess(platform::browser).onFailure { platform.rejected(NativePlatformFailure.Browser) }.let { }

                        else -> Unit
                    }
                }
                onPauseOrDispose {}
            }
            LifecycleResumeEffect(state.download, vm) {
                (state.download as? ArtifactDownloadState.Ready)?.let { vm.claimDownloadedFile(it.token)?.let { platform.openFile(it.path) } }
                onPauseOrDispose {}
            }
            fun menu(file: Artifact) {
                val (menu, values) = artifactMenu(file)
                nav.push(ActionSheetDestination(file.name, values.toList(), menu, id, id))
            }
            ArtifactsScreen(state, true, null, vm::refresh, vm::retry, {
                vm.cancelUnfinishedDownload()
                nav.pop()
            }, { file ->
                if (file.isFolder && file.childrenHref != null) {
                    vm.cancelUnfinishedDownload()
                    nav.push(destination.copy(href = checkNotNull(file.childrenHref), name = file.name))
                } else {
                    menu(file)
                }
            }, ::menu, vm::retryDownload, vm::cancelDownload, {})
        }
    )
}
