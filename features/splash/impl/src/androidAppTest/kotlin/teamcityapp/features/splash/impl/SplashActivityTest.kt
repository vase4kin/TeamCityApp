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
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.di.modules.SessionModule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.*
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.splash.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.ResponseCacheStore

/** Installed aliases execute the production shared account restoration and Ktor root. */
@HiltAndroidTest
@UninstallModules(SessionModule::class)
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

    private val gate = CompletableDeferred<Unit>()
    private val reads = AtomicInteger()
    private var current: AccountSecret? = null

    @Volatile private var fail = false

    @BindValue @JvmField
    val storage: AccountStorage = object : AccountStorage, AccountPreferencesStorage {
        override suspend fun active(): AccountSecret? {
            reads.incrementAndGet()
            if (fail) error("account storage")
            gate.await()
            return current
        }
        override suspend fun accounts() = listOfNotNull(current?.identity)
        override suspend fun secret(key: String) = current?.takeIf { it.identity.key == key }
        override suspend fun saveAndActivate(account: AccountSecret) {
            current = account
        }
        override suspend fun activate(key: String) {
            check(current?.identity?.key == key)
        }
        override suspend fun remove(key: String) {
            if (current?.identity?.key == key) current = null
        }
        override suspend fun signOut() {
            current = null
        }
        override suspend fun preferences(key: String) = AccountPreferences(ratingHandled = true)
        override suspend fun updatePreferences(key: String, transform: (AccountPreferences) -> AccountPreferences) = transform(preferences(key))
    }

    @BindValue @JvmField
    val cache: ResponseCacheStore = object : ResponseCacheStore {
        private var value: String? = null
        override suspend fun read() = value
        override suspend fun write(value: String) {
            this.value = value
        }
    }

    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before fun before() {
        TestUtils.disableOnboarding()
        app.appInjector.sharedUserStorage().clearAll()
    }

    private fun launch() = ActivityScenario.launch<MainActivity>(Intent().setClassName(app, IncomingDestinationResolver.SPLASH_COMPONENT))
    private fun awaitTag(tag: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    }

    @Test fun pendingAccountRestoreSurvivesRecreationBeforeShowingLogin() {
        launch().use { scenario ->
            compose.waitUntil(10_000) { reads.get() > 0 }
            scenario.recreate()
            compose.onNodeWithTag("login:submit").assertDoesNotExist()
            gate.complete(Unit)
            awaitTag("login:submit")
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun noAccountsReplacesSplashWithLoginOnceAndBackExitsTheRoot() {
        gate.complete(Unit)
        launch().use { scenario ->
            awaitTag("login:submit")
            scenario.recreate()
            awaitTag("login:submit")
            androidx.test.espresso.Espresso.pressBackUnconditionally()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun restoredAccountEntersTheSharedHomeAndSurvivesRecreation() {
        current = AccountSecret(AccountIdentity(Mocks.URL, "Guest user", true))
        gate.complete(Unit)
        launch().use { scenario ->
            awaitTag("home:tab:Projects")
            scenario.recreate()
            awaitTag("home:tab:Projects")
            compose.onNodeWithTag("login:submit").assertDoesNotExist()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun accountStorageFailureOffersRetryAndRecoversToLogin() {
        fail = true
        launch().use { scenario ->
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty() }
            scenario.recreate()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty() }
            fail = false
            gate.complete(Unit)
            compose.onNodeWithText("Try again").performClick()
            awaitTag("login:submit")
        }
    }

    @Test fun installedStartupAndLauncherAliasesStillTargetTheRoot() {
        for ((name, exported) in listOf(IncomingDestinationResolver.SPLASH_COMPONENT to false, SplashEntryPoint.LAUNCHER_ACTIVITY to true)) {
            val info = app.packageManager.getActivityInfo(ComponentName(app.packageName, name), 0)
            assertEquals(exported, info.exported)
            assertEquals(MainActivity::class.java.name, info.targetActivity)
        }
        gate.complete(Unit)
        val component = ComponentName(app.packageName, SplashEntryPoint.LAUNCHER_ACTIVITY)
        ActivityScenario.launch<MainActivity>(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(component)).use {
            awaitTag("login:submit")
        }
    }
}
