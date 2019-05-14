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

import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;
import com.github.vase4kin.teamcityapp.runbuild.api.Branch;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FilterBuildsActivity}
 */
@RunWith(AndroidJUnit4.class)
public class FilterBuildsActivityTest {

    @Rule
    public DaggerMockRule<RestApiComponent> mDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(restApiComponent -> {
                TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                app.setRestApiInjector(restApiComponent);
            });

    @Rule
    public CustomIntentsTestRule<RootProjectsActivity> mActivityRule = new CustomIntentsTestRule<>(RootProjectsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.disableOnboarding();
    }

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getRestApiInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Single.just(new Builds(0, Collections.<Build>emptyList())));
    }

    @Test
    public void testUserCanFilterBuildsWithDefaultFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsByPinned() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on pin switcher
        onView(withId(R.id.switcher_is_pinned)).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:true,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsByPersonal() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on pin switcher
        onView(withId(R.id.switcher_is_personal)).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("state:any,canceled:any,failedToStart:any,branch:default:any,personal:true,pinned:false,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsByBranch() throws Exception {
        // Prepare mocks
        List<Branch> branches = new ArrayList<>();
        branches.add(new Branch("dev1"));
        branches.add(new Branch("dev2"));
        when(mTeamCityService.listBranches(anyString())).thenReturn(Single.just(new Branches(branches)));

        // Starting the activity
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Choose branch from autocomplete and verify it is appeared
        onView(withId(R.id.autocomplete_branches))
                .perform(typeText("dev"));
        onData(allOf(is(instanceOf(String.class)), is("dev1")))
                .inRoot(RootMatchers.withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .perform(click());
        onView(withText("dev1")).perform(click(), closeSoftKeyboard());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("state:any,canceled:any,failedToStart:any,branch:name:dev1,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testPinnedSwitchIsGoneWhenQueuedFilterIsChosen() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Check switchers are shown
        onView(withId(R.id.switcher_is_pinned)).check(matches(isDisplayed()));
        onView(withId(R.id.switcher_is_personal)).check(matches(isDisplayed()));

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by queued
        onView(withText("Queued")).perform(click());

        // Check switchers
        onView(withId(R.id.switcher_is_pinned)).check(matches(not(isDisplayed())));
        onView(withId(R.id.divider_switcher_is_pinned)).check(matches(not(isDisplayed())));
        onView(withId(R.id.switcher_is_personal)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanFilterBuildsWithSuccessFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by success
        onView(withText("Success")).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("status:SUCCESS,branch:default:any,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsWithFailedFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by success
        onView(withText("Failed")).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("status:FAILURE,branch:default:any,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsWithFailedServerErrorFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by success
        onView(withText("Failed due server error")).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("status:ERROR,branch:default:any,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsWithCancelledFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by success
        onView(withText("Cancelled")).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("canceled:true,branch:default:any,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsWithFailedToStartFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by success
        onView(withText("Failed to start")).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("failedToStart:true,branch:default:any,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testUserCanFilterBuildsWithRunningFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by success
        onView(withText("Running")).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("running:true,branch:default:any,personal:false,pinned:false"));
    }

    @Test
    public void testUserCanFilterBuildsWithQueuedFilter() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Click on filter chooser
        onView(withId(R.id.chooser_filter)).perform(click());

        // Filter by success
        onView(withText("Queued")).perform(click());

        // Click on filter fab
        onView(withId(R.id.fab_filter)).perform(click());

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(anyString(), eq("state:queued,branch:default:any,personal:false,pinned:any"));
    }

}