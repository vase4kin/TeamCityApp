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

package com.github.vase4kin.teamcityapp.favorites.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
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
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount
import com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy

@RunWith(AndroidJUnit4::class)
class FavoritesFragmentTest {

    @Rule
    @JvmField
    val restComponentDaggerRule: DaggerMockRule<RestApiComponent> =
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

    @Rule
    @JvmField
    val activityRule = CustomIntentsTestRule(HomeActivity::class.java)

    @Spy
    val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    private val storage: SharedUserStorage
        get() {
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            return app.appInjector.sharedUserStorage()
        }

    companion object {
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    @Before
    fun setUp() {
        val storage = storage
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeUpdatedToolbarTitle() {
        // Launch activity
        activityRule.launchActivity(null)

        // click on favorites tab
        clickOnFavoritesTab()

        // Checking toolbar title
        matchToolbarTitle("Favorites")
    }

    @Test
    fun testUserCanSeeFavoritesListIfSmthBadHappensInFavoritesLoading() {
        // Prepare mocks
        `when`(teamCityService.buildType(anyString())).thenReturn(Single.error(RuntimeException("smth bad happend!")))
        storage.addBuildTypeToFavorites("id")
        storage.addBuildTypeToFavorites("id2")
        storage.addBuildTypeToFavorites("id3")

        // Launch activity
        activityRule.launchActivity(null)

        // click on favorites tab
        clickOnFavoritesTab()

        // Check empty list
        onView(withId(R.id.favorites_empty_title_view)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_favorites)))
    }

    @Test
    fun testUserCanSeeLoadEmptyListMessageIfFavoritesIsEmpty() {
        // launch activity
        activityRule.launchActivity(null)

        // click on favorites tab
        clickOnFavoritesTab()

        // Check badge
        checkFavoritesTabBadgeCount("0")

        // Check the list is empty
        onView(withId(R.id.favorites_empty_title_view)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_favorites)))
    }

    @Test
    fun testUserCanClickOnFabAndSeeProjects() {
        // launch activity
        activityRule.launchActivity(null)

        // click on favorites tab
        clickOnFavoritesTab()

        // click on fab
        onView(withId(R.id.home_floating_action_button)).perform(click())

        // check snack bar text
        onView(withText(R.string.text_info_add)).check(matches(isDisplayed()))

        // click on snack bar action
        onView(withText(R.string.text_info_add_action)).perform(click())

        // Checking toolbar title
        matchToolbarTitle("Projects")
    }

    @Test
    fun testUserCanAddBuildTypeToFavorites() {
        // prepare mocks
        `when`(teamCityService.buildType(Mocks.buildTypeMock().id)).thenReturn(Single.just(Mocks.buildTypeMock()))

        // launch activity
        activityRule.launchActivity(null)

        // Open build type
        onView(withText("build type"))
            .perform(click())

        // Click on add to favorites
        onView(withId(R.id.add_to_favorites)).perform(click())

        // check snack bar text
        onView(withText(R.string.text_add_to_favorites)).check(matches(isDisplayed()))

        // click on snack bar action
        onView(withText(R.string.text_view_favorites)).perform(click())

        // Checking toolbar title
        matchToolbarTitle("Favorites")

        // List has item with header
        onView(withId(R.id.favorites_recycler_view)).check(hasItemsCount(2))
        // Checking header 1
        onView(withRecyclerView(R.id.favorites_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("Secret project")))
        // Checking adapter item 1
        onView(withRecyclerView(R.id.favorites_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("build type")))
    }

    @Test
    fun testFavoritesListRefreshesOnResume() {
        // prepare mocks
        storage.addBuildTypeToFavorites(Mocks.buildTypeMock().id)
        `when`(teamCityService.buildType(Mocks.buildTypeMock().id)).thenReturn(Single.just(Mocks.buildTypeMock()))

        // launch activity
        activityRule.launchActivity(null)

        // click on favorites tab
        clickOnFavoritesTab()

        // Check badge
        checkFavoritesTabBadgeCount("1")

        // List has item with header
        onView(withId(R.id.favorites_recycler_view)).check(hasItemsCount(2))
        // Checking header 1
        onView(withRecyclerView(R.id.favorites_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("Secret project")))
        // Checking adapter item 1
        onView(withRecyclerView(R.id.favorites_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("build type")))

        // Click on item 1
        onView(withRecyclerView(R.id.favorites_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .perform(click())

        // Click on add to favorites
        onView(withId(R.id.add_to_favorites)).perform(click())

        // check snack bar text
        onView(withText(R.string.text_remove_from_favorites)).check(matches(isDisplayed()))

        pressBack()

        // Check badge
        checkFavoritesTabBadgeCount("0")

        // Check the list is empty
        onView(withId(R.id.favorites_empty_title_view)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_favorites)))
    }

    private fun clickOnFavoritesTab() {
        onView(
            withChild(
                allOf(
                    withId(R.id.bottom_navigation_small_item_title),
                    withText(R.string.favorites_drawer_item)
                )
            )
        )
            .perform(click())
    }

    private fun checkFavoritesTabBadgeCount(count: String) {
        onView(
            allOf(
                withChild(allOf(withId(R.id.bottom_navigation_notification), withText(count))),
                withChild(
                    allOf(
                        withId(R.id.bottom_navigation_small_item_title),
                        withText(R.string.favorites_drawer_item)
                    )
                )
            )
        )
            .check(matches(isDisplayed()))
    }
}
