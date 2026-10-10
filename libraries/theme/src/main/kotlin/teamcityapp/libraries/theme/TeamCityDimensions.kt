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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Shared layout dimensions in dp; text sizes belong to MaterialTheme.typography in sp. */
object TeamCityDimensions {
    val extraSmallSpacing = 4.dp
    val smallSpacing = 8.dp
    val contentPadding = 16.dp
    val sectionSpacing = 24.dp
    val iconSize = 24.dp
    val minimumTouchTarget = 48.dp
    val leadingContentWidth = 56.dp
    val paneMaxWidth = 640.dp
    val screenContentMaxWidth = 720.dp
    val emptyStateIconSize = 124.dp
}

internal val TeamCityShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
