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

package teamcityapp.libraries.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility

internal expect val clipsAccessibleActionRows: Boolean

/**
 * UIKit clips action frames to the scroll viewport. A narrow visible strip must
 * not become a tiny VoiceOver target; scrolling reveals the row and its action.
 * Apply before clickable semantics, on rows that already meet the minimum touch target.
 */
@Composable
fun Modifier.hideClippedActionFromAccessibility(enabled: Boolean): Modifier {
    if (!enabled || !clipsAccessibleActionRows) return this
    var hidden by remember { mutableStateOf(true) }
    val minimum = with(LocalDensity.current) { TeamCityDimensions.minimumTouchTarget.toPx() }
    return onGloballyPositioned { coordinates ->
        val visible = coordinates.boundsInWindow()
        hidden = visible.width < minimum || visible.height < minimum
    }.then(if (hidden) Modifier.clearAndSetSemantics { hideFromAccessibility() } else Modifier)
}
