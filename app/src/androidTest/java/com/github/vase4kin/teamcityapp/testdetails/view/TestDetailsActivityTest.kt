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

package com.github.vase4kin.teamcityapp.testdetails.view

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
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
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.test_details.feature.view.TestDetailsActivity
import teamcityapp.features.test_details.repository.models.TestOccurrence

/**
 * Tests for [TestDetailsActivity]
 */
@RunWith(AndroidJUnit4::class)
class TestDetailsActivityTest {

    @JvmField
    @Rule
    val daggerRule: DaggerMockRule<RestApiComponent> = DaggerMockRule(RestApiComponent::class.java, RestApiModule(Mocks.URL))
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
    val activityRule: CustomActivityTestRule<TestDetailsActivity> = CustomActivityTestRule(TestDetailsActivity::class.java)

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Mock
    lateinit var test: TestOccurrence

    @Test
    fun testUserSeesTestDetails() {
        // Prepare mocks
        `when`(test.details).thenReturn("Test details")
        `when`(teamCityService.testOccurrence(anyString())).thenReturn(
            Single.just(test)
        )

        // Prepare intent
        val intent = Intent()
        val b = Bundle()
        b.putString(BundleExtractorValues.TEST_URL, "/test")
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking toolbar title
        TestUtils.matchToolbarTitle("Details")

        // check details
        onView(withId(R.id.test_occurrence_details)).check(
            matches(
                allOf(
                    withText("Test details"),
                    isDisplayed()
                )
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
        b.putString(BundleExtractorValues.TEST_URL, "/test")
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
        b.putString(BundleExtractorValues.TEST_URL, "/test")
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking toolbar title
        TestUtils.matchToolbarTitle("Details")

        // check details error
        onView(withText("Errror!")).check(matches(isDisplayed()))
    }
}
