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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamCityColorSchemeTest {
    @Test fun fixedRolesStayBrandedAndStableAcrossModes() {
        assertEquals(fixedFamilies(LightColorScheme), fixedFamilies(DarkColorScheme))
        assertEquals(Color(0xFFDCE8FF), LightColorScheme.primaryFixed)
        fixedFamilies(LightColorScheme).forEach { family ->
            family.take(2).forEach { container ->
                family.drop(2).forEach { content -> assertContrast(content, container, 4.5f) }
            }
        }
    }

    @Test fun textAndInteractiveOutlinesHaveContrastInBothModes() {
        listOf(LightColorScheme, DarkColorScheme).forEach { scheme ->
            listOf(
                scheme.onPrimary to scheme.primary,
                scheme.onPrimaryContainer to scheme.primaryContainer,
                scheme.onSecondary to scheme.secondary,
                scheme.onSecondaryContainer to scheme.secondaryContainer,
                scheme.onTertiary to scheme.tertiary,
                scheme.onTertiaryContainer to scheme.tertiaryContainer,
                scheme.onError to scheme.error,
                scheme.onErrorContainer to scheme.errorContainer,
                scheme.inverseOnSurface to scheme.inverseSurface,
                scheme.onBackground to scheme.background,
                scheme.onSurfaceVariant to scheme.surfaceVariant
            ).forEach { (content, container) -> assertContrast(content, container, 4.5f) }
            listOf(
                scheme.surface,
                scheme.surfaceContainerLowest,
                scheme.surfaceContainerLow,
                scheme.surfaceContainer,
                scheme.surfaceContainerHigh,
                scheme.surfaceContainerHighest,
                scheme.surfaceDim,
                scheme.surfaceBright
            ).forEach { surface ->
                assertContrast(scheme.onSurface, surface, 4.5f)
                assertContrast(scheme.onSurfaceVariant, surface, 4.5f)
            }
            assertContrast(scheme.outline, scheme.surface, 3f)
        }
    }

    private fun fixedFamilies(scheme: ColorScheme) = listOf(
        listOf(scheme.primaryFixed, scheme.primaryFixedDim, scheme.onPrimaryFixed, scheme.onPrimaryFixedVariant),
        listOf(scheme.secondaryFixed, scheme.secondaryFixedDim, scheme.onSecondaryFixed, scheme.onSecondaryFixedVariant),
        listOf(scheme.tertiaryFixed, scheme.tertiaryFixedDim, scheme.onTertiaryFixed, scheme.onTertiaryFixedVariant)
    )

    private fun assertContrast(content: Color, container: Color, minimum: Float) {
        val a = content.luminance()
        val b = container.luminance()
        val contrast = (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
        assertTrue("$content on $container has contrast $contrast, expected >=$minimum", contrast >= minimum)
    }
}
