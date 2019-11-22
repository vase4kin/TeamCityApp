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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.account.manage.view.ManageAccountsActivity
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import teamcityapp.features.about.AboutActivity

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
    val activityTestRule: CustomIntentsTestRule<HomeActivity> =
        CustomIntentsTestRule(HomeActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    companion object {
        @JvmStatic
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    private val sharedStorage: SharedUserStorage
        get() {
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            return app.restApiInjector.sharedUserStorage()
        }

    @Before
    fun setUp() {
        val sharedUserStorage = this.sharedStorage
        sharedUserStorage.clearAll()
        sharedUserStorage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSwitchAccounts() {
        val url = Mocks.URL + "/v2"
        sharedStorage.saveGuestUserAccountAndSetItAsActive(url, false)
        activityTestRule.launchActivity(null)
        // Opening drawer
        clickOnBurgerButton()

        // Check active user details
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                0,
                R.id.title
            )
        )
            .check(matches(withText("Guest user")))
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                0,
                R.id.subTitle
            )
        )
            .check(matches(withText(url)))

        // Check another user
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                2,
                R.id.title
            )
        )
            .check(matches(withText("Guest user")))
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                2,
                R.id.subTitle
            )
        )
            .check(matches(withText(Mocks.URL)))

        // Click on another user
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPosition(
                2
            )
        )
            .perform(click())

        // Check user has been switched
        Intents.intended(
            allOf(
                IntentMatchers.hasComponent(HomeActivity::class.java.name),
                IntentMatchers.hasExtras(
                    BundleMatchers.hasEntry(
                        CoreMatchers.equalTo(
                            BundleExtractorValues.IS_REQUIRED_TO_RELOAD
                        ), CoreMatchers.equalTo(true)
                    )
                )
            )
        )

        // Check that active user
        Assert.assertEquals(sharedStorage.activeUser.teamcityUrl, Mocks.URL)
    }

    @Test
    fun testUserCanSeeActiveUser() {
        activityTestRule.launchActivity(null)
        // Opening drawer
        clickOnBurgerButton()

        // Check active user details
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                0,
                R.id.title
            )
        )
            .check(matches(withText("Guest user")))
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                0,
                R.id.subTitle
            )
        )
            .check(matches(withText(Mocks.URL)))

        // Check active user is not clickable
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPosition(
                0
            )
        )
            .check(matches(not(isClickable())))
    }

    @Test
    fun testUserCanNavigateToAboutScreen() {
        activityTestRule.launchActivity(null)
        // Opening drawer
        clickOnBurgerButton()

        // Click on about
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                5,
                R.id.title
            )
        )
            .check(matches(withText(R.string.about_drawer_item)))
            .perform(click())

        // Check about screen is being opened
        Intents.intended(
            IntentMatchers.hasComponent(AboutActivity::class.java.name)
        )
    }

    @Test
    fun testUserCanNavigateToAccounts() {
        activityTestRule.launchActivity(null)
        // Opening drawer
        clickOnBurgerButton()

        // Opening managing account activity
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                3,
                R.id.title
            )
        )
            .check(matches(withText(R.string.text_manage_accounts)))
            .perform(click())

        // Check manage accounts screen is being opened
        Intents.intended(
            IntentMatchers.hasComponent(ManageAccountsActivity::class.java.name)
        )
    }

    @Test
    fun testUserCanNavigateToCreateAccount() {
        activityTestRule.launchActivity(null)
        // Opening drawer
        clickOnBurgerButton()

        // Opening new account activity
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                2,
                R.id.title
            )
        )
            .check(matches(withText(R.string.text_add_account)))
            .perform(click())

        // Check create account screen is being opened
        Intents.intended(
            IntentMatchers.hasComponent(CreateAccountActivity::class.java.name)
        )
    }

    @Test
    fun testUserCanSeeBottomNavigation() {
        activityTestRule.launchActivity(null)
        // Opening drawer
        clickOnBurgerButton()

        // Check rate the app is there
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                7,
                R.id.privacy
            )
        )
            .check(matches(allOf(withText(R.string.about_app_text_privacy), isDisplayed())))

        // Check the privicy policy is there
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.bottom_sheet_drawer_recycler_view).atPositionOnView(
                7,
                R.id.rate_the_app
            )
        )
            .check(matches(allOf(withText(R.string.text_rate_the_app), isDisplayed())))
    }

    /**
     * Open drawer by clicking on burger button
     */
    private fun clickOnBurgerButton() {
        onView(withContentDescription(R.string.content_navigation_content_description)).perform(
            click()
        )
    }
}
