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

package teamcityapp.features.settings.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.app_theme.ThemeOptions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.junit.runners.model.Statement
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.theme.TeamCityTheme

/** Every Settings state, in both themes, sizes, and enlarged text. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
    data class Variant(val name: String, val width: Int, val height: Int, val dark: Boolean, val fontScale: Float) {
        override fun toString() = name
    }

    private val compose = createComposeRule()
    private val device = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val qualifiers = RuntimeEnvironment.getQualifiers()
                val fontScale = RuntimeEnvironment.getFontScale()
                RuntimeEnvironment.setQualifiers(
                    "en-rUS-w${variant.width}dp-h${variant.height}dp-${if (variant.dark) "night" else "notnight"}-mdpi"
                )
                RuntimeEnvironment.setFontScale(variant.fontScale)
                try {
                    base.evaluate()
                } finally {
                    RuntimeEnvironment.setQualifiers(qualifiers)
                    RuntimeEnvironment.setFontScale(fontScale)
                }
            }
        }
    }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(device).around(compose)

    @Test fun rendersState() {
        val options = when(stateName) {
            "auto_battery", "dialog_battery" -> ThemeOptions.forSdk(28)
            "dialog_legacy" -> ThemeOptions.forSdk(24)
            else -> ThemeOptions.forSdk(35)
        }
        val selected = when(stateName) {
            "light", "dialog_light", "dialog_legacy" -> ThemeMode.Light
            "dark", "dialog_dark" -> ThemeMode.Dark
            "auto_battery", "dialog_battery", "unavailable" -> ThemeMode.AutoBattery
            else -> ThemeMode.System
        }
        val state = when(stateName) {
            "loading" -> SettingsUiState.Loading
            "error" -> SettingsUiState.Error
            else -> SettingsUiState.Content(selected, options.modes, saving = stateName == "saving", saveFailed = stateName == "save_error")
        }
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { SettingsScreen(state, {}, {}, {}, {}, dialogOpen = stateName.startsWith("dialog")) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val name = "settings_${stateName}_${variant.name}"
        if(stateName.startsWith("dialog")) compose.onNodeWithTag("settings:dialog").captureRoboImage("$name.png")
        else compose.onRoot().captureRoboImage("$name.png")
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(
                Variant("phone_$theme", 360, 800, dark, 1f),
                Variant("tablet_$theme", 1000, 700, dark, 1f),
                Variant("large_font_$theme", 360, 800, dark, 1.5f),
            )
            listOf("loading", "error", "light", "dark", "system", "auto_battery", "unavailable", "saving", "save_error", "dialog_light", "dialog_dark", "dialog_system", "dialog_battery", "dialog_legacy").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
        }
    }
}
