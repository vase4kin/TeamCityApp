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

package com.github.vase4kin.teamcityapp.filter_builds.view;

import android.content.Intent;

import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.runbuild.api.Branch;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorKt.EXTRA_BUILD_TYPE_ID;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FilterBuildsActivity}
 */
@RunWith(AndroidJUnit4.class)
public class FilterBuildsBranchesAutoCompleteTest {

    private static final String TYPE_ID = "TypeId";

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
    public CustomActivityTestRule<FilterBuildsActivity> mActivityRule = new CustomActivityTestRule<>(FilterBuildsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getRestApiInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);
    }

    @Test
    public void testUserCanNoBranchesAvailableForFilterIfBuildTypeHasSingleBranchAvailable() throws Exception {
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, TYPE_ID);
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Check the branches autocomplete field is not visible
        onView(withId(R.id.autocomplete_branches)).check(matches(not(isDisplayed())));
        // Check no branches available for filter is displayed
        onView(withId(R.id.text_no_branches_available_to_filter)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanNoBranchesAvailableForFilterIfBuildTypeHasEmptyBranchesList() throws Exception {
        // Prepare mocks
        when(mTeamCityService.listBranches(anyString())).thenReturn(Single.just(new Branches(new ArrayList<Branch>())));
        // Prepare intent
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUILD_TYPE_ID, TYPE_ID);
        // Starting the activity
        mActivityRule.launchActivity(intent);
        // Check the branches autocomplete field is not visible
        onView(withId(R.id.autocomplete_branches)).check(matches(not(isDisplayed())));
        // Check no branches available for filter is displayed
        onView(withId(R.id.text_no_branches_available_to_filter)).check(matches(isDisplayed()));
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
        intent.putExtra(EXTRA_BUILD_TYPE_ID, TYPE_ID);
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

}