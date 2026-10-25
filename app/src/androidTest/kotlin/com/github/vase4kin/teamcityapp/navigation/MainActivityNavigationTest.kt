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

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.RootDestinationLauncher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.navigation.DestinationRegistry

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {
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

    @Inject lateinit var launcher: RootDestinationLauncher
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val info = ServerInfo("navigation-server", "https://navigation.example")

    @Before fun before() {
        com.github.vase4kin.teamcityapp.helper.TestUtils.disableOnboarding()
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
        app.appInjector.cacheManager().evictAllCache()
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        runBlocking { app.appInjector.themePreferences().setTheme(ThemeMode.Light) }
        `when`(service.serverInfo()).thenReturn(Single.just(info))
    }

    private fun await(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    private fun launchAbout() = ActivityScenario.launch<MainActivity>(
        Intent(app, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(AboutDestination))
    )

    @Test fun incomingDestinationUsesSameActivityAndBackRestoresRetainedAbout() {
        launchAbout().use { scenario ->
            await(info.version)
            scenario.onActivity { launcher.open(it, SettingsDestination) }
            await("Light theme")
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
            compose.onNodeWithContentDescription("Back").performClick()
            await(info.version)
            verify(service, times(1)).serverInfo()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test fun recreationRestoresTheFullStackEvenWhenIntentPointsToTheLatestDestination() {
        launchAbout().use { scenario ->
            await(info.version)
            scenario.onActivity { launcher.open(it, SettingsDestination) }
            await("Light theme")
            scenario.recreate()
            await("Light theme")
            compose.onNodeWithContentDescription("Back").performClick()
            await(info.version)
            verify(service, times(1)).serverInfo()
        }
    }

    @Test fun systemBackPopsTheCurrentDestinationWithoutClosingTheHost() {
        launchAbout().use { scenario ->
            await(info.version)
            scenario.onActivity { launcher.open(it, SettingsDestination) }
            await("Light theme")
            androidx.test.espresso.Espresso.pressBack()
            await(info.version)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test fun installedAliasNewIntentUsesTheExistingHostAndSavedStack() {
        launchAbout().use { scenario ->
            await(info.version)
            scenario.onActivity { activity ->
                activity.startActivity(Intent().setClassName(activity, IncomingDestinationResolver.SETTINGS_COMPONENT).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            await("Light theme")
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.onActivity {
                assertEquals(MainActivity::class.java.name, it.intent.component?.className)
                assertEquals(SettingsDestination, registry.decodeDestination(checkNotNull(it.intent.getStringExtra(MainActivity.EXTRA_DESTINATION))))
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
            scenario.recreate()
            await("Light theme")
            compose.onNodeWithContentDescription("Back").performClick()
            await(info.version)
            verify(service, times(1)).serverInfo()
        }
    }

    @Test fun installedFeatureAliasesArePrivateAndTargetTheRootActivity() {
        listOf(IncomingDestinationResolver.ABOUT_COMPONENT, IncomingDestinationResolver.SETTINGS_COMPONENT, IncomingDestinationResolver.SPLASH_COMPONENT, IncomingDestinationResolver.TEST_DETAILS_COMPONENT, IncomingDestinationResolver.CHANGE_DETAILS_COMPONENT, IncomingDestinationResolver.MANAGE_ACCOUNTS_COMPONENT, IncomingDestinationResolver.LOGIN_COMPONENT, IncomingDestinationResolver.LEGACY_LOGIN_COMPONENT, IncomingDestinationResolver.CREATE_ACCOUNT_COMPONENT, IncomingDestinationResolver.LEGACY_CREATE_ACCOUNT_COMPONENT).forEach { name ->
            val component = android.content.ComponentName(app.packageName, name)
            val activity = app.packageManager.getActivityInfo(component, 0)
            assertEquals(MainActivity::class.java.name, activity.targetActivity)
            assertFalse(activity.exported)
        }
    }

    @Test fun toolbarBackReturnsToSharedHomeBeforeRootExit() {
        launchAbout().use { scenario ->
            await(info.version)
            compose.onNodeWithContentDescription("Back").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("home:tab:Projects").fetchSemanticsNodes().isNotEmpty() }
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            androidx.test.espresso.Espresso.pressBackUnconditionally()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    private fun captureDisplay(scenario: ActivityScenario<MainActivity>, name: String) {
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        val frame = java.util.concurrent.CountDownLatch(1)
        scenario.onActivity { activity -> activity.window.decorView.postOnAnimation { activity.window.decorView.postOnAnimation { frame.countDown() } } }
        check(frame.await(5, java.util.concurrent.TimeUnit.SECONDS)) { "Display did not render the settled route" }
        val bitmap = androidx.test.runner.screenshot.Screenshot.capture().bitmap
        val file = java.io.File(app.getExternalFilesDir(null), "shared-production-$name.png")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun packagedAndroidNoticesUseTheSharedReaderAndAllThemeChoices() {
        launchAbout().use { scenario ->
            await(info.version)
            compose.onNodeWithText("Open sources libraries").performScrollTo().performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("licenses:android_0").fetchSemanticsNodes().isNotEmpty() }
            captureDisplay(scenario, "licenses")
            compose.onNodeWithTag("licenses:android_0").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("license:text").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("license:text").assertIsDisplayed()
            captureDisplay(scenario, "license-text")
            scenario.onActivity { launcher.open(it, SettingsDestination) }
            await("Light theme")
            listOf("Light", "Dark", "AutoBattery", "System").forEach { compose.onNodeWithTag("settings:option:$it").assertExists() }
            captureDisplay(scenario, "settings")
        }
    }

    @Test fun sharedAccountManageAddAndSignOutUseOneRoot() {
        ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java)).use { scenario ->
            fun click(tag: String) {
                compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
                if (tag == "drawer:manage" || tag == "accounts:sign-out") compose.onNodeWithTag(tag).performScrollTo().performClick() else compose.onNodeWithTag(tag).performClick()
            }
            click("home:account")
            compose.onNodeWithTag("drawer:manage").assertExists()
            captureDisplay(scenario, "account")
            click("drawer:manage")
            compose.onNodeWithTag("accounts:list").assertExists()
            captureDisplay(scenario, "manage")
            click("accounts:add")
            compose.onNodeWithTag("create-account:form").assertExists()
            captureDisplay(scenario, "add")
            androidx.test.espresso.Espresso.pressBack()
            compose.onNodeWithText("Discard").performClick()
            androidx.test.espresso.Espresso.pressBack()
            click("accounts:sign-out")
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("login:submit").fetchSemanticsNodes().isNotEmpty() }
            captureDisplay(scenario, "signed-out")
            scenario.recreate()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("login:submit").fetchSemanticsNodes().isNotEmpty() }
        }
    }
}
