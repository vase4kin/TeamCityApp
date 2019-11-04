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

package com.github.vase4kin.teamcityapp.tests.view

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents
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
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.test_details.view.TestDetailsActivity

private const val BUILD_TYPE_NAME = "name"

/**
 * Tests for [TestOccurrencesFragment]
 */
@RunWith(AndroidJUnit4::class)
class TestOccurrencesFragmentTest {

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
    val activityRule: CustomIntentsTestRule<BuildDetailsActivity> =
        CustomIntentsTestRule(BuildDetailsActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Spy
    private val build = Mocks.failedBuild()

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
    fun testUserCanSeeBuildFailedTests() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking tests tab title
        onView(withText("Tests (16)"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Check failed tests
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("Failed (2)")))
        onView(withId(R.id.tests_recycler_view)).check(TestUtils.hasItemsCount(3))
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(1, R.id.title))
            .check(matches(withText("Test 1")))
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(2, R.id.title))
            .check(matches(withText("Test 6")))
    }

    @Test
    fun testUserCanBeNavigatedToFailedTest() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking tests tab title
        onView(withText("Tests (16)"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Click on first failed test
        onView(withRecyclerView(R.id.tests_recycler_view).atPosition(1)).perform(click())

        // Check recorded intent
        intended(
            allOf(
                hasComponent(TestDetailsActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.TEST_URL),
                        equalTo("/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)")
                    )
                )
            )
        )
    }

    @Test
    fun testUserCanSeeEmptyFailedTestsMessage() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        `when`(teamCityService.listTestOccurrences("/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10")).thenReturn(
            Single.just(TestOccurrences(0))
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking tests tab title
        onView(withText("Tests (16)"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Check failed tests empty message
        onView(withText(R.string.empty_passed_tests)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeePassedTests() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking tests tab title
        onView(withText("Tests (16)"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Open menu
        openActionBarOverflowOrOptionsMenu(activityRule.activity)

        // Show passed tests
        onView(withText("Show passed")).perform(click())

        // Check passed tests
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("Passed (10)")))
        onView(withId(R.id.tests_recycler_view)).check(TestUtils.hasItemsCount(3))
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(1, R.id.title))
            .check(matches(withText("Test 5")))
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(2, R.id.title))
            .check(matches(withText("Test 2")))
    }

    @Test
    fun testUserCanNotInteractWithPassedTests() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking tests tab title
        onView(withText("Tests (16)"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Open menu
        openActionBarOverflowOrOptionsMenu(activityRule.activity)

        // Show passed tests
        onView(withText("Show passed")).perform(click())

        // Click on first passed test
        onView(withRecyclerView(R.id.tests_recycler_view).atPosition(1)).perform(click())

        // Check no intent were started
        assertNoUnverifiedIntents()
    }

    @Test
    fun testUserCanSeeIgnoredTests() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking tests tab title
        onView(withText("Tests (16)"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Open menu
        openActionBarOverflowOrOptionsMenu(activityRule.activity)

        // Show ignored tests
        onView(withText("Show ignored")).perform(click())

        // Check ignored tests
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(0, R.id.section_text))
            .check(matches(withText("Ignored (4)")))
        onView(withId(R.id.tests_recycler_view)).check(TestUtils.hasItemsCount(3))
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(1, R.id.title))
            .check(matches(withText("Test 4")))
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(2, R.id.title))
            .check(matches(withText("Test 9")))
    }

    @Test
    fun testUserCanNotInteractWithIgnoredTests() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking tests tab title
        onView(withText("Tests (16)"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Open menu
        openActionBarOverflowOrOptionsMenu(activityRule.activity)

        // Show ignored tests
        onView(withText("Show passed")).perform(click())

        // Click on first ignored test
        onView(withRecyclerView(R.id.tests_recycler_view).atPosition(1)).perform(click())

        // Check no intent were started
        assertNoUnverifiedIntents()
    }
}
