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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.*
import teamcityapp.libraries.theme.*
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class, ExperimentalMaterial3Api::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenScreenshotTest(private val stateName: String, private val variant: Variant) {
    data class Variant(val name: String, val width: Int, val height: Int, val dark: Boolean, val fontScale: Float) {
        override fun toString() = name
    }

    private val compose = createComposeRule()
    private val device = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val qualifiers = RuntimeEnvironment.getQualifiers()
                val fontScale = RuntimeEnvironment.getFontScale()
                RuntimeEnvironment.setQualifiers("en-rUS-w${variant.width}dp-h${variant.height}dp-${if (variant.dark) "night" else "notnight"}-mdpi")
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
        val badges = HomeTab.entries.filter { it != HomeTab.Projects }.associateWith<HomeTab, HomeBadge> { HomeBadge.Count(if (it == HomeTab.Favorites) 8 else 123) }
        val query = HomeQuery("account", listOf("mock"), teamcityapp.features.filter_bottom_sheet.api.QuickFilter.RunningFavorites, teamcityapp.features.filter_bottom_sheet.api.QuickFilter.QueuedAll, teamcityapp.features.filter_bottom_sheet.api.QuickFilter.AgentsConnected, HomeAccount("Andrey Tolpeev", "https://teamcity.example"))
        val state = (
            when {
                stateName == "tooltip" -> HomeUiState()
                stateName == "loading" -> HomeUiState()
                stateName == "running_all" -> HomeUiState(selected = HomeTab.Running)
                stateName == "queue_favorites" -> HomeUiState(selected = HomeTab.Queue)
                stateName == "agents_disconnected" -> HomeUiState(selected = HomeTab.Agents)
                stateName == "filter_loading" -> HomeUiState(selected = HomeTab.Running)
                stateName == "filter_applying" -> HomeUiState(selected = HomeTab.Running, applyingFilter = QuickFilter.RunningAll)
                stateName == "filter_failed" -> HomeUiState(selected = HomeTab.Running, filterFailed = true)
                stateName == "counts_failed" -> HomeUiState(selected = HomeTab.Running, badges = badges.mapValues { HomeBadge.Failed() })
                stateName == "partial_failure" -> HomeUiState(selected = HomeTab.Running, badges = badges + (HomeTab.Queue to HomeBadge.Failed()))
                stateName == "running_count_failed" -> HomeUiState(selected = HomeTab.Running, badges = badges + (HomeTab.Running to HomeBadge.Failed()))
                stateName == "build_counts_failed" -> HomeUiState(selected = HomeTab.Running, badges = badges + setOf(HomeTab.Running, HomeTab.Queue).associateWith { HomeBadge.Failed() })
                stateName == "projects_count_failure" -> HomeUiState(badges = badges + (HomeTab.Running to HomeBadge.Failed()))
                stateName == "agents_zero" -> HomeUiState(selected = HomeTab.Agents, badges = badges + (HomeTab.Agents to HomeBadge.Count(0)))
                stateName == "agents_loading" -> HomeUiState(selected = HomeTab.Agents, badges = badges + (HomeTab.Agents to HomeBadge.Loading))
                stateName == "agents_failed" -> HomeUiState(selected = HomeTab.Agents, badges = badges + (HomeTab.Agents to HomeBadge.Failed()))
                stateName == "agents_only" -> HomeUiState(selected = HomeTab.Agents, badges = badges + (HomeTab.Running to HomeBadge.Count(0)) + (HomeTab.Queue to HomeBadge.Count(0)))
                stateName == "onboarding_failed" -> HomeUiState(badges = badges, onboardingFailed = true)
                stateName == "favorites_count_failure" -> HomeUiState(selected = HomeTab.Favorites, badges = badges + (HomeTab.Running to HomeBadge.Failed()))
                stateName == "favorites_toolbar" -> HomeUiState(selected = HomeTab.Favorites, badges = badges)
                stateName.startsWith("prompt_") -> HomePrompt.valueOf(stateName.removePrefix("prompt_")).let { HomeUiState(selected = if (it == HomePrompt.Drawer) HomeTab.Projects else HomeTab.valueOf(it.name), badges = badges, prompt = it.ui()) }
                else -> HomeUiState(selected = HomeTab.valueOf(stateName.removePrefix("tab_")), badges = badges)
            }
            ).copy(
            query = when (stateName) {
                "running_all" -> query.copy(runningFilter = QuickFilter.RunningAll)
                "queue_favorites" -> query.copy(queueFilter = QuickFilter.QueuedFavorites)
                "agents_disconnected" -> query.copy(agentsFilter = QuickFilter.AgentsDisconnected)
                "filter_loading" -> null
                else -> query
            }
        )
        compose.mainClock.autoAdvance = false
        compose.setContent {
            TeamCityTheme(darkTheme = variant.dark) {
                HomeScreen(state, variant.width >= 600, {}, {}, {}, {}, remember { SnackbarHostState() }) {
                    Surface(Modifier.fillMaxSize()) {
                        Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
                            if (state.prompt?.prompt == HomePrompt.Favorites) {
                                FilledTonalButton({}, Modifier.coachmarkAnchor("home:action")) { Text("Add to favorites") }
                            }
                            Text("TeamCity App", style = MaterialTheme.typography.titleMedium)
                            Surface(Modifier.fillMaxWidth().padding(top = TeamCityDimensions.smallSpacing), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.large) {
                                Text(if (state.isActivity) "Mock debug · main" else "Mock debug", Modifier.padding(TeamCityDimensions.contentPadding), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        compose.onNodeWithTag("home:count:Activity", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("home:count-failed:Activity", useUnmergedTree = true).assertDoesNotExist()
        if (stateName == "tooltip") {
            compose.mainClock.autoAdvance = true
            compose.onNodeWithTag("home:tab:Favorites").performTouchInput { longClick() }
            compose.waitForIdle()
            compose.onNode(isPopup()).assertExists()
            compose.mainClock.autoAdvance = false
            captureScreenRoboImage("home_${stateName}_${variant.name}.png")
            return
        }
        if (stateName in listOf("counts_failed", "partial_failure", "running_count_failed", "build_counts_failed", "agents_failed")) {
            compose.onNode(hasText("Retry") and hasAnyAncestor(hasTestTag("retry:snackbar"))).assertIsDisplayed()
        }
        if (stateName in listOf("onboarding_failed", "favorites_count_failure", "projects_count_failure")) compose.onNodeWithTag("retry:snackbar").assertDoesNotExist()
        compose.onRoot().captureRoboImage("home_${stateName}_${variant.name}.png")
    }
    companion object {
        @JvmStatic
        @Parameters(name = "{0}_{1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            val theme = if (dark) "dark" else "light"
            val variants = listOf(Variant("phone_$theme", 360, 800, dark, 1f), Variant("tablet_$theme", 1000, 700, dark, 1f), Variant("large_font_$theme", 360, 800, dark, 1.5f))
            (listOf("tooltip", "loading", "counts_failed", "partial_failure", "running_count_failed", "build_counts_failed", "projects_count_failure", "agents_zero", "agents_loading", "agents_failed", "agents_only", "onboarding_failed", "favorites_toolbar", "favorites_count_failure", "running_all", "queue_favorites", "agents_disconnected", "filter_loading", "filter_applying", "filter_failed") + HomeTab.entries.map { "tab_${it.name}" } + HomePrompt.entries.map { "prompt_${it.name}" }).flatMap { state -> variants.map { arrayOf<Any>(state, it) } }
        }
    }
}
