@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@file:Suppress("DEPRECATION")

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

package teamcityapp.features.filter_builds.impl

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.*
import teamcityapp.features.filter_builds.impl.router.FilterBuildsRouter

@Composable
fun SharedFilterBuildsRoute(router: FilterBuildsRouter, viewModel: FilterBuildsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.viewed()
        onPauseOrDispose {}
    }

    BackHandler { router.close() }
    FilterBuildsScreen(state, viewModel::update, { router.apply(viewModel.apply()) }, router::close)
}
