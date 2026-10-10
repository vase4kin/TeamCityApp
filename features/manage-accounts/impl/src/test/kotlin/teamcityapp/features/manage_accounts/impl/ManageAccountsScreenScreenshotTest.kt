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

package teamcityapp.features.manage_accounts.impl

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
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
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.libraries.theme.TeamCityTheme

/** Every account and dialog state, in both themes, sizes, and enlarged text. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ManageAccountsScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
    data class Variant(val name: String, val width: Int, val height: Int, val dark: Boolean, val fontScale: Float, val rtl: Boolean = false) {
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
        val primary = ManagedAccount(ManagedAccountId("https://teamcity.example/primary", "Guest user"), false, false)
        val secondary = ManagedAccount(ManagedAccountId("https://teamcity.example/secondary", "Guest user"), true, true)
        val accounts = when (stateName) {
            "normal" -> listOf(primary)
            "named" -> listOf(primary.copy(id = primary.id.copy(userName = "alex.morgan"), isActive = true))
            "named_long" -> listOf(primary.copy(id = primary.id.copy(userName = "alexander.morgan.platform"), isActive = true))
            "long", "scrolled" -> (1..16).map { ManagedAccount(ManagedAccountId("https://a-very-long-teamcity-server.example/projects/production/$it", "Developer with a very long full name $it"), it == 1, it % 2 == 0) }
            else -> listOf(primary, secondary)
        }
        val list = when (stateName) {
            "loading" -> AccountListUiState.Loading
            "empty", "cleanup_error" -> AccountListUiState.Empty
            "error" -> AccountListUiState.Error
            else -> AccountListUiState.Content(accounts)
        }
        val removal = when (stateName) {
            "removing" -> AccountRemovalUiState.Removing(primary.id)
            "remove_error", "cleanup_error" -> AccountRemovalUiState.Error(primary.id)
            else -> AccountRemovalUiState.Idle
        }
        val dialog = when (stateName) {
            "ssl_dialog" -> ManageAccountsDialog.SslWarning
            "remove_dialog" -> ManageAccountsDialog.ConfirmRemoval(primary.id)
            else -> ManageAccountsDialog.None
        }
        compose.mainClock.autoAdvance = !(stateName in listOf("loading", "removing"))
        compose.setContent { CompositionLocalProvider(LocalLayoutDirection provides if (variant.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { TeamCityTheme(darkTheme = variant.dark) { ManageAccountsScreen(ManageAccountsUiState(list, removal), {}, {}, {}, {}, dialog = dialog) } } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (stateName == "scrolled") {
            compose.onNodeWithTag("accounts:list").performScrollToIndex(accounts.lastIndex)
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
        }
        val name = "accounts_${stateName}_${variant.name}"
        if (dialog != ManageAccountsDialog.None) {
            captureScreenRoboImage("$name.png")
        } else {
            compose.onRoot().captureRoboImage("$name.png")
            if (stateName == "remove_error") {
                compose.onNodeWithTag("${accountTag(secondary.id)}:ssl").performScrollTo().assertIsDisplayed()
                compose.mainClock.advanceTimeBy(500)
                compose.waitForIdle()
                compose.onRoot().captureRoboImage("${name}_bottom.png")
            }
        }
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
                Variant("double_font_$theme", 360, 800, dark, 2f)
            )
            val existing = listOf("loading", "empty", "error", "normal", "mixed", "long", "scrolled", "removing", "remove_error", "cleanup_error", "ssl_dialog", "remove_dialog").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            }
            existing + listOf(
                arrayOf<Any>("named", Variant("phone_$theme", 360, 800, dark, 1f)),
                arrayOf<Any>("named_long", Variant("double_font_$theme", 360, 800, dark, 2f)),
                arrayOf<Any>("named_long", Variant("rtl_large_font_$theme", 360, 800, dark, 1.5f, rtl = true))
            )
        }
    }
}
