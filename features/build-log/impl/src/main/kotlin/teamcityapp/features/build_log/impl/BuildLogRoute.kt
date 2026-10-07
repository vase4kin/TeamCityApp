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

package teamcityapp.features.build_log.impl

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import teamcityapp.features.build_log.impl.router.BuildLogRouter

@Composable
fun BuildLogRoute(router: BuildLogRouter, pageDelay: Long, viewModel: BuildLogViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DisposableEffect(router) {
        router.initCustomsTabs()
        onDispose { router.unbindCustomsTabs() }
    }
    BuildLogScreen(state, viewModel::retry, viewModel::authenticate, router::openUrl) { session, modifier ->
        BuildLogWebContent(session.session.url, session.attempt, pageDelay, viewModel::pageStarted, viewModel::pageFinished, viewModel::pageFailed, modifier)
    }
}
