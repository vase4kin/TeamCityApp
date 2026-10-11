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

package teamcityapp.libraries.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * Displays one child while retaining every child's ViewModel and saveable state in the parent
 * destination. Child visit IDs must be stable within, and unique to, the parent visit. Removing
 * a child clears its state; removing the parent clears all its child ViewModel stores.
 *
 * Only the selected child is composed. Hidden routes lose collectors and view resources without
 * losing completed ViewModel state. Tabs are siblings, so Back belongs to the parent navigator.
 */
@Composable
fun RetainedNavigationPane(
    children: List<NavigationEntry>,
    selectedEntryId: String,
    navigator: Navigator,
    registry: DestinationRegistry,
    modifier: Modifier = Modifier
) {
    require(children.map { it.id }.distinct().size == children.size) { "Child entry IDs must be unique" }
    require(children.any { it.id == selectedEntryId }) { "The selected child must exist" }
    val entries = rememberDecoratedNavEntries(
        backStack = children,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { registry.entry(it, navigator) }
    )
    // Decorate all children, but pass only the active child to NavDisplay. This keeps hidden
    // stores alive and prevents a sibling tab from becoming a system Back destination.
    NavDisplay(
        entries = listOf(entries.first { it.contentKey == selectedEntryId }),
        modifier = modifier,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        onBack = {}
    )
}
