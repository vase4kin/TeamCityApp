/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.splash.impl

import android.content.ComponentName
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.di.features.splash.SplashRepositoryModule
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.AppAccountFlowNavigation
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.splash.api.*
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination

/** Exercises startup's destination owner and installed components on the single activity. */
@HiltAndroidTest
@UninstallModules(SplashRepositoryModule::class)
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val api = HiltApiTestRule(hilt)

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()
    private val gate = CompletableDeferred<Boolean>()
    private val reads = AtomicInteger()
    private val cancellations = AtomicInteger()
    private val completedReads = AtomicInteger()

    @Inject lateinit var accounts: AppAccountFlowNavigation

    @Volatile private var fail = false

    @BindValue @JvmField
    val repository: SplashRepository = object : SplashRepository {
        override suspend fun hasAccounts(): Boolean {
            reads.incrementAndGet()
            if (fail) throw IllegalStateException("account store")
            try {
                return gate.await().also { completedReads.incrementAndGet() }
            } catch (e: kotlinx.coroutines.CancellationException) {
                cancellations.incrementAndGet()
                throw e
            }
        }
    }
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before fun before() {
        TestUtils.disableOnboarding()
        app.appInjector.sharedUserStorage().clearAll()
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
    }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent().setClassName(app, IncomingDestinationResolver.SPLASH_COMPONENT))
    private fun awaitTag(tag: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithTag(tag).assertIsDisplayed()
    }
    private fun awaitLogin() {
        val description = app.getString(teamcityapp.features.login.impl.R.string.text_app_description)
        compose.waitUntil(10_000) { compose.onAllNodesWithText(description).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(description).assertIsDisplayed()
    }

    @Test fun pendingLoadingSurvivesRecreationWithoutDuplicateBranding() {
        launch().use { scenario ->
            awaitTag("splash:loading")
            compose.onNodeWithContentDescription("TeamCityApp").assertDoesNotExist()
            scenario.recreate()
            awaitTag("splash:loading")
            compose.onNodeWithContentDescription("TeamCityApp").assertDoesNotExist()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test fun stoppingCancelsPendingReadImmediatelyAndResumeRestartsIt() {
        launch().use { scenario ->
            awaitTag("splash:loading")
            compose.waitUntil(10_000) { reads.get() > 0 }
            scenario.moveToState(Lifecycle.State.CREATED)
            compose.waitUntil(10_000) { cancellations.get() > 0 }
            val before = reads.get()
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitUntil(10_000) { reads.get() > before }
            awaitTag("splash:loading")
            compose.onNodeWithTag("splash:error").assertDoesNotExist()
        }
    }

    @Test fun noAccountsReplacesSplashWithLoginOnceAndBackExitsTheRoot() {
        assertNavigation(false)
    }

    @Test fun accountsReplaceSplashWithHomeOnceAndBackExitsTheRoot() {
        assertNavigation(true)
    }
    private fun assertNavigation(hasAccounts: Boolean) {
        if (hasAccounts) app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        launch().use { scenario ->
            awaitTag("splash:loading")
            gate.complete(hasAccounts)
            if (hasAccounts) awaitTag("navigation:row:project:id") else awaitLogin()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
            scenario.recreate()
            if (hasAccounts) awaitTag("navigation:row:project:id") else awaitLogin()
            assertEquals(1, reads.get())
            androidx.test.espresso.Espresso.pressBackUnconditionally()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun completionInBackgroundWaitsForResumeBeforeRouting() {
        launch().use { scenario ->
            awaitTag("splash:loading")
            scenario.moveToState(Lifecycle.State.STARTED)
            gate.complete(false)
            // A paused Android frame clock need not redraw the ready screen. Observe the
            // completed read and account coordinator without requiring a paused UI frame.
            compose.waitUntil(10_000) { completedReads.get() == 1 }
            assertEquals(SplashNavigationDestination, accounts.session.value.initial)
            assertEquals(Lifecycle.State.STARTED, scenario.state)
            compose.onNodeWithText(app.getString(teamcityapp.features.login.impl.R.string.text_app_description)).assertDoesNotExist()
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitLogin()
            scenario.recreate()
            awaitLogin()
            assertEquals(1, reads.get())
        }
    }

    @Test fun storageFailureOffersRetryAndCanRecoverToLogin() {
        fail = true
        launch().use { scenario ->
            awaitTag("splash:error")
            scenario.recreate()
            awaitTag("splash:error")
            assertEquals(1, reads.get())
            fail = false
            gate.complete(false)
            compose.onNodeWithText("Try again").performClick()
            awaitLogin()
            assertEquals(2, reads.get())
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test fun installedPrivateStartupComponentTargetsTheRootAndCreatesNoFragments() {
        val component = ComponentName(app.packageName, IncomingDestinationResolver.SPLASH_COMPONENT)
        val info = app.packageManager.getActivityInfo(component, 0)
        assertFalse(info.exported)
        assertEquals(MainActivity::class.java.name, info.targetActivity)
        launch().use { scenario ->
            awaitTag("splash:loading")
            scenario.onActivity {
                assertEquals(MainActivity::class.java, it.javaClass)
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
        }
    }

    @Test fun installedLauncherComponentStillResolvesAndLaunchesTheRootActivity() {
        val component = ComponentName(app.packageName, SplashEntryPoint.LAUNCHER_ACTIVITY)
        val info = app.packageManager.getActivityInfo(component, 0)
        assertTrue(info.exported)
        assertEquals(MainActivity::class.java.name, info.targetActivity)
        ActivityScenario.launch<MainActivity>(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(component)).use {
            awaitTag("splash:loading")
            compose.onNodeWithContentDescription("TeamCityApp").assertDoesNotExist()
        }
    }
}
