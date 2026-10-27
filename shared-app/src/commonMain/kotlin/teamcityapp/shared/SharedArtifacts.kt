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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.json.Json
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.artifacts.impl.*
import teamcityapp.features.artifacts.impl.router.SharedArtifactsRouter
import teamcityapp.features.artifacts.impl.tracker.ArtifactsTrackerImpl
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.api.navigation.*
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph

internal fun sharedArtifactEntries(graph: TeamCityAccountGraph?): Set<DestinationRegistration> {
    val repository = graph?.artifacts ?: return emptySet()
    return setOf(
        SharedEntry(ArtifactsDestination::class, ArtifactsDestination.serializer()) { destination, nav, id ->
            val platform = LocalNativePlatform.current
            val analytics = platform.analytics
            val title = artifactScreenTitle(destination.name)
            val vm = viewModel {
                ArtifactsViewModel(
                    destinationState(mapOf(ArtifactsViewModel.JSON_BUILD to Json.encodeToString(destination.build), ArtifactsViewModel.URL to destination.href, ArtifactsViewModel.NAME to title)),
                    repository,
                    ArtifactsTrackerImpl(analytics)
                )
            }
            val router = remember(platform, nav, destination, id) {
                object : SharedArtifactsRouter {
                    override fun close() {
                        nav.pop()
                    }
                    override fun openFolder(name: String, href: String) {
                        nav.push(destination.copy(name = name, href = href))
                    }
                    override fun openActions(title: String, descriptions: List<String>, menu: SheetMenuType) {
                        nav.push(ActionSheetDestination(title, descriptions, menu, id, id))
                    }
                    override fun openBrowser(href: String) {
                        runCatching { artifactBrowserUrl(graph.account.identity.serverUrl, destination.build, href, graph.account.identity.guest) }
                            .onSuccess(platform::browser).onFailure { platform.rejected(NativePlatformFailure.Browser) }
                    }
                    override fun openFile(file: DownloadedArtifact) = platform.openFile(file.path)
                    override fun onBackground(cancel: () -> Unit) = platform.onBackground(cancel)
                    override fun consumeAction() = nav.consumeResult(id, ActionSheetResult::class) { it.consumerId == id }?.item
                }
            }
            val owner = nav.snapshot.entries.lastOrNull { it.destination !is ActionSheetDestination }?.id
            SharedArtifactsRoute(vm, router, LocalNativeAppLifecycle.current, owner == id, nav.snapshot.results)
        }
    )
}
