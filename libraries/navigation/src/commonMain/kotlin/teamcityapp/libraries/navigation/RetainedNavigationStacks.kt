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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * Retains every tab's saved visits and owners while composing only the selected stack.
 * Back pops the active stack. At a tab's root, the containing navigation host owns Back.
 * Removing the containing destination clears the owners of every stack, including hidden tabs.
 * Root content belongs to its visit, so its header and constraints remain stable during transitions.
 */
@Composable
fun RetainedNavigationStacks(
    stacks: Map<String, Navigator>,
    selectedStackId: String,
    registry: DestinationRegistry,
    parentEntryId: String,
    modifier: Modifier = Modifier,
    rootContent: @Composable (String, @Composable () -> Unit) -> Unit = { _, content -> content() }
) {
    require(selectedStackId in stacks) { "The selected stack must exist" }
    val latestRootContent by rememberUpdatedState(rootContent)
    val selected = stacks.getValue(selectedStackId)
    DisposableEffect(selected, parentEntryId) {
        val detach = selected.attachAsActiveChild(parentEntryId)
        onDispose { detach() }
    }
    val entryOwner = LocalNavigationEntryLifecycleOwner.current ?: LocalLifecycleOwner.current
    CompositionLocalProvider(LocalLifecycleOwner provides entryOwner) {
        val entries = stacks.mapValues { (id, navigator) ->
            key(id) {
                val rootId = navigator.snapshot.entries.first().id
                rememberDecoratedNavEntries(
                    backStack = navigator.snapshot.entries,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = { visit ->
                        val entry = registry.entry(visit, navigator)
                        if (visit.id == rootId) {
                            NavEntry(visit, contentKey = entry.contentKey, metadata = entry.metadata) {
                                latestRootContent(id) { entry.Content() }
                            }
                        } else {
                            entry
                        }
                    }
                )
            }
        }
        // Keep decorators outside the display key so changing tabs releases collectors and
        // platform resources immediately, but retains every tab's ViewModels and saved state.
        key(selectedStackId) {
            NavDisplay(
                entries = entries.getValue(selectedStackId),
                modifier = modifier,
                sceneStrategies = listOf(BottomSheetSceneStrategy()),
                onBack = { stacks.getValue(selectedStackId).pop() }
            )
        }
    }
}
