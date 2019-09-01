/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.splash.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.login.view.LoginActivity
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [SplashActivity]
 */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {

    @JvmField
    @Rule
    val daggerRule: DaggerMockRule<AppComponent> = DaggerMockRule(
        AppComponent::class.java,
        AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication)
    )
        .set { appComponent ->
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            app.setAppInjector(appComponent)
        }

    @JvmField
    @Rule
    val activityRule: CustomIntentsTestRule<SplashActivity> = CustomIntentsTestRule(SplashActivity::class.java)

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.appInjector.sharedUserStorage().clearAll()
    }

    /**
     * Espresso can't record intents in case if there's no UI interactions
     */
    @Ignore
    @Test
    fun testUserNavigatesToRootProjectsActivityIgnored() {
        // Prepate data
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)

        // Launch activity
        activityRule.launchActivity(null)

        // Check launched intent
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                toPackage("com.github.vase4kin.teamcityapp.mock.debug")
            )
        )
    }

    /**
     * Espresso can't record intents in case if there's no UI interactions
     */
    @Ignore
    @Test
    fun testUserNavigatesToLoginActivityIgnored() {
        // Launch activity
        activityRule.launchActivity(null)

        // Check launched intent
        intended(
            allOf(
                hasComponent(LoginActivity::class.java.name),
                toPackage("com.github.vase4kin.teamcityapp.mock.debug")
            )
        )
    }

    /**
     * Workaround test to test that root projects activity is opened
     */
    @Ignore
    @Test
    fun testUserNavigatesToRootProjectsActivity() {
        // Prepate data
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)

        // Launch activity
        activityRule.launchActivity(null)

        // Check that root projects activity is opened
        TestUtils.matchToolbarTitle("Projects")
    }

    /**
     * Workaround test to test that login activity is opened
     */
    @Test
    fun testUserNavigatesToLoginActivity() {
        // Launch activity
        activityRule.launchActivity(null)

        // Checking that Login Activity is opened
        onView(withText(R.string.text_app_description)).check(matches(isDisplayed()))
    }
}
