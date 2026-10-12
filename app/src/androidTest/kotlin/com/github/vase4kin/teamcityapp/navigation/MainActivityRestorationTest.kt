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

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.BuildComposeFixtures
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.RootDestinationLauncher
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.build_log.api.BuildLogConsentStorage
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.navigation.DestinationRegistry

/** Root restoration must preserve valid visits without replaying another account's input. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityRestorationTest {
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

    @Inject lateinit var consent: BuildLogConsentStorage
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val app get() = context.applicationContext as TeamCityApplicationBase
    private val storage get() = app.appInjector.sharedUserStorage()
    private val destination = ArtifactsDestination(BuildComposeFixtures.incoming, "/old-account/folder", "Old account folder")

    @Before fun before() {
        TestUtils.disableOnboarding()
        app.appInjector.cacheManager().evictAllCache()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        doReturn(Single.just(ServerInfo("restoration-server", "https://restoration.example"))).`when`(service).serverInfo()
        doReturn(Single.just(Files(emptyList()))).`when`(service).listArtifacts(org.mockito.kotlin.any(), org.mockito.kotlin.any())
        app.getSharedPreferences("rateTheAppPref", android.content.Context.MODE_PRIVATE).edit().putBoolean("rated", true).commit()
    }

    private fun alias() = Intent().setClassName(context.packageName, IncomingDestinationResolver.ARTIFACTS_COMPONENT)
        .putExtra(LegacyArtifactsEntry.BUILD, BuildComposeFixtures.legacy(destination.build))
        .putExtra(LegacyArtifactsEntry.URL, destination.href).putExtra(LegacyArtifactsEntry.NAME, destination.name)

    @Test fun sameAccountRecreationRetainsTheAliasVisitAndCompletedContent() {
        ActivityScenario.launch<MainActivity>(alias()).use { scenario ->
            awaitText(destination.name)
            var visit: String? = null
            scenario.onActivity { visit = it.navigationSnapshot!!.entries.single().id }
            scenario.recreate()
            awaitText(destination.name)
            scenario.onActivity {
                assertEquals(visit, it.navigationSnapshot!!.entries.single().id)
                assertEquals(destination, it.navigationSnapshot!!.entries.single().destination)
            }
            verify(service, times(1)).listArtifacts(destination.href, "browseArchives:true")
        }
    }

    @Test fun aDifferentUserOnTheSameServerCannotRestoreAnOldArtifactRequestOrReplayItOnLaterRecreation() {
        ActivityScenario.launch<MainActivity>(alias()).use { scenario ->
            awaitText(destination.name)
            runBlocking { consent.setAcknowledged(true) }
            val callback = object : Application.ActivityLifecycleCallbacks {
                override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {
                    if (activity is MainActivity) {
                        saveDifferentUser()
                        // A retired/invalid payload must be rejected before any decoding.
                        activity.intent.putExtra(MainActivity.EXTRA_DESTINATION, "unreadable-previous-account-destination")
                    }
                }
                override fun onActivityCreated(activity: Activity, state: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivityDestroyed(activity: Activity) {}
            }
            app.registerActivityLifecycleCallbacks(callback)
            try {
                scenario.recreate()
            } finally {
                app.unregisterActivityLifecycleCallbacks(callback)
            }
            awaitHome()
            assertFalse(runBlocking { consent.acknowledged() })
            compose.onNodeWithText(destination.name).assertDoesNotExist()
            scenario.onActivity {
                assertEquals("alice", storage.activeUser.userName)
                assertFalse(it.intent.hasExtra(LegacyArtifactsEntry.BUILD))
                assertFalse(it.intent.hasExtra(LegacyArtifactsEntry.URL))
                assertFalse(it.intent.hasExtra(LegacyArtifactsEntry.NAME))
                assertTrue(it.navigationSnapshot!!.entries.single().destination is HomeDestination)
                assertTrue(it.navigationSnapshot!!.results.isEmpty())
            }
            scenario.recreate()
            awaitHome()
            compose.onNodeWithText(destination.name).assertDoesNotExist()
            verify(service, times(1)).listArtifacts(destination.href, "browseArchives:true")
        }
    }

    @Test fun oldFragmentManagerStateIsDiscardedWhileTheComposeStackAndVisitsSurvive() {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(AboutDestination))).use { scenario ->
            awaitText("restoration-server")
            scenario.onActivity { launcher.open(it, SettingsDestination) }
            awaitTag("settings:theme")
            var visits: List<String>? = null
            scenario.onActivity {
                visits = it.navigationSnapshot!!.entries.map { entry -> entry.id }
                // A viewless framework fragment supplies real retired-host saved state.
                it.supportFragmentManager.beginTransaction().add(Fragment(), "retired-host").commitNow()
                assertEquals(1, it.supportFragmentManager.fragments.size)
            }
            scenario.recreate()
            awaitTag("settings:theme")
            scenario.onActivity {
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
                assertEquals(visits, it.navigationSnapshot!!.entries.map { entry -> entry.id })
            }
            compose.onNodeWithContentDescription("Back").performClick()
            awaitText("restoration-server")
        }
    }

    @Test fun removingTheAccountWhileSavingReturnsToLoginAndDiscardsOldBuildArguments() {
        ActivityScenario.launch<MainActivity>(alias()).use { scenario ->
            awaitText(destination.name)
            val callback = object : Application.ActivityLifecycleCallbacks {
                override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {
                    if (activity is MainActivity) storage.clearAll()
                }
                override fun onActivityCreated(activity: Activity, state: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivityDestroyed(activity: Activity) {}
            }
            app.registerActivityLifecycleCallbacks(callback)
            try {
                scenario.recreate()
            } finally {
                app.unregisterActivityLifecycleCallbacks(callback)
            }
            awaitTag("login:form")
            compose.onNodeWithText(destination.name).assertDoesNotExist()
            scenario.onActivity {
                assertEquals(LoginDestination, it.navigationSnapshot!!.entries.single().destination)
                assertTrue(it.navigationSnapshot!!.results.isEmpty())
                assertFalse(it.intent.hasExtra(LegacyArtifactsEntry.BUILD))
            }
            verify(service, times(1)).listArtifacts(destination.href, "browseArchives:true")
        }
    }

    @Test fun launcherRelaunchPreservesAnInstalledAliasArgumentsAndVisitAcrossRecreation() {
        ActivityScenario.launch<MainActivity>(alias()).use { scenario ->
            awaitText(destination.name)
            var visit: String? = null
            scenario.onActivity {
                visit = it.navigationSnapshot!!.entries.single().id
                deliverIncoming(it, requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)))
                assertEquals(destination, it.incoming.destination(it.intent))
                assertEquals(visit, it.navigationSnapshot!!.entries.single().id)
            }
            scenario.recreate()
            awaitText(destination.name)
            scenario.onActivity {
                assertEquals(destination, it.navigationSnapshot!!.entries.single().destination)
                assertEquals(visit, it.navigationSnapshot!!.entries.single().id)
            }
            verify(service, times(1)).listArtifacts(destination.href, "browseArchives:true")
        }
    }

    @Test fun anIncomingRequestSavedBeforeTheNextFrameIsRestoredExactlyOnce() {
        val initial = Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(HomeDestination()))
        ActivityScenario.launch<MainActivity>(initial).use { scenario ->
            awaitHome()
            var homeVisit: String? = null
            scenario.onActivity { homeVisit = it.navigationSnapshot!!.entries.single().id }
            recreateDeliveringBeforeSave(scenario) {
                deliverIncoming(it, Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(AboutDestination)))
                assertEquals(homeVisit, it.navigationSnapshot!!.entries.single().id)
            }
            awaitText("restoration-server")
            var aboutVisit: String? = null
            scenario.onActivity {
                assertEquals(2, it.navigationSnapshot!!.entries.size)
                assertEquals(homeVisit, it.navigationSnapshot!!.entries.first().id)
                assertEquals(AboutDestination, it.navigationSnapshot!!.entries.last().destination)
                aboutVisit = it.navigationSnapshot!!.entries.last().id
            }
            scenario.recreate()
            awaitText("restoration-server")
            scenario.onActivity {
                assertEquals(2, it.navigationSnapshot!!.entries.size)
                assertEquals(aboutVisit, it.navigationSnapshot!!.entries.last().id)
            }
        }
    }

    @Test fun anAccountSwitchSavedBeforeTheNextFrameKeepsItsRequestedHomeTab() {
        val initial = Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(HomeDestination()))
        ActivityScenario.launch<MainActivity>(initial).use { scenario ->
            awaitHome()
            var oldVisit: String? = null
            scenario.onActivity { oldVisit = it.navigationSnapshot!!.entries.single().id }
            runBlocking { consent.setAcknowledged(true) }
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            saveDifferentUser()
            scenario.onActivity {
                deliverIncoming(
                    it,
                    Intent().setClassName(context.packageName, IncomingDestinationResolver.HOME_COMPONENT)
                        .putExtra(IncomingDestinationResolver.HOME_TAB, teamcityapp.features.home.api.HomeTab.Queue.ordinal)
                )
                assertEquals(oldVisit, it.navigationSnapshot!!.entries.single().id)
            }
            scenario.recreate()
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            awaitTag("home:tab:Queue")
            compose.waitUntil(10_000) {
                val entries = com.github.vase4kin.teamcityapp.helper.currentRootSnapshot().entries
                entries.size == 1 && (entries.single().destination as? HomeDestination)?.tab == teamcityapp.features.home.api.HomeTab.Queue
            }
            scenario.onActivity {
                assertNotEquals(oldVisit, it.navigationSnapshot!!.entries.single().id)
                assertEquals("alice", storage.activeUser.userName)
                assertTrue(it.navigationSnapshot!!.results.isEmpty())
            }
            assertFalse(runBlocking { consent.acknowledged() })
            scenario.recreate()
            awaitTag("home:tab:Queue")
            scenario.onActivity { assertEquals(teamcityapp.features.home.api.HomeTab.Queue, (it.navigationSnapshot!!.entries.single().destination as HomeDestination).tab) }
        }
    }

    private fun recreateDeliveringBeforeSave(scenario: ActivityScenario<MainActivity>, platformLaunch: Intent? = null, deliver: (MainActivity) -> Unit) {
        var saves = 0
        val callback = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPreSaveInstanceState(activity: Activity, state: Bundle) {
                if (activity is MainActivity && saves++ == 0) deliver(activity)
            }
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {
                // Simulate process restoration supplying the task's older launch Intent.
                if (activity is MainActivity && platformLaunch != null) activity.intent = Intent(platformLaunch)
            }
            override fun onActivityCreated(activity: Activity, state: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
        app.registerActivityLifecycleCallbacks(callback)
        try {
            scenario.recreate()
        } finally {
            app.unregisterActivityLifecycleCallbacks(callback)
        }
        assertEquals(1, saves)
    }

    private fun deliverIncoming(activity: MainActivity, request: Intent) {
        MainActivity::class.java.getDeclaredMethod("onNewIntent", Intent::class.java).apply { isAccessible = true }.invoke(activity, request)
    }

    private fun saveDifferentUser() {
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
    }
    private fun awaitText(value: String) = compose.waitUntil(10_000) { compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty() }
    private fun awaitTag(tag: String) = compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    private fun awaitHome() = compose.waitUntil(15_000) { compose.onAllNodesWithTag("home:tab:Projects").fetchSemanticsNodes().isNotEmpty() }
}
