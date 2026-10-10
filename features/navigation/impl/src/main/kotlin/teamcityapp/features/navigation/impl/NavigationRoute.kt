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

package teamcityapp.features.navigation.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import teamcityapp.features.navigation.impl.router.NavigationRouter

@Composable
internal fun NavigationRoute(
    router: NavigationRouter,
    root: Boolean,
    visible: Boolean = true,
    viewModel: NavigationViewModel = hiltViewModel()
) {
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
    if (visible) {
        LifecycleResumeEffect(state.openRating, viewModel) {
            if (state.openRating && viewModel.consumeRatingRequest()) router.openRating()
            onPauseOrDispose { }
        }
    }
    NavigationScreen(state, root, viewModel::refresh, viewModel::retry, router::navigateUp, router::open, viewModel::onRateCancel, viewModel::onRateNow, onRatingRetry = viewModel::retryRating)
}
