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

package teamcityapp.features.home.impl

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.remember
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
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.theme.*

/** Goldens cover Home's persistent chrome; child features own their screen-state goldens. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class, ExperimentalMaterial3Api::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NestedHomeScreenScreenshotTest(private val screen: String, private val variant: HomeScreenScreenshotTest.Variant) {
    private val compose = createComposeRule()
    private val device = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val qualifiers = RuntimeEnvironment.getQualifiers()
                val scale = RuntimeEnvironment.getFontScale()
                RuntimeEnvironment.setQualifiers("en-rUS-w${variant.width}dp-h${variant.height}dp-${if (variant.dark) "night" else "notnight"}-mdpi")
                RuntimeEnvironment.setFontScale(variant.fontScale)
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

    @Test fun rendersChildWithPersistentNavigation() {
        val tab = when (screen) {
            "project" -> HomeTab.Projects
            "history" -> HomeTab.Favorites
            else -> HomeTab.Running
        }
        val title = when (screen) {
            "project" -> "Android project"
            "history" -> "Build history"
            else -> "Build #42"
        }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                HomeScreen(HomeUiState(selected = tab), variant.width >= 600, {}, {}, {}, {}, remember { SnackbarHostState() }, showHomeHeader = false) {
                    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { TextButton({}) { Text("Back") } }) }) { padding ->
                        Column(Modifier.fillMaxSize().padding(padding).padding(TeamCityDimensions.contentPadding)) {
                            Text("Mock debug", style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(TeamCityDimensions.contentPadding))
                            Card(Modifier.fillMaxWidth()) { Text("Completed successfully", Modifier.padding(TeamCityDimensions.contentPadding)) }
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithTag("home:title").assertDoesNotExist()
        listOf("Projects", "Favorites", "Activity").forEach { compose.onNodeWithTag("home:tab:$it").assertIsDisplayed() }
        compose.onRoot().captureRoboImage("home_nested_${screen}_${variant.name}.png")
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(
                HomeScreenScreenshotTest.Variant("phone_$theme", 360, 800, dark, 1f),
                HomeScreenScreenshotTest.Variant("tablet_$theme", 1000, 700, dark, 1f),
                HomeScreenScreenshotTest.Variant("large_font_$theme", 360, 800, dark, 1.5f)
            )
            listOf("project", "history", "details").flatMap { screen -> variants.map { arrayOf<Any>(screen, it) } }
        }
    }
}
