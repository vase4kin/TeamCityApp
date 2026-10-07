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
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
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
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.properties.repository.models.Properties

private const val NAME = "name"
private const val TIMEOUT = 5000

/**
 * Tests for [PropertiesFragment]
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PropertiesFragmentTest {

    private fun clickSheetAction(label: String) {
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(label).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        compose.onNodeWithText(label).performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("sheet:content").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty()
        }
    }

    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockitoRule = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val apiRule = HiltApiTestRule(hiltRule) { teamCityService }

    @JvmField
    @Rule(order = 3)
    val activityRule: CustomActivityTestRule<BuildDetailsActivity> =
        CustomActivityTestRule(BuildDetailsActivity::class.java)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage()
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

        compose.onNodeWithText("sdk").assertIsDisplayed()
        compose.onNodeWithText("24").assertIsDisplayed()
        compose.onNodeWithText("userName").assertIsDisplayed()
        compose.onNodeWithText("Murdock").assertIsDisplayed()
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
        compose.onNodeWithText(activityRule.activity.getString(teamcityapp.features.properties.impl.R.string.empty_list_message_parameters)).assertIsDisplayed()
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
        compose.onNodeWithText(activityRule.activity.getString(teamcityapp.features.properties.impl.R.string.empty_list_message_parameters)).assertIsDisplayed()
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
        compose.onNodeWithTag("properties:row:0").performClick()

        // Clicking on copy
        clickSheetAction(InstrumentationRegistry.getInstrumentation().targetContext.getString(teamcityapp.features.bottom_sheet.impl.R.string.build_element_copy))

        // Checking toast message
        onView(withText(R.string.build_element_copy_text))
            .check(matches(isDisplayed()))
    }
}
