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

package teamcityapp.features.create_account.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.theme.TeamCityTheme

/** Every account and dialog state, in both themes, sizes, and enlarged text. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CreateAccountScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
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
        val error = when (stateName) {
            "empty_url" -> AuthenticationError.EmptyUrl
            "empty_user" -> AuthenticationError.EmptyUserName
            "empty_password" -> AuthenticationError.EmptyPassword
            "save_error" -> AuthenticationError.SaveFailed
            "duplicate" -> AuthenticationError.DuplicateAccount
            "server_error" -> AuthenticationError.Server("Unable to connect to TeamCity server")
            else -> null
        }
        val form = AuthenticationFormState(serverUrl = if (stateName == "empty_url") "" else "https://teamcity.example/server", userName = if (stateName == "filled") "developer" else "", password = if (stateName == "filled") "password" else "", guest = stateName == "guest", busy = stateName == "loading", error = error)
        val dialog = when (stateName) {
            "ssl_dialog" -> CreateAccountDialog.Ssl
            "discard" -> CreateAccountDialog.Discard
            else -> CreateAccountDialog.None
        }
        compose.mainClock.autoAdvance = false
        compose.setContent { TeamCityTheme(darkTheme = variant.dark) { CreateAccountScreen(CreateAccountUiState(form), {}, {}, {}, {}, dialog) } }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        if (stateName == "scrolled") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("create-account:scroll").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, 10_000f) }
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
        }
        compose.onNodeWithContentDescription(RuntimeEnvironment.getApplication().getString(teamcityapp.libraries.theme.R.string.action_close)).assertIsDisplayed()
        val tag = when (stateName) {
            "loading" -> "auth:progress"
            "ssl_dialog" -> "auth:warning"
            "discard" -> "create-account:discard"
            else -> null
        }
        if (tag == null) compose.onRoot().captureRoboImage("create_account_${stateName}_${variant.name}.png") else captureScreenRoboImage("create_account_${stateName}_${variant.name}.png")
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
            listOf("user", "guest", "filled", "empty_url", "empty_user", "empty_password", "server_error", "duplicate", "save_error", "loading", "ssl_dialog", "discard", "scrolled").flatMap { state ->
                variants.map { arrayOf<Any>(state, it) }
            } + listOf("user", "scrolled").map { state -> arrayOf<Any>(state, Variant("landscape_$theme", 720, 360, dark, 1f)) }
        }
    }
}
