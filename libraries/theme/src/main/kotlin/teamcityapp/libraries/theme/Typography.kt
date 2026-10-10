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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val DefaultTypography = Typography()

internal val TeamCityTypography = Typography(
    displaySmall = DefaultTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = DefaultTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = DefaultTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = DefaultTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)

/** Technical values retain spacing and are selectable at their owning screen. */
val TeamCityMonospace: TextStyle
    @Composable get() = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
