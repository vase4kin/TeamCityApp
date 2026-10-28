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

import androidx.compose.runtime.*
import androidx.lifecycle.compose.*
import io.ktor.http.encodeURLQueryComponent

@Composable
fun SharedChangeDetailsRoute(vm: ChangeDetailsViewModel, onBack: () -> Unit, onBrowser: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(vm) {
        vm.onScreenViewed()
        onPauseOrDispose {}
    }
    ChangeDetailsScreen(state, {
        vm.onMoreDetailsClicked()
        onBrowser(it)
    }, { id, file ->
        vm.onFileDiffClicked()
        onBrowser(changeDiffUrl(id, file))
    }, onBack)
    LaunchedEffect(state) { if (state == ChangeDetailsUiState.InvalidInput) onBack() }
}

internal fun changeDiffUrl(id: String, file: String): String = "diffView.html?id=${id.encodeURLQueryComponent(encodeFull = true)}&vcsFileName=${file.encodeURLQueryComponent(encodeFull = true)}"
