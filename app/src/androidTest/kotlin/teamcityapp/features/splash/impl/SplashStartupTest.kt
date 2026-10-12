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
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.*
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
        TestUtils.disableOnboarding()
        app.appInjector.sharedUserStorage().clearAll()
    }

    private fun launch() = ActivityScenario.launch<MainActivity>(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(ComponentName(app.packageName, SplashEntryPoint.LAUNCHER_ACTIVITY)))

    @Test fun emptyStoreOpensTheExistingLoginScreen() {
        launch().use { scenario ->
            val description = app.getString(teamcityapp.features.login.impl.R.string.text_app_description)
            compose.waitUntil(10_000) { compose.onAllNodesWithText(description).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(description).assertIsDisplayed()
            org.junit.Assert.assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.recreate()
            compose.onNodeWithText(description).assertIsDisplayed()
        }
    }

    @Test fun storedAccountOpensTheExistingProjectsScreen() {
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        launch().use { scenario ->
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("navigation:row:project:id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("navigation:row:project:id").assertIsDisplayed()
            compose.onNodeWithTag("home:tab:Projects").assertIsSelected()
            org.junit.Assert.assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.recreate()
            compose.onNodeWithTag("navigation:row:project:id").assertIsDisplayed()
            compose.onNodeWithTag("home:tab:Projects").assertIsSelected()
        }
    }
}
