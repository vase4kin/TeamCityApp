/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.runningbuilds.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.hasItemsCount
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.matchHomeToolbarTitle
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Spy

@Ignore("https://github.com/vase4kin/TeamCityApp/issues/362")
@RunWith(AndroidJUnit4::class)
class RunningBuildsFragmentTest {

    @Rule
    @JvmField
    val daggerMockRule: DaggerMockRule<RestApiComponent> =
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
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    private val storage: SharedUserStorage
        get() {
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
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
    fun testUserCanSeeUpdatedToolbar() {
        activityRule.launchActivity(null)

        // Click on running builds tab
        clickOnRunningbuildsTab()

        // Checking toolbar title
        matchHomeToolbarTitle(
            R.id.home_running_builds_toolbar_title,
            R.string.running_builds_drawer_item
        )
    }

    @Ignore
    @Test
    fun testUserCanSeeSuccessFullyLoadedRunningBuilds() {
        // Favorites two builds
        val locator = BuildListFilterImpl().apply {
            setFilter(FilterBuildsView.FILTER_RUNNING)
        }.toLocator()
        val buildTypeId1 = "id1"
        val buildTypeId2 = "id2"
        storage.addBuildTypeToFavorites(buildTypeId1)
        storage.addBuildTypeToFavorites(buildTypeId2)
        val buildsByBuildTypeId1 = listOf<Build>(Mocks.runningBuild())
        val buildsByBuildTypeId2 = listOf<Build>(Mocks.runningBuild2())

        val buildTypeLocator1 = locator + ",${buildTypeIdLocator(buildTypeId1)}"
        val buildTypeLocator2 = locator + ",${buildTypeIdLocator(buildTypeId2)}"
        `when`(teamCityService.listRunningBuilds(buildTypeLocator1, null))
            .thenReturn(Single.just(Builds(buildsByBuildTypeId1.size, buildsByBuildTypeId1)))
        `when`(teamCityService.listRunningBuilds(buildTypeLocator1, "count"))
            .thenReturn(Single.just(Builds(buildsByBuildTypeId1.size, buildsByBuildTypeId1)))
        `when`(teamCityService.listRunningBuilds(buildTypeLocator2, null))
            .thenReturn(Single.just(Builds(buildsByBuildTypeId2.size, buildsByBuildTypeId2)))
        `when`(teamCityService.listRunningBuilds(buildTypeLocator2, "count"))
            .thenReturn(Single.just(Builds(buildsByBuildTypeId2.size, buildsByBuildTypeId2)))

        // ALL (one build by all)
        val builds = listOf<Build>(Mocks.runningBuild())
        `when`(teamCityService.listRunningBuilds(locator, null))
            .thenReturn(Single.just(Builds(builds.size, builds)))
        `when`(teamCityService.listRunningBuilds(locator, "count"))
            .thenReturn(Single.just(Builds(builds.size, builds)))

        activityRule.launchActivity(null)

        // Click on running builds tab
        clickOnRunningbuildsTab()

        // List has item with header
        onView(withId(R.id.running_builds_recycler_view)).check(hasItemsCount(2))
        // Checking header
        onView(withId(R.id.section_text)).check(matches(withText("project name - build type name")))
        // Checking adapter item
        onView(
            withRecyclerView(R.id.running_builds_recycler_view).atPositionOnView(
                1,
                R.id.itemTitle
            )
        ).check(
            matches(
                withText("Running tests")
            )
        )
        onView(
            withRecyclerView(R.id.running_builds_recycler_view).atPositionOnView(
                1,
                R.id.itemSubTitle
            )
        ).check(matches(withText("refs/heads/master")))
        onView(
            withRecyclerView(R.id.running_builds_recycler_view).atPositionOnView(
                1,
                R.id.buildNumber
            )
        ).check(
            matches(
                withText("#2458")
            )
        )
    }

    @Test
    fun testUserCanClickOnSection() {
        storage.addBuildTypeToFavorites("id")
        activityRule.launchActivity(null)

        // Click on running builds tab
        clickOnRunningbuildsTab()

        // Click on header header
        onView(withId(R.id.section_text))
            .check(matches(withText("project name - build type name")))
            .perform(click())

        // Check activity been opned
        intended(
            allOf(
                hasComponent(BuildListActivity::class.java.name),
                hasExtras(
                    allOf(
                        hasEntry(
                            equalTo(BundleExtractorValues.BUILD_LIST_FILTER),
                            equalTo<Any>(null)
                        ),
                        hasEntry(
                            equalTo(BundleExtractorValues.ID),
                            equalTo("Checkstyle_IdeaInspectionsPullRequest")
                        ),
                        hasEntry(equalTo(BundleExtractorValues.NAME), equalTo("build type name"))
                    )
                )
            )
        )
    }

    @Test
    fun testUserCanSeeFailureMessageIfSmthHappendsOnRunningBuildsLoading() {
        storage.addBuildTypeToFavorites("id")

        doReturn(Single.error<Builds>(RuntimeException("smth bad happend!")))
            .whenever(teamCityService).listRunningBuilds(any(), any())

        activityRule.launchActivity(null)

        // Click on running builds tab
        clickOnRunningbuildsTab()

        // Check badge count
        checkRunningTabBadgeCount("0")

        // Check error
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeEmptyDataMessageIfRunningBuildListIsEmpty() {
        doReturn(Single.just<Builds>(Builds(0, emptyList())))
            .whenever(teamCityService).listRunningBuilds(any(), any())

        activityRule.launchActivity(null)

        // Click on running builds tab
        clickOnRunningbuildsTab()

        // Check empty view
        onView(withId(R.id.running_empty_title_view)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_favorite_running_builds)))

        // filter builds to show all
        onView(allOf(withId(R.id.home_floating_action_button), isDisplayed())).perform(click())
        onView(withText(R.string.text_show_running)).perform(click())

        checkRunningTabBadgeCount("0")
        onView(withId(R.id.running_empty_title_view)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.empty_list_message_running_builds)))
    }

    private fun clickOnRunningbuildsTab() {
        onView(withId(R.id.running_builds))
            .perform(click())
    }

    private fun checkRunningTabBadgeCount(count: String) {
        // FIXME
    }

    private fun buildTypeIdLocator(buildTypeId: String): String = "buildType:$buildTypeId"
}
