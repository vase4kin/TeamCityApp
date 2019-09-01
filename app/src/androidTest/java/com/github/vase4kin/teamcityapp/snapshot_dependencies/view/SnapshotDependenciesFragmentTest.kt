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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.view

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.eq
import org.mockito.Mockito.verify
import org.mockito.Spy

private const val NAME = "name"

/**
 * Tests for [SnapshotDependenciesFragment]
 */
@RunWith(AndroidJUnit4::class)
class SnapshotDependenciesFragmentTest {

    @JvmField
    @Rule
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

    @JvmField
    @Rule
    val activityRule: CustomIntentsTestRule<BuildDetailsActivity> = CustomIntentsTestRule(BuildDetailsActivity::class.java)

    @Spy
    private val teamCityService = FakeTeamCityServiceImpl()

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
    }

    @Test
    fun testUserCanNotSeeSnapshotIfTheyNotAvailable() {

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val mockedBuild = Mocks.queuedBuild3()
        mockedBuild.snapshotBuilds = null
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, mockedBuild)
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scroll to properties tab title
        onView(withText(R.string.tab_parameters))
            .perform(scrollTo())

        onView(withText(R.string.tab_snapshot_dependencies))
            .check(doesNotExist())
    }

    @Test
    fun testUserCanSeeSnapshotIfTheyAvailable() {
        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val mockedBuild = Mocks.queuedBuild3()
        mockedBuild.setId("buildId")
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, mockedBuild)
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Scroll to properties tab title
        onView(withText(R.string.tab_snapshot_dependencies))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        verify<TeamCityService>(teamCityService).listBuilds(eq("buildId"))

        // List has item with header
        onView(withId(R.id.snapshot_recycler_view)).check(TestUtils.hasItemsCount(5))
        // Checking header 1
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("project name - build type name")))
        // Checking adapter item 1
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(1, R.id.itemTitle))
            .check(matches(withText("Queued build")))
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(1, R.id.itemSubTitle))
            .check(matches(withText("refs/heads/master")))
        // Checking adapter item 2
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(2, R.id.itemTitle))
            .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(2, R.id.itemSubTitle))
            .check(matches(withText("refs/heads/dev")))
        // Checking header 2
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(3, R.id.section_text))
            .check(matches(withText("Project name one two - Another configuration")))
        // Checking adapter item 3
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(4, R.id.itemTitle))
            .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
        onView(withRecyclerView(R.id.snapshot_recycler_view).atPositionOnView(4, R.id.itemSubTitle))
            .check(matches(withText("refs/heads/dev0feature")))
    }
}
