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

package com.github.vase4kin.teamcityapp.runbuild.view

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import okhttp3.ResponseBody
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Spy
import java.util.ArrayList

private const val PARAMETER_NAME = "version"
private const val PARAMETER_VALUE = "1.3.2"

/**
 * Tests for [RunBuildActivity]
 */
@RunWith(AndroidJUnit4::class)
class RunBuildActivityTest {

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
    val activityRule: CustomActivityTestRule<RunBuildActivity> =
        CustomActivityTestRule(RunBuildActivity::class.java)

    @Captor
    lateinit var buildCaptor: ArgumentCaptor<Build>

    @Mock
    lateinit var responseBody: ResponseBody

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().clearAll()
        app.restApiInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeSingleBranchChosenIfBuildTypeHasSingleBranchAvailable() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check the branches autocomplete field has branch as master and it's disabled
        onView(withId(R.id.autocomplete_branches)).check(
            matches(
                allOf(
                    withText("master"),
                    not(isEnabled())
                )
            )
        )
    }

    @Test
    fun testUserCanStartTheBuild() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click())
        // Checking triggered build
        verify(teamCityService).queueBuild(buildCaptor.capture())
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.branchName, `is`("master"))
        assertThat(capturedBuild.agent, `is`(nullValue()))
        assertThat(capturedBuild.isPersonal, `is`(equalTo(false)))
        assertThat(capturedBuild.isQueueAtTop, `is`(equalTo(false)))
        assertThat(capturedBuild.isCleanSources, `is`(equalTo(true)))
        // Checking activity is finishing
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanSeeChooseAgentIfAgentsAvailable() {
        // Prepare mocks
        val agents = ArrayList<Agent>()
        val agent = Agent("agent 1")
        agents.add(agent)
        `when`(teamCityService.listAgents(false, null, null)).thenReturn(
            Single.just(
                Agents(
                    1,
                    agents
                )
            )
        )
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Choose agent
        onView(withId(R.id.chooser_agent)).perform(click())
        onView(withText("agent 1")).perform(click())
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click())
        // Checking that build was triggered with agent
        verify(teamCityService).queueBuild(capture(buildCaptor))
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.agent, `is`(agent))
        // Checking activity is finishing
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanSeeChooseDefaultAgentIfAgentsAvailable() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check hint for selected agent
        onView(withId(R.id.selected_agent)).check(matches(withText(R.string.hint_default_filter_agent)))
    }

    @Test
    fun testUserCanSeeNoAgentsAvailableTextIfNoAgentsAvailable() {
        // Prepare mocks
        `when`(teamCityService.listAgents(false, null, null)).thenReturn(
            Single.error(
                RuntimeException("error")
            )
        )
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check no agents
        onView(withId(R.id.text_no_agents_available)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCleanAllFilesCheckBoxIsCheckedByDefault() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check clean all files is checked by default
        onView(withId(R.id.switcher_clean_all_files)).check(matches(isChecked()))
    }

    @Test
    fun testUserCanStartTheBuildWithDefaultParams() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Check personal
        onView(withId(R.id.switcher_is_personal)).perform(click())
        // Check queue to the top
        onView(withId(R.id.switcher_queueAtTop)).perform(click())
        // Check clean all files
        onView(withId(R.id.switcher_clean_all_files)).perform(click())
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click())
        // Checking triggered build
        verify(teamCityService).queueBuild(capture(buildCaptor))
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.branchName, `is`("master"))
        assertThat(capturedBuild.agent, `is`(nullValue()))
        assertThat(capturedBuild.isPersonal, `is`(equalTo(true)))
        assertThat(capturedBuild.isQueueAtTop, `is`(equalTo(true)))
        assertThat(capturedBuild.isCleanSources, `is`(equalTo(false)))
        // Checking activity is finishing
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanStartTheBuildWithCustomParams() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp())
        // Add new param
        onView(withId(R.id.button_add_parameter)).perform(click())
        // Fill params
        onView(withId(R.id.parameter_name)).perform(typeText(PARAMETER_NAME))
        onView(withId(R.id.parameter_value)).perform(typeText(PARAMETER_VALUE), closeSoftKeyboard())
        // Add param
        onView(withText(R.string.text_add_parameter_button)).perform(click())
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp())
        // Check params on view
        onView(allOf(withId(R.id.parameter_name), isDisplayed())).check(
            matches(
                withText(
                    PARAMETER_NAME
                )
            )
        )
        onView(allOf(withId(R.id.parameter_value), isDisplayed())).check(
            matches(
                withText(
                    PARAMETER_VALUE
                )
            )
        )
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click())
        // Checking triggered build
        verify(teamCityService).queueBuild(capture(buildCaptor))
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.properties.objects.size, `is`(equalTo(1)))
        val capturedProperty = capturedBuild.properties.objects[0]
        assertThat(capturedProperty.name, `is`(equalTo(PARAMETER_NAME)))
        assertThat(capturedProperty.value, `is`(equalTo(PARAMETER_VALUE)))
        // Checking activity is finishing
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanStartTheBuildWithClearAllCustomParams() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp())
        // Add new param
        onView(withId(R.id.button_add_parameter)).perform(click())
        // Fill params
        onView(withId(R.id.parameter_name)).perform(typeText(PARAMETER_NAME))
        onView(withId(R.id.parameter_value)).perform(typeText(PARAMETER_VALUE), closeSoftKeyboard())
        // Add param
        onView(withText(R.string.text_add_parameter_button)).perform(click())
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp())
        // Check params on view
        onView(allOf(withId(R.id.parameter_name), isDisplayed())).check(
            matches(
                withText(
                    PARAMETER_NAME
                )
            )
        )
        onView(allOf(withId(R.id.parameter_value), isDisplayed())).check(
            matches(
                withText(
                    PARAMETER_VALUE
                )
            )
        )
        // Clear all params
        onView(withId(R.id.button_clear_parameters)).perform(click())
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click())
        // Checking triggered build
        verify(teamCityService).queueBuild(capture(buildCaptor))
        val capturedBuild = buildCaptor.value
        assertThat(capturedBuild.properties, `is`(nullValue()))
        // Checking activity is finishing
        assertThat(activityRule.activity.isFinishing, `is`(true))
    }

    @Test
    fun testUserCanNotCreateEmptyBuildParamWithEmptyName() {
        // Prepare intent
        val intent = Intent()
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href")
        // Starting the activity
        activityRule.launchActivity(intent)
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp())
        // Add new param
        onView(withId(R.id.button_add_parameter)).perform(click())
        // Fill params
        onView(withId(R.id.parameter_name)).perform(typeText(""))
        onView(withId(R.id.parameter_value)).perform(typeText(PARAMETER_VALUE), closeSoftKeyboard())
        // Add param
        onView(withText(R.string.text_add_parameter_button)).perform(click())
        // Check error
        onView(withText(R.string.text_error_parameter_name)).check(matches(isDisplayed()))
    }
}
