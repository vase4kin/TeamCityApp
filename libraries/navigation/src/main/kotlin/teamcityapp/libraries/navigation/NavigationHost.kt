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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import java.util.UUID

@Composable
fun rememberNavigator(initialDestination: NavKey, registry: DestinationRegistry, sessionKey: String = "initial", parent: Navigator? = null, useParent: (NavKey) -> Boolean = { false }): Navigator {
    val configuration = remember(registry) { SavedStateConfiguration { serializersModule = registry.serializers } }
    val snapshot = rememberSerializable(
        sessionKey,
        configuration = configuration,
        serializer = MutableStateSerializer(NavigationSnapshot.serializer())
    ) {
        mutableStateOf(NavigationSnapshot(listOf(NavigationEntry(UUID.randomUUID().toString(), initialDestination)), sessionKey = sessionKey))
    }
    // Saveable inputs do not validate an account key restored from a previous process.
    // Replace old-session owners before any destination is composed.
    if (snapshot.value.sessionKey != sessionKey) {
        snapshot.value = NavigationSnapshot(listOf(NavigationEntry(UUID.randomUUID().toString(), initialDestination)), sessionKey = sessionKey)
    }
    return remember(snapshot, parent) { Navigator(snapshot, parent = parent, useParent = useParent) }
}

@Composable
fun NavigationHost(navigator: Navigator, registry: DestinationRegistry, onExit: () -> Unit, modifier: Modifier = Modifier) {
    NavDisplay(
        backStack = navigator.snapshot.entries,
        modifier = modifier,
        onBack = { if (!navigator.pop()) onExit() },
        sceneStrategies = listOf(BottomSheetSceneStrategy()),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { registry.entry(it, navigator) }
    )
}
