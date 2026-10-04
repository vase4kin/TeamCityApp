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

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.ComponentName
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.login.view.LoginActivity
import com.github.vase4kin.teamcityapp.splash.dagger.SplashRepositoryModule
import dagger.hilt.android.testing.*
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.splash.api.*

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

    @Volatile private var fail = false

    @BindValue @JvmField
    val repository: SplashRepository = object : SplashRepository {
        override suspend fun hasAccounts(): Boolean {
            reads.incrementAndGet()
            if (fail) throw IllegalStateException("account store")
            try {
                return gate.await()
            } catch (e: kotlinx.coroutines.CancellationException) {
                cancellations.incrementAndGet()
                throw e
            }
        }
    }
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before fun before() {
        app.appInjector.sharedUserStorage().clearAll()
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
    }
    private fun launch() = ActivityScenario.launch<SplashActivity>(Intent(app, SplashActivity::class.java))
    private fun awaitTag(tag: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    }

    @Test fun pendingLoadingSurvivesRecreationWithAnAccessibleLogo() {
        launch().use { scenario ->
            awaitTag("splash:loading")
            compose.onNodeWithContentDescription("TeamCityApp").assertIsDisplayed()
            scenario.recreate()
            awaitTag("splash:loading")
            compose.onNodeWithContentDescription("TeamCityApp").assertIsDisplayed()
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

    @Test fun noAccountsOpensLoginExactlyOnceAndFinishesSplash() {
        assertNavigation(false, LoginActivity::class.java.name)
    }

    @Test fun accountsOpenHomeExactlyOnceAndFinishSplash() {
        assertNavigation(true, HomeActivity::class.java.name)
    }
    private fun assertNavigation(hasAccounts: Boolean, target: String) {
        Intents.init()
        try {
            intending(hasComponent(target)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            launch().use { scenario ->
                awaitTag("splash:loading")
                gate.complete(hasAccounts)
                compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
                assertEquals(1, Intents.getIntents().count { it.component?.className == target })
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun completionInBackgroundWaitsForResumeBeforeRouting() {
        Intents.init()
        try {
            intending(hasComponent(LoginActivity::class.java.name)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            launch().use { scenario ->
                awaitTag("splash:loading")
                var vm: SplashViewModel? = null
                scenario.onActivity { vm = ViewModelProvider(it)[SplashViewModel::class.java] }
                scenario.moveToState(Lifecycle.State.STARTED)
                gate.complete(false)
                compose.waitUntil(10_000) { vm?.state?.value == SplashUiState.Ready(SplashDestination.Login) }
                assertTrue(Intents.getIntents().none { it.component?.className == LoginActivity::class.java.name })
                scenario.moveToState(Lifecycle.State.RESUMED)
                compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
                assertEquals(1, Intents.getIntents().count { it.component?.className == LoginActivity::class.java.name })
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun storageFailureOffersRetryAndCanRecoverToLogin() {
        fail = true
        Intents.init()
        try {
            intending(hasComponent(LoginActivity::class.java.name)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            launch().use { scenario ->
                awaitTag("splash:error")
                scenario.recreate()
                awaitTag("splash:error")
                assertEquals(1, reads.get())
                fail = false
                gate.complete(false)
                compose.onNodeWithText("Try again").performClick()
                compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
                assertEquals(2, reads.get())
                assertEquals(1, Intents.getIntents().count { it.component?.className == LoginActivity::class.java.name })
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun installedLauncherComponentStillResolvesAndLaunchesTheComposeActivity() {
        val component = ComponentName(app.packageName, SplashEntryPoint.LAUNCHER_ACTIVITY)
        val info = app.packageManager.getActivityInfo(component, 0)
        assertTrue(info.exported)
        assertEquals(SplashActivity::class.java.name, info.targetActivity)
        ActivityScenario.launch<SplashActivity>(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(component)).use {
            awaitTag("splash:loading")
            compose.onNodeWithContentDescription("TeamCityApp").assertIsDisplayed()
        }
    }
}
