/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.queue.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView
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
class BuildQueueFragmentTest {

    @Rule
    @JvmField
    var daggerRule: DaggerMockRule<RestApiComponent> = DaggerMockRule(RestApiComponent::class.java, RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent::class.java, AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication))
            .set { restApiComponent ->
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
                app.restApiInjector = restApiComponent
            }

    @Rule
    @JvmField
    var activityRule = CustomActivityTestRule(HomeActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    private val storage: SharedUserStorage
        get() {
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            return app.appInjector.sharedUserStorage()
        }

    companion object {
        @JvmStatic
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
    fun testUserCanSeeUseFiltersToLoadBuilds() {
        // Favorites
        val buildTypeId1 = "id1"
        val buildTypeId2 = "id2"
        storage.addBuildTypeToFavorites(buildTypeId1)
        storage.addBuildTypeToFavorites(buildTypeId2)
        val buildsByBuildTypeId1 = listOf<Build>(Mocks.queuedBuild1())
        val buildsByBuildTypeId2 = listOf<Build>(Mocks.queuedBuild3())
        `when`(teamCityService.listQueueBuilds(buildTypeIdLocator(buildTypeId1), null))
                .thenReturn(Single.just(Builds(buildsByBuildTypeId1.size, buildsByBuildTypeId1)))
        `when`(teamCityService.listQueueBuilds(buildTypeIdLocator(buildTypeId1), "count"))
                .thenReturn(Single.just(Builds(buildsByBuildTypeId1.size, buildsByBuildTypeId1)))
        `when`(teamCityService.listQueueBuilds(buildTypeIdLocator(buildTypeId2), null))
                .thenReturn(Single.just(Builds(buildsByBuildTypeId2.size, buildsByBuildTypeId2)))
        `when`(teamCityService.listQueueBuilds(buildTypeIdLocator(buildTypeId2), "count"))
                .thenReturn(Single.just(Builds(buildsByBuildTypeId2.size, buildsByBuildTypeId2)))

        // ALL
        val builds = listOf<Build>(Mocks.queuedBuild1(), Mocks.queuedBuild2(), Mocks.queuedBuild3())
        `when`(teamCityService.listQueueBuilds(null, null))
                .thenReturn(Single.just(Builds(builds.size, builds)))
        `when`(teamCityService.listQueueBuilds(null, "count"))
                .thenReturn(Single.just(Builds(builds.size, builds)))

        activityRule.launchActivity(null)

        // Click on build queue tab
        clickOnBuildQueueTab()

        // Check badge
        checkBuildQueueTabBadgeCount("2")

        // List has item with header
        onView(withId(R.id.build_queue_recycler_view)).check(hasItemsCount(4))
        // Checking header 1
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("project name - build type name")))
        // Checking adapter item 1
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Queued build")))
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(1, R.id.itemSubTitle))
                .check(matches(withText("refs/heads/master")))
        // Checking header 2
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(2, R.id.section_text))
                .check(matches(withText("Project name one two - Another configuration")))
        // Checking adapter item 3
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(3, R.id.itemTitle))
                .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(3, R.id.itemSubTitle))
                .check(matches(withText("refs/heads/dev0feature")))

        // filter builds to show all
        onView(allOf(withId(R.id.floating_action_button), isDisplayed())).perform(click())
        onView(withText(R.string.text_show_queued)).perform(click())

        // check snack bar text
        onView(withText(R.string.text_filters_applied)).check(matches(isDisplayed()))

        // Check badge
        checkBuildQueueTabBadgeCount("3")

        // Check all builds

        // List has item with header
        onView(withId(R.id.build_queue_recycler_view)).check(hasItemsCount(5))
        // Checking header 1
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("project name - build type name")))
        // Checking adapter item 1
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Queued build")))
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(1, R.id.itemSubTitle))
                .check(matches(withText("refs/heads/master")))
        // Checking adapter item 2
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(2, R.id.itemTitle))
                .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(2, R.id.itemSubTitle))
                .check(matches(withText("refs/heads/dev")))
        // Checking header 2
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(3, R.id.section_text))
                .check(matches(withText("Project name one two - Another configuration")))
        // Checking adapter item 3
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(4, R.id.itemTitle))
                .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
        onView(withRecyclerView(R.id.build_queue_recycler_view).atPositionOnView(4, R.id.itemSubTitle))
                .check(matches(withText("refs/heads/dev0feature")))
    }

    @Test
    fun testUserCanSeeUpdatedToolbar() {
        activityRule.launchActivity(null)

        // Click on build queue tab
        clickOnBuildQueueTab()

        matchToolbarTitle("Build queue")
    }

    @Test
    fun testUserCanSeeFailureMessageForFavoritesQueueBuilds() {
        // Prepare data
        `when`(teamCityService.listQueueBuilds(anyString(), anyString())).thenReturn(Single.error(RuntimeException("smth bad happend!")))
        storage.addBuildTypeToFavorites("id1")

        activityRule.launchActivity(null)

        // Click on build queue tab
        clickOnBuildQueueTab()

        checkBuildQueueTabBadgeCount("0")

        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))

        // filter builds to show all
        onView(allOf(withId(R.id.floating_action_button), isDisplayed())).perform(click())
        onView(withText(R.string.text_show_queued)).perform(click())

        checkBuildQueueTabBadgeCount("0")

        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeFailureMessageForAllQueueBuilds() {
        // Prepare data
        `when`(teamCityService.listQueueBuilds(anyString(), anyString())).thenReturn(Single.error(RuntimeException("smth bad happend!")))
        storage.addBuildTypeToFavorites("id1")

        activityRule.launchActivity(null)

        // Click on build queue tab
        clickOnBuildQueueTab()

        checkBuildQueueTabBadgeCount("0")

        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeEmptyDataMessageIfBuildQueueIsEmpty() {
        `when`(teamCityService.listQueueBuilds(anyString(), anyString())).thenReturn(Single.just(Builds(0, emptyList())))

        activityRule.launchActivity(null)

        // Click on build queue tab
        clickOnBuildQueueTab()

        checkBuildQueueTabBadgeCount("0")
        onView(withId(R.id.queued_empty_title_view)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_favorite_build_queue)))

        // filter builds to show all
        onView(allOf(withId(R.id.floating_action_button), isDisplayed())).perform(click())
        onView(withText(R.string.text_show_queued)).perform(click())

        checkBuildQueueTabBadgeCount("0")
        onView(withId(R.id.queued_empty_title_view)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_build_queue)))
    }

    private fun clickOnBuildQueueTab() {
        onView(withChild(allOf(withId(R.id.bottom_navigation_small_item_title), withText(R.string.build_queue_drawer_item))))
                .perform(click())
    }

    private fun checkBuildQueueTabBadgeCount(count: String) {
        onView(allOf(
                withChild(allOf(withId(R.id.bottom_navigation_notification), withText(count))),
                withChild(allOf(withId(R.id.bottom_navigation_small_item_title), withText(R.string.build_queue_drawer_item))))
        )
                .check(matches(isDisplayed()))
    }

    private fun buildTypeIdLocator(buildTypeId: String): String = "buildType:$buildTypeId"
}