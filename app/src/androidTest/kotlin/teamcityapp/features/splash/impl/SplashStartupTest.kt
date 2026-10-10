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

package teamcityapp.features.splash.impl

import android.content.ComponentName
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.splash.api.SplashEntryPoint

/** Exercises the real storage adapter, UI router, and launcher alias together. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SplashStartupTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val api = HiltApiTestRule(hilt)

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before fun before() {
        app.appInjector.sharedUserStorage().clearAll()
    }

    @After fun closeDestinations() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val monitor = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
            androidx.test.runner.lifecycle.Stage.values().flatMap { monitor.getActivitiesInStage(it) }.distinct().filter { it is com.github.vase4kin.teamcityapp.home.view.HomeActivity || it is teamcityapp.features.login.impl.LoginActivity }.forEach { it.finish() }
        }
        instrumentation.waitForIdleSync()
    }
    private fun launch() = ActivityScenario.launch<SplashActivity>(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(ComponentName(app.packageName, SplashEntryPoint.LAUNCHER_ACTIVITY)))

    @Test fun emptyStoreOpensTheExistingLoginScreen() {
        launch().use { scenario ->
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
            compose.onNodeWithText(app.getString(teamcityapp.features.login.impl.R.string.text_app_description)).assertIsDisplayed()
        }
    }

    @Test fun storedAccountOpensTheExistingProjectsScreen() {
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        launch().use { scenario ->
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
            compose.onNodeWithText(app.getString(teamcityapp.features.navigation.impl.R.string.navigation_projects_title)).assertIsDisplayed()
        }
    }
}
