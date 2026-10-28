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
import androidx.compose.ui.Modifier
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.libraries.theme.DRAWER_COACHMARK_ANCHOR
import teamcityapp.libraries.theme.coachmarkAnchor

@Composable
fun NavigationScreen(state: NavigationUiState, onRefresh: () -> Unit, onRetry: () -> Unit, onNavigateUp: () -> Unit, onEntryClick: (NavigationEntry) -> Unit, onRateCancel: () -> Unit, onRateNow: () -> Unit, modifier: Modifier = Modifier, onRatingRetry: () -> Unit = onRetry) = SharedNavigationScreen(state, onRefresh, onRetry, onNavigateUp, onEntryClick, onRateCancel, onRateNow, modifier, onRatingRetry, if (state.root) Modifier.coachmarkAnchor(DRAWER_COACHMARK_ANCHOR) else Modifier)

val NavigationUiState.navigationLabelRes: Int get() = if (root) R.string.navigation_open_drawer else R.string.navigation_back
