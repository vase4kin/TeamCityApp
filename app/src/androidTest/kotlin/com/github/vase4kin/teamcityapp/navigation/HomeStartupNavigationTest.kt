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

package com.github.vase4kin.teamcityapp.navigation

import android.content.ComponentName
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.build_log.api.BuildLogConsentStorage
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.home.api.HomeTab
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.libraries.navigation.DestinationRegistry

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeStartupNavigationTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockito = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val api = HiltApiTestRule(hilt) { service }

    @JvmField
    @Rule(order = 3)
    val compose = createEmptyComposeRule()

    @Spy private val service: TeamCityService = FakeTeamCityServiceImpl()

    @Inject lateinit var registry: DestinationRegistry

    @Inject lateinit var consent: BuildLogConsentStorage
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val storage get() = app.appInjector.sharedUserStorage()

    @Before fun before() {
        TestUtils.disableOnboarding()
        app.appInjector.cacheManager().evictAllCache()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java))
    private fun awaitTag(tag: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag(tag).assertIsDisplayed()
    }
    private fun awaitRoot() = awaitTag("navigation:row:project:id")
    private fun openDrawer() {
        compose.onNodeWithTag("navigation:up", useUnmergedTree = true).performClick()
        awaitTag("drawer:list")
    }
    private fun select(tab: HomeTab) {
        compose.onNodeWithTag("home:tab:${tab.name}").performClick().assertIsSelected()
    }
    private fun encoded(key: androidx.navigation3.runtime.NavKey) = Intent(app, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(key))

    @Test fun homeCoachmarksFollowRealDrawerAndTabActionsAndPersistOnlyAfterDismissal() {
        val prefs = app.getSharedPreferences(teamcityapp.libraries.onboarding.OnboardingManagerImpl.PREF_NAME, android.content.Context.MODE_PRIVATE)
        val tips = listOf(HomeTab.Favorites to "AddToFavorites", HomeTab.Running to "RBFilter", HomeTab.Queue to "BQFilter", HomeTab.Agents to "AFilter")
        prefs.edit().putBoolean("NavDrawerV1", false).apply { tips.forEach { putBoolean(it.second, false) } }.commit()
        ActivityScenario.launch<MainActivity>(encoded(HomeDestination(HomeTab.Running))).use { scenario ->
            awaitTag("home:coachmark:Drawer")
            assertFalse(prefs.getBoolean("NavDrawerV1", false))
            compose.onNodeWithTag("running_builds:drawer").assertDoesNotExist()
            scenario.recreate()
            awaitTag("home:coachmark:Drawer")
            compose.onNodeWithTag("home:dismiss-prompt").performClick()
            compose.waitUntil(10_000) { prefs.getBoolean("NavDrawerV1", false) }
            // The initial Running tab also has its own first-use action tip.
            awaitTag("home:coachmark:Running")
            compose.onNodeWithTag("home:dismiss-prompt").performClick()
            compose.waitUntil(10_000) { prefs.getBoolean("RBFilter", false) }
            tips.filter { it.first != HomeTab.Running }.forEach { (tab, key) ->
                compose.onNodeWithTag("home:tab:${tab.name}").performClick()
                awaitTag("home:coachmark:${tab.name}")
                assertFalse(prefs.getBoolean(key, false))
                compose.onNodeWithTag("home:fab").assertDoesNotExist()
                scenario.recreate()
                awaitTag("home:coachmark:${tab.name}")
                compose.onNodeWithTag("home:dismiss-prompt").performClick()
                compose.waitUntil(10_000) { prefs.getBoolean(key, false) }
                awaitTag("home:fab")
            }
            scenario.recreate()
            awaitTag("home:fab")
            compose.onAllNodesWithTag("home:coachmark:Agents").assertCountEquals(0)
        }
    }

    @Test fun installedLauncherAliasHostsStartupAndPreservesConsentForTheSavedAccount() {
        runBlocking { consent.setAcknowledged(true) }
        val alias = Intent().setComponent(ComponentName(app.packageName, "teamcityapp.features.splash.view.SplashActivity"))
        ActivityScenario.launch<MainActivity>(alias).use { scenario ->
            awaitRoot()
            HomeTab.entries.forEach { compose.onNodeWithTag("home:tab:${it.name}").assertIsDisplayed() }
            scenario.onActivity {
                assertEquals(MainActivity::class.java, it.javaClass)
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
            assertTrue(runBlocking { consent.acknowledged() })
        }
    }

    @Test fun startupWithoutAnAccountShowsLoginInTheSameHost() {
        storage.clearAll()
        launch().use { scenario ->
            awaitTag("login:form")
            scenario.recreate()
            awaitTag("login:form")
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun tabOwnersRetainStateAndRecreationDoesNotReloadTheVisibleTab() {
        launch().use { scenario ->
            awaitRoot()
            select(HomeTab.Agents)
            awaitTag("agents:list")
            select(HomeTab.Projects)
            awaitRoot()
            verify(service, times(1)).listBuildTypes("_Root")
            select(HomeTab.Agents)
            awaitTag("agents:list")
            verify(service, times(2)).listAgents(false, null, null)
            scenario.recreate()
            awaitTag("agents:list")
            compose.onNodeWithTag("home:tab:Agents").assertIsSelected()
            verify(service, times(2)).listAgents(false, null, null)
        }
    }

    @Test fun recursiveProjectVisitsHaveIndependentStateAndBackReturnsToHome() {
        launch().use { scenario ->
            awaitRoot()
            compose.onNodeWithTag("navigation:row:project:id").performClick()
            awaitTag("navigation:row:project:id")
            compose.onNodeWithTag("navigation:row:project:id").performClick()
            awaitTag("navigation:row:project:id")
            scenario.recreate()
            awaitTag("navigation:row:project:id")
            pressBack()
            awaitTag("navigation:row:project:id")
            pressBack()
            awaitRoot()
            compose.onNodeWithTag("home:tab:Projects").assertIsSelected()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            verify(service, times(2)).listBuildTypes("id")
        }
    }

    @Test fun drawerSettingsReplacesTheSheetAndBackRestoresTheHomeTab() {
        launch().use { scenario ->
            awaitRoot()
            openDrawer()
            compose.onNodeWithTag("drawer:settings").performScrollTo().performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Light theme").fetchSemanticsNodes().isNotEmpty() }
            pressBack()
            awaitRoot()
            compose.onNodeWithTag("drawer:list").assertDoesNotExist()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun quickFilterUpdatesTheRetainedAgentsQueryAndRestoresTheSelectedTab() {
        launch().use { scenario ->
            awaitRoot()
            select(HomeTab.Agents)
            awaitTag("agents:list")
            compose.onNodeWithTag("home:fab").performClick()
            awaitTag("quick-filter:content")
            scenario.recreate()
            awaitTag("quick-filter:content")
            compose.onNodeWithTag("quick-filter:apply").performClick()
            awaitTag("agents:list")
            compose.waitUntil(10_000) { runCatching { verify(service).listAgents(true, null, null) }.isSuccess }
            compose.onNodeWithTag("home:tab:Agents").assertIsSelected()
            compose.onNodeWithTag("quick-filter:content").assertDoesNotExist()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun switchingAccountsOnTheSameServerReplacesTheHomeOwnersAndClearsConsent() {
        var persisted = false
        storage.saveUserAccountAndSetItAsActive(
            Mocks.URL,
            "alice",
            "secret",
            false,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {
                    persisted = true
                }
                override fun onFail() {
                    fail("Account persistence failed")
                }
            }
        )
        assertTrue(persisted)
        ActivityScenario.launch<MainActivity>(encoded(HomeDestination(HomeTab.Agents))).use { scenario ->
            awaitTag("agents:list")
            runBlocking { consent.setAcknowledged(true) }
            compose.onNodeWithContentDescription(app.getString(teamcityapp.features.agents.impl.R.string.agents_open_drawer)).performClick()
            awaitTag("drawer:list")
            compose.onNodeWithTag("drawer:account:${Mocks.URL.length}:${Mocks.URL}:Guest user").performClick()
            awaitRoot()
            assertEquals("Guest user", storage.activeUser.userName)
            assertFalse(runBlocking { consent.acknowledged() })
            compose.onNodeWithTag("home:tab:Projects").assertIsSelected()
            select(HomeTab.Agents)
            awaitTag("agents:list")
            verify(service, times(2)).listAgents(false, null, null)
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }
}
