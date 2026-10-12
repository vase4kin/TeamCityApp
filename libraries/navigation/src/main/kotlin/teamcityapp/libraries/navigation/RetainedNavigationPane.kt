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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    modifier: Modifier = Modifier,
    resultRecipientId: String? = null
) {
    require(children.map { it.id }.distinct().size == children.size) { "Child entry IDs must be unique" }
    require(children.any { it.id == selectedEntryId }) { "The selected child must exist" }
    val entryOwner = LocalNavigationEntryLifecycleOwner.current ?: LocalLifecycleOwner.current
    CompositionLocalProvider(LocalLifecycleOwner provides entryOwner, LocalNavigationResultRecipient provides (resultRecipientId ?: LocalNavigationResultRecipient.current)) {
        val entries = rememberDecoratedNavEntries(
            backStack = children,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { registry.entry(it, navigator) }
        )
        // Ownership stays outside the selected display's key so hidden stores remain alive.
        // Start each sibling's display at its own scene: even a no-animation scene transition
        // otherwise keeps the previous child's content until a later frame, after the parent's
        // header and constraints have already changed. Siblings are not Back destinations.
        key(selectedEntryId) {
            NavDisplay(
                entries = listOf(entries.first { it.contentKey == selectedEntryId }),
                modifier = modifier,
                transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                onBack = {}
            )
        }
    }
}
