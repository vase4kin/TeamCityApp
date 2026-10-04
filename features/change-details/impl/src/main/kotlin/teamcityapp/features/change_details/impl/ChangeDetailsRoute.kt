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

package teamcityapp.features.change_details.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import teamcityapp.features.change_details.impl.router.ChangeDetailsRouter

@Composable
internal fun ChangeDetailsRoute(router: ChangeDetailsRouter, viewModel: ChangeDetailsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) { if (state == ChangeDetailsUiState.InvalidInput) router.close() }
    if (state is ChangeDetailsUiState.Content) {
        LifecycleStartEffect(router) {
            router.start()
            onStopOrDispose { router.stop() }
        }
        LifecycleResumeEffect(viewModel) {
            viewModel.onScreenViewed()
            onPauseOrDispose { }
        }
    }
    ChangeDetailsScreen(state,
        onOpenUrl = { url -> viewModel.onMoreDetailsClicked(); router.openUrl(url) },
        onOpenDiff = { id, fileName -> viewModel.onFileDiffClicked(); router.openDiff(id, fileName) },
        onClose = router::close)
}
