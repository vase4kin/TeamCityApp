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
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.runbuild.api.Branch;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor.CODE_FORBIDDEN;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RunBuildActivity}
 */
@RunWith(AndroidJUnit4.class)
public class RunBuildActivityTest {

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

    @Mock
    ResponseBody mResponseBody;

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getRestApiInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL);
    }

    @Test
    public void testUserCanSeeSingleBranchChosenIfBuildTypeHasSingleBranchAvailable() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(RunBuildInteractor.EXTRA_BUILD_TYPE_ID, "href");
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
        when(mTeamCityService.listBranches(anyString())).thenReturn(Observable.just(new Branches(branches)));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(RunBuildInteractor.EXTRA_BUILD_TYPE_ID, "href");
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
        intent.putExtra(RunBuildInteractor.EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        assertThat(mActivityRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testUserCanSeeErrorSnackBarIfServerReturnsAnError() throws Exception {
        // Prepare mocks
        when(mTeamCityService.queueBuild(any(Build.class))).thenReturn(Observable.<Build>error(new RuntimeException()));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(RunBuildInteractor.EXTRA_BUILD_TYPE_ID, "href");
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
        when(mTeamCityService.queueBuild(any(Build.class))).thenReturn(Observable.<Build>error(httpException));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(RunBuildInteractor.EXTRA_BUILD_TYPE_ID, "href");
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Starting the build
        onView(withId(R.id.fab_queue_build)).perform(click());
        // Checking the error snackbar text
        onView(withText(R.string.error_forbidden_error)).check(matches(isDisplayed()));
    }
}