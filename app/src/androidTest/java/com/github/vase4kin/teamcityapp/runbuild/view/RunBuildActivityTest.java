/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.runbuild.view;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.runbuild.api.Branch;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorKt.CODE_FORBIDDEN;
import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorKt.EXTRA_BUILD_TYPE_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RunBuildActivity}
 */
@RunWith(AndroidJUnit4.class)
public class RunBuildActivityTest {

    private static final String PARAMETER_NAME = "version";
    private static final String PARAMETER_VALUE = "1.3.2";

    @Rule
    public DaggerMockRule<RestApiComponent> mDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<RestApiComponent>() {
                @Override
                public void setComponent(RestApiComponent restApiComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setRestApiInjector(restApiComponent);
                }
            });

    @Rule
    public CustomActivityTestRule<RunBuildActivity> mActivityRule = new CustomActivityTestRule<>(RunBuildActivity.class);

    @Captor
    private ArgumentCaptor<Build> mBuildCaptor;

    @Mock
    ResponseBody mResponseBody;

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getRestApiInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);
    }

    @Test
    public void testUserCanSeeSingleBranchChosenIfBuildTypeHasSingleBranchAvailable() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Check the branches autocomplete field has branch as master and it's disabled
        onView(withId(R.id.autocomplete_branches)).check(matches(allOf(withText("master"), not(isEnabled()))));
    }

    @Test
    public void testUserCanSeeMultipleBranchesIfBuildTypeHasMultipleAvailable() throws Exception {
        // Prepare mocks
        List<Branch> branches = new ArrayList<>();
        branches.add(new Branch("dev1"));
        branches.add(new Branch("dev2"));
        when(mTeamCityService.listBranches(anyString())).thenReturn(Single.just(new Branches(branches)));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Choose branch from autocomplete and verify it is appeared
        onView(withId(R.id.autocomplete_branches))
                .perform(typeText("dev"));
        onData(allOf(is(instanceOf(String.class)), is("dev1")))
                .inRoot(RootMatchers.withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .perform(click());

        onView(withText("dev1")).perform(click());
        onView(withId(R.id.autocomplete_branches)).check(matches(allOf(withText("dev1"), isEnabled())));
    }

    @Test
    public void testUserCanStartTheBuild() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking triggered build
        verify(mTeamCityService).queueBuild(mBuildCaptor.capture());
        Build capturedBuild = mBuildCaptor.getValue();
        assertThat(capturedBuild.getBranchName(), is("master"));
        assertThat(capturedBuild.getAgent(), is(nullValue()));
        assertThat(capturedBuild.isPersonal(), is(equalTo(false)));
        assertThat(capturedBuild.isQueueAtTop(), is(equalTo(false)));
        assertThat(capturedBuild.isCleanSources(), is(equalTo(true)));
        // Checking activity is finishing
        assertThat(mActivityRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testUserCanSeeErrorSnackBarIfServerReturnsAnError() throws Exception {
        // Prepare mocks
        when(mTeamCityService.queueBuild(any(Build.class))).thenReturn(Single.<Build>error(new RuntimeException()));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking the error snackbar text
        onView(withText(R.string.error_base_error)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeErrorForbiddenSnackBarIfServerReturnsAnError() throws Exception {
        // Prepare mocks
        HttpException httpException = new HttpException(Response.<Build>error(CODE_FORBIDDEN, mResponseBody));
        when(mTeamCityService.queueBuild(any(Build.class))).thenReturn(Single.<Build>error(httpException));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking the error snackbar text
        onView(withText(R.string.error_forbidden_error)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeChooseAgentIfAgentsAvailable() throws Exception {
        // Prepare mocks
        List<Agent> agents = new ArrayList<>();
        Agent agent = new Agent("agent 1");
        agents.add(agent);
        when(mTeamCityService.listAgents(false, null, null)).thenReturn(Single.just(new Agents(1, agents)));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Choose agent
        onView(withId(R.id.chooser_agent)).perform(click());
        onView(withText("agent 1")).perform(click());
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking that build was triggered with agent
        verify(mTeamCityService).queueBuild(mBuildCaptor.capture());
        Build capturedBuild = mBuildCaptor.getValue();
        assertThat(capturedBuild.getAgent(), is(agent));
        // Checking activity is finishing
        assertThat(mActivityRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testUserCanSeeChooseDefaultAgentIfAgentsAvailable() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Check hint for selected agent
        onView(withId(R.id.selected_agent)).check(matches(withText(R.string.hint_default_filter_agent)));
    }

    @Test
    public void testUserCanSeeNoAgentsAvailableTextIfNoAgentsAvailable() throws Exception {
        // Prepare mocks
        when(mTeamCityService.listAgents(false, null, null)).thenReturn(Single.<Agents>error(new RuntimeException()));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Check no agents
        onView(withId(R.id.text_no_agents_available)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCleanAllFilesCheckBoxIsCheckedByDefault() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Check clean all files is checked by default
        onView(withId(R.id.switcher_clean_all_files)).check(matches(isChecked()));
    }

    @Test
    public void testUserCanStartTheBuildWithDefaultParams() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Check personal
        onView(withId(R.id.switcher_is_personal)).perform(click());
        // Check queue to the top
        onView(withId(R.id.switcher_queueAtTop)).perform(click());
        // Check clean all files
        onView(withId(R.id.switcher_clean_all_files)).perform(click());
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking triggered build
        verify(mTeamCityService).queueBuild(mBuildCaptor.capture());
        Build capturedBuild = mBuildCaptor.getValue();
        assertThat(capturedBuild.getBranchName(), is("master"));
        assertThat(capturedBuild.getAgent(), is(nullValue()));
        assertThat(capturedBuild.isPersonal(), is(equalTo(true)));
        assertThat(capturedBuild.isQueueAtTop(), is(equalTo(true)));
        assertThat(capturedBuild.isCleanSources(), is(equalTo(false)));
        // Checking activity is finishing
        assertThat(mActivityRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testUserCanStartTheBuildWithCustomParams() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp());
        // Add new param
        onView(withId(R.id.button_add_parameter)).perform(click());
        // Fill params
        onView(withId(R.id.parameter_name)).perform(typeText(PARAMETER_NAME));
        onView(withId(R.id.parameter_value)).perform(typeText(PARAMETER_VALUE), closeSoftKeyboard());
        // Add param
        onView(withText(R.string.text_add_parameter_button)).perform(click());
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp());
        // Check params on view
        onView(allOf(withId(R.id.parameter_name), isDisplayed())).check(matches(withText(PARAMETER_NAME)));
        onView(allOf(withId(R.id.parameter_value), isDisplayed())).check(matches(withText(PARAMETER_VALUE)));
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking triggered build
        verify(mTeamCityService).queueBuild(mBuildCaptor.capture());
        Build capturedBuild = mBuildCaptor.getValue();
        assertThat(capturedBuild.getProperties().getObjects().size(), is(equalTo(1)));
        Properties.Property capturedProperty = capturedBuild.getProperties().getObjects().get(0);
        assertThat(capturedProperty.getName(), is(equalTo(PARAMETER_NAME)));
        assertThat(capturedProperty.getValue(), is(equalTo(PARAMETER_VALUE)));
        // Checking activity is finishing
        assertThat(mActivityRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testUserCanStartTheBuildWithClearAllCustomParams() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp());
        // Add new param
        onView(withId(R.id.button_add_parameter)).perform(click());
        // Fill params
        onView(withId(R.id.parameter_name)).perform(typeText(PARAMETER_NAME));
        onView(withId(R.id.parameter_value)).perform(typeText(PARAMETER_VALUE), closeSoftKeyboard());
        // Add param
        onView(withText(R.string.text_add_parameter_button)).perform(click());
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp());
        // Check params on view
        onView(allOf(withId(R.id.parameter_name), isDisplayed())).check(matches(withText(PARAMETER_NAME)));
        onView(allOf(withId(R.id.parameter_value), isDisplayed())).check(matches(withText(PARAMETER_VALUE)));
        // Clear all params
        onView(withId(R.id.button_clear_parameters)).perform(click());
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking triggered build
        verify(mTeamCityService).queueBuild(mBuildCaptor.capture());
        Build capturedBuild = mBuildCaptor.getValue();
        assertThat(capturedBuild.getProperties(), is(nullValue()));
        // Checking activity is finishing
        assertThat(mActivityRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testUserCanNotCreateEmptyBuildParamWithEmptyName() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Scroll to
        onView(withId(android.R.id.content)).perform(swipeUp());
        // Add new param
        onView(withId(R.id.button_add_parameter)).perform(click());
        // Fill params
        onView(withId(R.id.parameter_name)).perform(typeText(""));
        onView(withId(R.id.parameter_value)).perform(typeText(PARAMETER_VALUE), closeSoftKeyboard());
        // Add param
        onView(withText(R.string.text_add_parameter_button)).perform(click());
        // Check error
        onView(withText(R.string.text_error_parameter_name)).check(matches(isDisplayed()));
    }
}