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

package teamcityapp.features.test_details.view

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import io.reactivex.Single
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import org.hamcrest.Matchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.test_details.repository.models.TestOccurrence
import java.util.concurrent.TimeUnit

/**
 * Tests for [TestDetailsActivity]
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TestDetailsActivityTest {

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
    val activityRule: CustomActivityTestRule<TestDetailsActivity> =
        CustomActivityTestRule(TestDetailsActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Mock
    lateinit var test: TestOccurrence

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storage = app.appInjector.sharedUserStorage()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserSeesTestDetails() {
        // Prepare mocks
        val testDetails = "Test details"
        `when`(test.details).thenReturn(testDetails)
        `when`(teamCityService.testOccurrence(anyString())).thenReturn(
            Single.just(test)
        )

        // Prepare intent
        val intent = Intent()
        val b = Bundle()
        b.putString(TestDetailsActivity.ARG_TEST_URL, "/test")
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking toolbar title
        TestUtils.matchToolbarTitle("Details")

        // Check web view content
        Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.ID, "test_details"))
            .withTimeout(5, TimeUnit.SECONDS)
            .check(
                WebViewAssertions.webMatches(
                    DriverAtoms.getText(),
                    Matchers.containsString(testDetails)
                )
            )
    }

    @Test
    fun testUserSeesNoDataIfTestDetailsAreNotProvided() {
        // Prepare mocks
        `when`(test.details).thenReturn("")
        `when`(teamCityService.testOccurrence(anyString())).thenReturn(
            Single.just(test)
        )

        // Prepare intent
        val intent = Intent()
        val b = Bundle()
        b.putString(TestDetailsActivity.ARG_TEST_URL, "/test")
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking toolbar title
        TestUtils.matchToolbarTitle("Details")

        // check no data message
        onView(withId(R.id.empty)).check(
            matches(
                allOf(
                    withText(R.string.text_empty_test_details),
                    isDisplayed()
                )
            )
        )
    }

    @Test
    fun testUserSeesErrorMessageIfDetailsIsNotLoaded() {
        // Prepare mocks
        `when`(teamCityService.testOccurrence(anyString())).thenReturn(
            Single.error(RuntimeException("Errror!"))
        )

        // Prepare intent
        val intent = Intent()
        val b = Bundle()
        b.putString(TestDetailsActivity.ARG_TEST_URL, "/test")
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking toolbar title
        TestUtils.matchToolbarTitle("Details")

        // check details error
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }
}
