/*
 * Copyright 2026 Andrey Tolpeev
 * Copyright 2025 The Android Open Source Project
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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

enum class DestinationPresentation {
    Screen,
    BottomSheet;

    internal val metadata: Map<String, Any>
        get() = when (this) {
            Screen -> emptyMap()
            BottomSheet -> metadata { put(BottomSheetKey, true) }
        }
}

private object BottomSheetKey : NavMetadataKey<Boolean>

@Stable
class SheetInteractions {
    var enabled by mutableStateOf(true)
        private set
    fun updateEnabled(value: Boolean) {
        enabled = value
    }
}
val LocalSheetInteractions = staticCompositionLocalOf<SheetInteractions?> { null }

/** Compose sheets participate in the same saved back stack and entry ownership as screens. */
internal class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val sheet = entries.lastOrNull() ?: return null
        if (sheet.metadata[BottomSheetKey] != true) return null
        require(entries.size > 1) { "A bottom sheet requires an underlying destination" }
        return BottomSheetScene(sheet, entries.dropLast(1), onBack)
    }
}

// Adapted from Android's Navigation 3 bottom sheet recipe. The key is the visit ID rather than
// its payload so equal sheet arguments still receive independent saveable state and ViewModels.
@OptIn(ExperimentalMaterial3Api::class)
private data class BottomSheetScene<T : Any>(
    val sheet: NavEntry<T>,
    override val overlaidEntries: List<NavEntry<T>>,
    val onDismiss: () -> Unit
) : OverlayScene<T> {
    override val key: Any = sheet.contentKey
    override val entries: List<NavEntry<T>> = listOf(sheet)
    override val previousEntries: List<NavEntry<T>> = overlaidEntries
    override val content: @Composable () -> Unit = {
        val owner = rememberLifecycleOwner()
        val interactions = remember { SheetInteractions() }
        ModalBottomSheet(
            modifier = Modifier.testTag("navigation:sheet"),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { if (interactions.enabled) onDismiss() },
            sheetGesturesEnabled = interactions.enabled,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = interactions.enabled,
                shouldDismissOnClickOutside = interactions.enabled
            )
        ) {
            CompositionLocalProvider(LocalLifecycleOwner provides owner, LocalSheetInteractions provides interactions) { sheet.Content() }
        }
    }
}
