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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1455B8), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE8FF), onPrimaryContainer = Color(0xFF002D70),
    secondary = Color(0xFF505F7C), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E3FC), onSecondaryContainer = Color(0xFF25334E),
    tertiary = Color(0xFF65558F), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEBDDFF), onTertiaryContainer = Color(0xFF34245D),
    primaryFixed = Color(0xFFDCE8FF), primaryFixedDim = Color(0xFFAEC7FF),
    onPrimaryFixed = Color(0xFF001B3F), onPrimaryFixedVariant = Color(0xFF004395),
    secondaryFixed = Color(0xFFD9E3FC), secondaryFixedDim = Color(0xFFB9C7E4),
    onSecondaryFixed = Color(0xFF0D1B34), onSecondaryFixedVariant = Color(0xFF394764),
    tertiaryFixed = Color(0xFFEBDDFF), tertiaryFixedDim = Color(0xFFCDBDF3),
    onTertiaryFixed = Color(0xFF20113F), onTertiaryFixedVariant = Color(0xFF4D3D75),
    background = Color(0xFFF9F9FF), onBackground = Color(0xFF191C24),
    surface = Color(0xFFF9F9FF), onSurface = Color(0xFF191C24),
    surfaceVariant = Color(0xFFE1E5F0), onSurfaceVariant = Color(0xFF424754),
    surfaceDim = Color(0xFFD9DAE3), surfaceBright = Color(0xFFF9F9FF),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF0F3FC),
    surfaceContainer = Color(0xFFEAEDF7), surfaceContainerHigh = Color(0xFFE4E8F2),
    surfaceContainerHighest = Color(0xFFDEE2EC),
    outline = Color(0xFF737987), outlineVariant = Color(0xFFC3C8D5),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    inverseSurface = Color(0xFF2E3038), inverseOnSurface = Color(0xFFF0F0F8),
    inversePrimary = Color(0xFFAEC7FF), surfaceTint = Color(0xFF1455B8), scrim = Color.Black
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAEC7FF), onPrimary = Color(0xFF002F6C),
    primaryContainer = Color(0xFF004395), onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFFB9C7E4), onSecondary = Color(0xFF22314B),
    secondaryContainer = Color(0xFF394764), onSecondaryContainer = Color(0xFFD9E3FC),
    tertiary = Color(0xFFCDBDF3), onTertiary = Color(0xFF35275D),
    tertiaryContainer = Color(0xFF4D3D75), onTertiaryContainer = Color(0xFFEBDDFF),
    primaryFixed = Color(0xFFDCE8FF), primaryFixedDim = Color(0xFFAEC7FF),
    onPrimaryFixed = Color(0xFF001B3F), onPrimaryFixedVariant = Color(0xFF004395),
    secondaryFixed = Color(0xFFD9E3FC), secondaryFixedDim = Color(0xFFB9C7E4),
    onSecondaryFixed = Color(0xFF0D1B34), onSecondaryFixedVariant = Color(0xFF394764),
    tertiaryFixed = Color(0xFFEBDDFF), tertiaryFixedDim = Color(0xFFCDBDF3),
    onTertiaryFixed = Color(0xFF20113F), onTertiaryFixedVariant = Color(0xFF4D3D75),
    background = Color(0xFF11131B), onBackground = Color(0xFFE2E2EC),
    surface = Color(0xFF11131B), onSurface = Color(0xFFE2E2EC),
    surfaceVariant = Color(0xFF424754), onSurfaceVariant = Color(0xFFC3C8D5),
    surfaceDim = Color(0xFF11131B), surfaceBright = Color(0xFF373943),
    surfaceContainerLowest = Color(0xFF0C0E15), surfaceContainerLow = Color(0xFF191C24),
    surfaceContainer = Color(0xFF1E2029), surfaceContainerHigh = Color(0xFF282A33),
    surfaceContainerHighest = Color(0xFF33353E),
    outline = Color(0xFF8D93A2), outlineVariant = Color(0xFF424754),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE2E2EC), inverseOnSurface = Color(0xFF2E3038),
    inversePrimary = Color(0xFF1455B8), surfaceTint = Color(0xFFAEC7FF), scrim = Color.Black
)

/** Semantic status surfaces always accompany a label or icon. */
data class TeamCityStatusColor(val container: Color, val onContainer: Color)
data class TeamCityStatusColorScheme(val success: TeamCityStatusColor, val warning: TeamCityStatusColor, val info: TeamCityStatusColor)

@Composable
fun teamCityStatusColors(): TeamCityStatusColorScheme = if (LocalDarkTheme.current) {
    TeamCityStatusColorScheme(
        success = TeamCityStatusColor(Color(0xFF164B2D), Color(0xFFAEF4BE)),
        warning = TeamCityStatusColor(Color(0xFF594400), Color(0xFFFFE08A)),
        info = TeamCityStatusColor(DarkColorScheme.primaryContainer, DarkColorScheme.onPrimaryContainer)
    )
} else {
    TeamCityStatusColorScheme(
        success = TeamCityStatusColor(Color(0xFFC6F6D0), Color(0xFF003917)),
        warning = TeamCityStatusColor(Color(0xFFFFE7AA), Color(0xFF3C2D00)),
        info = TeamCityStatusColor(LightColorScheme.primaryContainer, LightColorScheme.onPrimaryContainer)
    )
}

/** Also used by mode previews without changing host system bars. */
fun teamCityColorScheme(darkTheme: Boolean): ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
