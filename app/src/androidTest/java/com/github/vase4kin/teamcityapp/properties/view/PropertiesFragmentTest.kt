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

package com.github.vase4kin.teamcityapp.properties.view

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.properties.repository.models.Properties

private const val NAME = "name"
private const val TIMEOUT = 5000

/**
 * Tests for [PropertiesFragment]
 */
@RunWith(AndroidJUnit4::class)
class PropertiesFragmentTest {

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
    val activityRule: CustomActivityTestRule<BuildDetailsActivity> =
        CustomActivityTestRule(BuildDetailsActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Spy
    private val build: Build = Mocks.successBuild()

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
    fun testUserCanSeeBuildProperties() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking properties tab title
        onView(withText("Parameters"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Check properties
        onView(withId(R.id.properties_recycler_view)).check(TestUtils.hasItemsCount(2))
        onView(
            withRecyclerView(R.id.properties_recycler_view).atPositionOnView(
                0,
                R.id.title
            )
        ).check(matches(withText("sdk")))
        onView(
            withRecyclerView(R.id.properties_recycler_view).atPositionOnView(
                0,
                R.id.subTitle
            )
        ).check(matches(withText("24")))
        onView(
            withRecyclerView(R.id.properties_recycler_view).atPositionOnView(
                1,
                R.id.title
            )
        ).check(matches(withText("userName")))
        onView(
            withRecyclerView(R.id.properties_recycler_view).atPositionOnView(
                1,
                R.id.subTitle
            )
        ).check(matches(withText("Murdock")))
    }

    @Test
    fun testUserCanSeeEmptyPropertiesMessageIfPropertiesAreNull() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild(null))
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking properties tab title
        onView(withText("Parameters"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking empty message
        onView(withText(R.string.empty_list_message_parameters)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeEmptyPropertiesMessageIfPropertiesAreEmpty() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(
            BundleExtractorValues.BUILD,
            Mocks.successBuild(
                Properties(
                    emptyList()
                )
            )
        )
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking properties tab title
        onView(withText("Parameters"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking empty message
        onView(withText(R.string.empty_list_message_parameters)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun testUserCanCopyPropertyValueFromTheList() {
        ConditionWatcher.setTimeoutLimit(TIMEOUT)
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking properties tab title
        onView(withText("Parameters"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Click on parameter
        onView(withRecyclerView(R.id.properties_recycler_view).atPosition(0))
            .perform(click())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "The parameters menu is not opened"
            }

            override fun checkCondition(): Boolean {
                var isParameterClicked = false
                try {
                    onView(withText(R.string.build_element_copy)).check(matches(isDisplayed()))
                    isParameterClicked = true
                } catch (ignored: Exception) {
                    onView(withRecyclerView(R.id.properties_recycler_view).atPosition(0))
                        .perform(click())
                }

                return isParameterClicked
            }
        })

        // Clicking on copy
        onView(withText(R.string.build_element_copy)).perform(click())

        // Checking toast message
        onView(withText(R.string.build_element_copy_text))
            .check(matches(isDisplayed()))
    }
}
