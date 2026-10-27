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

package teamcityapp.features.about.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import teamcityapp.features.about.api.AboutRepository

/** The destination owns the ViewModel; platforms supply repositories and outgoing UI actions. */
@Composable
fun SharedAboutRoute(
    repository: AboutRepository,
    appVersion: String,
    onOpenUrl: (String) -> Unit,
    onAction: (AboutAction) -> Unit,
    onClose: () -> Unit
) {
    val viewModel = viewModel { AboutViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    SharedAboutScreen(state, onOpenUrl, onAction, onClose, appVersion)
}

/** The host supplies OS capabilities; About owns its action destinations. */
@Composable
fun SharedAboutRoute(
    repository: AboutRepository,
    appVersion: String,
    onOpenUrl: (String) -> Unit,
    onRate: () -> Unit,
    onLibraries: () -> Unit,
    onClose: () -> Unit
) = SharedAboutRoute(repository, appVersion, onOpenUrl, { action ->
    performAboutAction(action, onOpenUrl, onRate, onLibraries)
}, onClose)

internal fun performAboutAction(action: AboutAction, onOpenUrl: (String) -> Unit, onRate: () -> Unit, onLibraries: () -> Unit) {
    when (action) {
        AboutAction.Rate -> onRate()
        AboutAction.Libraries -> onLibraries()
        AboutAction.Issue -> onOpenUrl("https://github.com/vase4kin/TeamCityApp/issues/new/choose")
        AboutAction.Source -> onOpenUrl("https://github.com/vase4kin/TeamCityApp")
        AboutAction.Website -> onOpenUrl("https://teamcity.app")
        AboutAction.Email -> onOpenUrl("mailto:contact@teamcity.app?subject=Questions%20about%20TeamCityApp")
        AboutAction.Privacy -> onOpenUrl("https://telegra.ph/TeamCityApp-Private-Policy-09-18")
    }
}
