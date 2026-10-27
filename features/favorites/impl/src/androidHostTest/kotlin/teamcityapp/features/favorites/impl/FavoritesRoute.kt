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

package teamcityapp.features.favorites.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import teamcityapp.features.favorites.api.LocalFavoritesProjectsAction
import teamcityapp.features.favorites.impl.router.FavoritesRouter
import teamcityapp.libraries.list_ui.ConfigurationRecreationEffect

// Regression harness for explicit fake ViewModels; production uses shared-app routes.
@Composable
internal fun FavoritesRoute(router: FavoritesRouter, viewModel: FavoritesViewModel, visible: Boolean = true) {
    ConfigurationRecreationEffect(viewModel) { viewModel.onConfigurationRecreation(wasVisible = visible) }
    if (visible) {
        LifecycleResumeEffect(viewModel) {
            viewModel.onResumed()
            onPauseOrDispose { viewModel.onPaused() }
        }
    }
    val state = if (visible) {
        val collected by viewModel.state.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
        collected
    } else {
        remember(viewModel) { viewModel.state.value }
    }
    FavoritesScreen(state, viewModel::refresh, viewModel::retry, router::openDrawer, router::openProject, router::openConfiguration, onBrowseProjects = LocalFavoritesProjectsAction.current ?: router::openProjects)
}
