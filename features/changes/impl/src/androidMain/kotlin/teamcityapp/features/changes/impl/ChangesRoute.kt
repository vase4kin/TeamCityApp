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

package teamcityapp.features.changes.impl

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import teamcityapp.features.changes.impl.router.ChangesRouter
import teamcityapp.libraries.list_ui.PagingListSnapshot

@Composable
internal fun ChangesRoute(router: ChangesRouter, viewModel: ChangesViewModel = hiltViewModel<AndroidChangesViewModel>(), parentCount: ChangesCountState? = null, onParentCountRetry: (() -> Unit)? = null) {
    SharedChangesContent(viewModel, router::openChange, router::updateTabCount, parentCount, onParentCountRetry, onUnavailableCount = { router.updateTabCount(0) })
}
