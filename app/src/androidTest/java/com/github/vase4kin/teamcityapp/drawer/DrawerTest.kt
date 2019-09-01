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

package com.github.vase4kin.teamcityapp.drawer

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.matchToolbarTitle
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy

/**
 * Tests for Drawer
 */
@RunWith(AndroidJUnit4::class)
class DrawerTest {

    @JvmField
    @Rule
    val daggerRule: DaggerMockRule<RestApiComponent> =
        DaggerMockRule(RestApiComponent::class.java, RestApiModule(Mocks.URL))
            .addComponentDependency(
                AppComponent::class.java,
                AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication)
            )
            .set { restApiComponent ->
                val app =
                    InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
                app.setRestApiInjector(restApiComponent)
            }

    @JvmField
    @Rule
    val activityTestRule: CustomActivityTestRule<HomeActivity> =
        CustomActivityTestRule(HomeActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    companion object {
        @JvmStatic
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().clearAll()
        app.restApiInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        activityTestRule.launchActivity(null)
    }

    @Test
    fun testUserCanSeeInfo() {
        // Opening drawer
        clickOnBurgerButton()

        // Check userInfo
        onView(allOf(withId(R.id.material_drawer_account_header_name), isDisplayed()))
            .check(matches(withText("Guest user")))

        onView(allOf(withId(R.id.material_drawer_account_header_email), isDisplayed()))
            .check(matches(withText(Mocks.URL)))
    }

    @Test
    fun testUserCanSeeProjectsIsSelectedByDefault() {
        // Opening drawer
        clickOnBurgerButton()

        // Check projects is selected
        onView(
            allOf(
                withId(R.id.material_drawer_name),
                withText(R.string.home_drawer_item),
                isDisplayed()
            )
        )
            .check(matches(isSelected()))
    }

    @Test
    fun testUserCanNavigateToProjectsScreen() {
        // open build type
        onView(withText("build type"))
            .perform(click())

        // Checking toolbar title
        matchToolbarTitle("build type")

        // Opening drawer
        onView(withId(R.id.material_drawer_layout))
            .perform(open())

        // Check projects is opened
        onView(
            allOf(
                withId(R.id.material_drawer_name),
                withText(R.string.home_drawer_item),
                isDisplayed()
            )
        )
            .perform(click())

        // Checking toolbar title
        matchToolbarTitle("Projects")
    }

    @Test
    fun testUserCanNavigateToAgentsScreen() {
        // Opening drawer
        clickOnBurgerButton()

        // Check agents is opened
        onView(
            allOf(
                withId(R.id.material_drawer_name),
                withText(R.string.agents_drawer_item),
                isDisplayed()
            )
        )
            .perform(click())

        // Checking toolbar title
        matchToolbarTitle("Agents")

        // Opening drawer
        onView(withId(R.id.material_drawer_layout))
            .perform(open())

        // Check about is selected
        onView(
            allOf(
                withId(R.id.material_drawer_name),
                withText(R.string.agents_drawer_item),
                isDisplayed()
            )
        )
            .check(matches(isSelected()))
    }

    @Test
    fun testUserCanNavigateToAboutScreen() {
        // Opening drawer
        clickOnBurgerButton()

        // Check about is opened
        onView(
            allOf(
                withId(R.id.material_drawer_name),
                withText(R.string.about_drawer_item),
                isDisplayed()
            )
        )
            .perform(click())

        // Checking toolbar title
        matchToolbarTitle("About")

        // Opening drawer
        onView(withId(R.id.material_drawer_layout))
            .perform(open())

        // Check about is selected
        onView(
            allOf(
                withId(R.id.material_drawer_name),
                withText(R.string.about_drawer_item),
                isDisplayed()
            )
        )
            .check(matches(isSelected()))
    }

    @Test
    fun testUserCanNavigateToAccounts() {
        // Opening drawer
        clickOnBurgerButton()

        // Click on account
        onView(allOf(withId(R.id.material_drawer_account_header_name), isDisplayed()))
            .perform(click())

        // Opening managing account activity
        onView(
            allOf(
                withId(R.id.material_drawer_name),
                withText(R.string.title_activity_account_list)
            )
        )
            .perform(click())

        // Checking toolbar title
        matchToolbarTitle("Manage Accounts")
    }

    /**
     * Open drawer by clicking on burger button
     */
    private fun clickOnBurgerButton() {
        onView(withContentDescription("Open")).perform(click())
    }
}
