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

package com.github.vase4kin.teamcityapp.restart_build

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.any
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.runbuild.interactor.CODE_FORBIDDEN
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Matchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Spy
import retrofit2.HttpException
import retrofit2.Response
import teamcityapp.features.properties.repository.models.Properties

private const val BRANCH_NAME = "refs/heads/dev"
private const val PROPERTY_NAME = "property"
private const val PROPERTY_VALUE = "true"
private const val BUILD_TYPE_NAME = "name"

/**
 * Tests for restart build feature
 */
@RunWith(AndroidJUnit4::class)
class RestartBuildTest {

    @Rule
    @JvmField
    val daggerRule: DaggerMockRule<RestApiComponent> =
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
    val activityRule: CustomIntentsTestRule<BuildDetailsActivity> =
        CustomIntentsTestRule(BuildDetailsActivity::class.java)

    @Captor
    lateinit var buildArgumentCaptor: ArgumentCaptor<Build>

    @Mock
    lateinit var teamCityService: TeamCityService

    @Spy
    val build = Mocks.failedBuild()

    val build2 = Mocks.queuedBuild2()

    @Mock
    lateinit var responseBody: ResponseBody

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
        `when`(
            teamCityService.listAgents(
                any(),
                any(),
                any()
            )
        ).thenReturn(Single.just(Mocks.connectedAgents()))
        `when`(
            teamCityService.listArtifacts(
                any(),
                any()
            )
        ).thenReturn(Single.just(Mocks.artifacts()))
        `when`(teamCityService.listTestOccurrences(any())).thenReturn(Single.just(Mocks.ignoredTests()))
        `when`(teamCityService.listChanges(any())).thenReturn(Single.just(Mocks.changes()))
    }

    @Test
    fun testUserCanRestartBuildWithTheSameParameters() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
            .thenReturn(Single.just(build2))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.just(
                Mocks.queuedBuild2()
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        val buildToRestart = Mocks.failedBuild()
        val property = Properties.Property(PROPERTY_NAME, PROPERTY_VALUE)
        buildToRestart.branchName = BRANCH_NAME
        buildToRestart.properties =
            Properties(
                listOf(property)
            )
        b.putSerializable(BundleExtractorValues.BUILD, buildToRestart)
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click())

        // Check dialog text is displayed
        onView(withText(R.string.text_restart_the_build)).check(matches(isDisplayed()))

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_restarted)).check(matches(isDisplayed()))

        // Checking passed params
        verify(teamCityService).queueBuild(capture(buildArgumentCaptor))
        val capturedBuild = buildArgumentCaptor.value
        assertThat(capturedBuild.branchName, `is`(equalTo(BRANCH_NAME)))
        assertThat(capturedBuild.properties!!.properties.size, `is`(equalTo(1)))
        val capturedProperty = capturedBuild.properties!!.properties[0]
        assertThat(capturedProperty.name, `is`(equalTo(PROPERTY_NAME)))
        assertThat(capturedProperty.value, `is`(equalTo(PROPERTY_VALUE)))

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click())

        // Check build is opened
        intended(
            allOf(
                hasComponent(BuildDetailsActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        CoreMatchers.equalTo(BundleExtractorValues.BUILD),
                        CoreMatchers.equalTo(build2)
                    )
                )
            )
        )

        // Checking Result was changed
        onView(
            withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.subTitle)
        )
            .check(matches(withText("This build will not start because there are no compatible agents which can run it")))
    }

    @Test
    fun testUserCanRestartBuildWithTheSameParametersButFailedToOpenItThen() {
        // Prepare mocks
        val httpException = HttpException(Response.error<Build>(500, responseBody))
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
            .thenReturn(Single.error(httpException))
            .thenReturn(Single.error(httpException))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.just(
                Mocks.queuedBuild2()
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        val buildToRestart = Mocks.failedBuild()
        val property = Properties.Property(PROPERTY_NAME, PROPERTY_VALUE)
        buildToRestart.branchName = BRANCH_NAME
        buildToRestart.properties =
            Properties(
                listOf(property)
            )
        b.putSerializable(BundleExtractorValues.BUILD, buildToRestart)
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click())

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_restarted)).check(matches(isDisplayed()))

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click())

        // Check error snack bar
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()))

        // Click on retry button
        onView(withText(R.string.download_artifact_retry_snack_bar_retry_button)).perform(click())

        // Check error snack bar
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeForbiddenErrorWhenRestartingBuild() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val httpException = HttpException(Response.error<Build>(CODE_FORBIDDEN, responseBody))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.error(
                httpException
            )
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

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click())

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.error_restart_build_forbidden_error)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeServerErrorWhenRestartingBuild() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val httpException = HttpException(Response.error<Build>(500, responseBody))
        `when`(teamCityService.queueBuild(any())).thenReturn(
            Single.error(
                httpException
            )
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

        // Opening context menu
        openContextualActionModeOverflowMenu()

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click())

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click())

        // Check snack bar is displayed
        onView(withText(R.string.error_base_restart_build_error)).check(matches(isDisplayed()))
    }
}
