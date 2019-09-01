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

package com.github.vase4kin.teamcityapp.changes.view

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.changes.api.Changes
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

private const val NAME = "name"

/**
 * Tests for [ChangesFragment]
 */
@RunWith(AndroidJUnit4::class)
class ChangesFragmentTest {

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
    fun testUserCanSeeBuildChanges() {
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

        // Checking changes tab title
        onView(withText("Changes (1)"))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.changes_recycler_view)).check(hasItemsCount(1))
        // Checking User name
        onView(
            withRecyclerView(R.id.changes_recycler_view).atPositionOnView(
                0,
                R.id.userName
            )
        ).check(matches(withText("john-117")))
        // Checking comment
        onView(
            withRecyclerView(R.id.changes_recycler_view).atPositionOnView(
                0,
                R.id.itemTitle
            )
        ).check(matches(withText("Do you believe?")))
        // Checking commit number
        onView(
            withRecyclerView(R.id.changes_recycler_view).atPositionOnView(
                0,
                R.id.itemSubTitle
            )
        ).check(matches(withText("21312fsd1321")))
        // Checking data
        onView(withRecyclerView(R.id.changes_recycler_view).atPositionOnView(0, R.id.date)).check(
            matches(withText("30 Jul 16 00:36"))
        )

        // Clicking on change
        onView(withRecyclerView(R.id.changes_recycler_view).atPosition(0)).perform(click())

        // Сhecking dialog content
        onView(withId(R.id.md_content)).check(matches(withText("john-117 on 30 Jul 16 00:36")))
        // Сheck files
        onView(withText("filename!")).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeChangesFailureMessageIfSmthBadHappens() {
        // Prepare mocks
        `when`(teamCityService.listChanges(anyString())).thenReturn(Single.error(RuntimeException("Fake error happened!")))

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

        // Checking changes tab title
        onView(withText("Changes (0)"))
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking error
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeEmptyChangesMessageIfNoChangesAreAvailable() {
        // Prepare mocks
        `when`(teamCityService.listChanges(anyString())).thenReturn(
            Single.just(
                Changes(
                    emptyList(),
                    0
                )
            )
        )

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

        // Checking changes tab title
        onView(withText("Changes (0)"))
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking message
        onView(withText(R.string.empty_list_message_changes)).check(matches(isDisplayed()))
    }
}
