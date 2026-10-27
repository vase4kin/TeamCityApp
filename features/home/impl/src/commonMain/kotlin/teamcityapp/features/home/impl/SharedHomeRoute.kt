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

package teamcityapp.features.home.impl

import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import teamcityapp.libraries.navigation.*

@Composable
fun SharedHomeRoute(model: HomeViewModel, state: HomeUiState, stacks: Map<String, Navigator>, registry: DestinationRegistry, entryId: String, onReselect: () -> Unit, onAccount: () -> Unit) {
    LifecycleResumeEffect(model) {
        model.viewed()
        onPauseOrDispose {}
    }
    val snackbar = remember { SnackbarHostState() }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        HomeNavigationScreen(state, maxWidth >= 600.dp, stacks, registry, entryId, { tab ->
            if (tab == state.selected) onReselect()
            model.select(tab)
        }, model::selectFilter, model::retry, model::dismissPrompt, snackbar, onAccount, {
            if (state.isActivity) onReselect()
            model.selectActivity()
        })
    }
}
