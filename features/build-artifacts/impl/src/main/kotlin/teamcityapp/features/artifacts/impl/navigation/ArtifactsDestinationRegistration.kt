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

package teamcityapp.features.artifacts.impl.navigation

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.artifacts.impl.*
import teamcityapp.features.artifacts.impl.ArtifactsViewModel
import teamcityapp.features.artifacts.impl.R
import teamcityapp.features.artifacts.impl.router.*
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.api.navigation.*
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.navigation.*

class ArtifactsDestinationRegistration @Inject constructor(private val platforms: ArtifactsPlatformRouterFactory) : FeatureDestination<ArtifactsDestination>(ArtifactsDestination::class, ArtifactsDestination.serializer()) {
    @Composable override fun Content(destination: ArtifactsDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val recipient = LocalBuildDetailsUiHost.current?.entryId ?: LocalNavigationResultRecipient.current ?: entryId
        val arguments = remember(destination) {
            Bundle().apply {
                putString(ArtifactsViewModel.JSON_BUILD, Json.encodeToString(destination.build))
                putString(ArtifactsViewModel.URL, destination.href)
                putString(ArtifactsViewModel.NAME, destination.name)
            }
        }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val failed = stringResource(R.string.artifacts_download_failed)
        val events = remember(entryId) { Channel<ArtifactAction>(Channel.UNLIMITED) }
        DisposableEffect(events) { onDispose { events.close() } }
        val router = remember(activity, navigator, entryId, recipient, events) {
            val platform = platforms.create(activity)
            object : ArtifactsRouter {
                override val actions = events.receiveAsFlow()
                override fun navigateUp() {
                    if (!navigator.pop()) activity.finish()
                }
                override fun openFolder(name: String, build: BuildLaunchData, href: String) {
                    navigator.push(ArtifactsDestination(build, href, name))
                }
                override fun openActions(file: Artifact) {
                    val (menu, values) = artifactMenu(file)
                    navigator.push(ActionSheetDestination(file.name, values.toList(), menu, recipient, entryId))
                }
                override fun permission(file: ArtifactDownload) = platform.permission(activity, file)
                override fun openFile(file: DownloadedArtifact) = platform.openDownloadedFile(activity, file)
                override fun openBrowser(build: BuildLaunchData, href: String) = platform.openBrowser(activity, build, href)
                override fun downloadFailed() {
                    scope.launch { snackbar.showSnackbar(failed) }
                }
                override fun dispose() = platform.dispose(activity)
            }
        }
        DestinationArguments(arguments) {
            LifecycleResumeEffect(navigator.snapshot.results, router) {
                while (true) {
                    val result = navigator.consumeResult(recipient, ActionSheetResult::class) { it.consumerId == entryId } ?: break
                    val item = result.item
                    when (item.action) {
                        SheetAction.ArtifactDownload -> events.trySend(ArtifactAction.Download(ArtifactDownload(item.fileName, item.description)))
                        SheetAction.ArtifactOpen -> events.trySend(ArtifactAction.Open(item.fileName, item.description))
                        SheetAction.ArtifactBrowser -> events.trySend(ArtifactAction.Browser(item.description))
                        else -> Unit
                    }
                }
                onPauseOrDispose {}
            }
            Box(Modifier.fillMaxSize()) {
                ArtifactsRoute(router, destination.showToolbar)
                SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ArtifactsDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: ArtifactsDestinationRegistration): DestinationRegistration
}
