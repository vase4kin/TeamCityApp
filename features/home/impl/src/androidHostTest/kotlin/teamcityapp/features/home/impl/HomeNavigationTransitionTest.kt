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
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.IntSize
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.*
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
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.theme.*

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class, ExperimentalMaterial3Api::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeNavigationTransitionTest(private val variant: HomeScreenScreenshotTest.Variant) {
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
    private val rootSizes = mutableSetOf<IntSize>()
    private val detailSizes = mutableSetOf<IntSize>()
    private lateinit var navigator: Navigator
    private lateinit var activity: ComponentActivity

    private fun registry() = DestinationRegistry(
        setOf(
            object : FeatureDestination<HomeDestination>(HomeDestination::class, HomeDestination.serializer()) {
                @Composable override fun Content(destination: HomeDestination, navigator: Navigator, entryId: String) {
                    if (destination.tab == HomeTab.Running) {
                        Column(Modifier.fillMaxSize().onSizeChanged { rootSizes += it }) {
                            if (!LocalTeamCityHeaderProvided.current) Text("Standalone Running header", Modifier.testTag("running:standalone-header"))
                            Text("Running build #42", Modifier.padding(TeamCityDimensions.contentPadding))
                            Button({ navigator.push(HomeDestination(HomeTab.Favorites)) }, Modifier.testTag("open-build")) { Text("Open build") }
                        }
                    } else {
                        Scaffold(topBar = { TopAppBar(title = { Text("Build #42") }) }) { padding ->
                            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).onSizeChanged { detailSizes += it }) {
                                Text("Build details", Modifier.padding(TeamCityDimensions.contentPadding).testTag("build:content"))
                            }
                        }
                    }
                }
            }
        )
    )

    @Test fun rootAndBuildHeadersKeepTheirLayoutsThroughPushAndSystemBack() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            activity = checkNotNull(LocalActivity.current) as ComponentActivity
            val registry = remember { registry() }
            navigator = rememberNavigator(HomeDestination(HomeTab.Running), registry)
            TeamCityTheme(darkTheme = variant.dark) {
                HomeNavigationScreen(
                    HomeUiState(selected = HomeTab.Running), variant.width >= 600,
                    mapOf("host/Running" to navigator), registry, "host",
                    {}, {}, {}, {}, remember { SnackbarHostState() }, {}, {}
                )
            }
        }
        settle()
        val navigationBounds = compose.onNodeWithTag("home:tab:Activity").fetchSemanticsNode().boundsInRoot
        val rootSize = rootSizes.single()
        capture("root")
        compose.onNodeWithTag("open-build").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(320)
        assertEquals(2, navigator.snapshot.entries.size)
        assertStable(navigationBounds)
        assertEquals(setOf(rootSize), rootSizes)
        compose.onNodeWithTag("home:title").assertExists()
        compose.onNodeWithTag("build:content").assertExists()
        capture("opening")
        settle()
        compose.onNodeWithTag("home:title").assertDoesNotExist()
        val detailSize = detailSizes.single()
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(320)
        assertEquals(1, navigator.snapshot.entries.size)
        assertStable(navigationBounds)
        assertEquals(setOf(rootSize), rootSizes)
        assertEquals(setOf(detailSize), detailSizes)
        compose.onNodeWithTag("home:title").assertExists()
        compose.onNodeWithTag("build:content").assertExists()
        capture("back")
        settle()
        compose.onNodeWithTag("home:title").assertIsDisplayed()
        compose.onNodeWithTag("build:content").assertDoesNotExist()
        assertStable(navigationBounds)
    }

    @Test fun activityCountFailureDisappearsInBuildDetailsAndReturnsAtTheActivityRoot() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            activity = checkNotNull(LocalActivity.current) as ComponentActivity
            val registry = remember { registry() }
            navigator = rememberNavigator(HomeDestination(HomeTab.Running), registry)
            TeamCityTheme(darkTheme = variant.dark) {
                HomeNavigationScreen(
                    HomeUiState(selected = HomeTab.Running, badges = mapOf(HomeTab.Running to HomeBadge.Failed())), variant.width >= 600,
                    mapOf("host/Running" to navigator), registry, "host",
                    {}, {}, {}, {}, remember { SnackbarHostState() }, {}, {}
                )
            }
        }
        settle()
        compose.onNodeWithText("Couldn’t load the running build count.").assertIsDisplayed()
        compose.onNodeWithTag("open-build").performClick()
        settle()
        compose.onNodeWithTag("build:content").assertIsDisplayed()
        compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        settle()
        settle()
        assertEquals(1, navigator.snapshot.entries.size)
        compose.onNodeWithTag("home:title").assertIsDisplayed()
        compose.onNodeWithText("Couldn’t load the running build count.").assertIsDisplayed()
    }

    private fun assertStable(bounds: androidx.compose.ui.geometry.Rect) {
        compose.onNodeWithTag("running:standalone-header").assertDoesNotExist()
        assertEquals(bounds, compose.onNodeWithTag("home:tab:Activity").fetchSemanticsNode().boundsInRoot)
    }
    private fun settle() {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
    }
    private fun capture(frame: String) {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("home_transition_${frame}_${variant.name}.png")
    }
    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            listOf(
                HomeScreenScreenshotTest.Variant("phone_$theme", 360, 800, dark, 1f),
                HomeScreenScreenshotTest.Variant("tablet_$theme", 1000, 700, dark, 1f),
                HomeScreenScreenshotTest.Variant("large_font_$theme", 360, 800, dark, 1.5f)
            ).map { arrayOf<Any>(it) }
        }
    }
}
