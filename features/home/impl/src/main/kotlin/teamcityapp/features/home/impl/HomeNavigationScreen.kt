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

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.navigation.DestinationRegistry
import teamcityapp.libraries.navigation.Navigator
import teamcityapp.libraries.navigation.RetainedNavigationStacks

/** Root headers travel with their visits; the surrounding navigation keeps fixed constraints. */
@Composable
internal fun HomeNavigationScreen(
    state: HomeUiState,
    expanded: Boolean,
    stacks: Map<String, Navigator>,
    registry: DestinationRegistry,
    entryId: String,
    onTabSelected: (HomeTab) -> Unit,
    onFilter: (QuickFilter) -> Unit,
    onRetry: () -> Unit,
    onDismissPrompt: () -> Unit,
    snackbar: SnackbarHostState,
    onAccount: () -> Unit,
    onActivity: () -> Unit
) {
    HomeScreen(
        state,
        expanded,
        onTabSelected,
        onFilter,
        onRetry,
        onDismissPrompt,
        snackbar,
        onAccount = onAccount,
        onActivity = onActivity,
        showHomeHeader = false
    ) {
        RetainedNavigationStacks(stacks, "$entryId/${state.selected.name}", registry, entryId) { stackId, content ->
            val tab = HomeTab.entries.first { "$entryId/${it.name}" == stackId }
            HomeScreen(
                state.copy(selected = tab),
                expanded = false,
                onTabSelected = onTabSelected,
                onFilterSelected = onFilter,
                onRetry = onRetry,
                onDismissPrompt = onDismissPrompt,
                snackbar = snackbar,
                onAccount = onAccount,
                showNavigation = false,
                content = content
            )
        }
    }
}
