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

package teamcityapp.shared

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.settings.impl.SettingsScreen
import teamcityapp.features.settings.impl.SettingsUiState
import teamcityapp.libraries.accounts.AccountIdentity
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalRoborazziApi::class, ExperimentalTestApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlatformScreensScreenshotTest(private val scene: String, private val variant: Variant) {
    data class Variant(val name: String, val width: Int, val height: Int, val dark: Boolean, val scale: Float) {
        override fun toString() = name
    }
    private val compose = createComposeRule()
    private val device = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val qualifiers = RuntimeEnvironment.getQualifiers()
                val scale = RuntimeEnvironment.getFontScale()
                RuntimeEnvironment.setQualifiers("en-rUS-w${variant.width}dp-h${variant.height}dp-${if (variant.dark) "night" else "notnight"}-mdpi")
                RuntimeEnvironment.setFontScale(variant.scale)
                try {
                    base.evaluate()
                } finally {
                    RuntimeEnvironment.setQualifiers(qualifiers)
                    RuntimeEnvironment.setFontScale(scale)
                }
            }
        }
    }

    @get:Rule val rules: RuleChain = RuleChain.outerRule(device).around(compose)

    @Test fun rendersState() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(variant.dark) {
                when (scene) {
                    "settings_all_modes", "settings_all_modes_scrolled" -> SettingsScreen(SettingsUiState.Content(ThemeMode.System, ThemeMode.entries), {}, {}, {}, {})
                    "settings_auto_battery" -> SettingsScreen(SettingsUiState.Content(ThemeMode.AutoBattery, ThemeMode.entries), {}, {}, {}, {})
                    "license_content_no_source" -> NativeLicenseScreen("Android library", NativeLicenseText.Content("Apache License\nVersion 2.0, January 2004\n\nA packaged Android notice without a separate upstream URL."), {}, null, {})
                    "navigation_save_error" -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { NavigationSaveError({}, Modifier.align(Alignment.BottomCenter)) }
                    "licenses" -> NativeLicensesList({}, {})
                    "license_loading" -> NativeLicenseScreen("Kotlin — Apache 2.0", NativeLicenseText.Loading, {}, {}, {})
                    "license_error" -> NativeLicenseScreen("Kotlin — Apache 2.0", NativeLicenseText.Error, {}, {}, {})
                    "license_content" -> NativeLicenseScreen("Kotlin — Apache 2.0", NativeLicenseText.Content("Apache License\nVersion 2.0, January 2004\n\nTERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION\n\nThis deterministic fixture verifies the bundled text reader and its upstream action."), {}, {}, {})
                    else -> NativeFailureDialog(NativePlatformFailure.valueOf(scene), {})
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (scene == "settings_all_modes_scrolled") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("settings:option:System").performScrollTo().assertIsDisplayed()
            compose.waitForIdle()
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(100)
        }
        compose.onRoot().captureRoboImage("platform_${scene}_${variant.name}.png")
    }
    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases() = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(
                Variant("phone_$theme", 360, 800, dark, 1f),
                Variant("tablet_$theme", 1000, 700, dark, 1f),
                Variant("large_font_$theme", 360, 800, dark, 1.5f),
                Variant("double_font_$theme", 360, 800, dark, 2f)
            )
            (listOf("navigation_save_error", "licenses", "license_loading", "license_error", "license_content", "settings_all_modes", "settings_all_modes_scrolled", "settings_auto_battery", "license_content_no_source") + NativePlatformFailure.entries.map { it.name }).flatMap { scene ->
                variants.map { arrayOf<Any>(scene, it) }
            }
        }
    }
}
