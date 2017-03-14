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

package com.github.vase4kin.teamcityapp.buildlist.view;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter;
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.util.Collections;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BuildListActivity}
 */
@RunWith(AndroidJUnit4.class)
public class BuildListActivityTest {

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
    public CustomIntentsTestRule<RootProjectsActivity> mActivityRule = new CustomIntentsTestRule<>(RootProjectsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getRestApiInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL);
    }

    @Test
    public void testUserCanSeeToolbarText() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("build type");
    }

    @Test
    public void testUserCanSeeSuccessFullyLoadedBuilds() throws Exception {
        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // List has item with header
        onView(withId(R.id.build_recycler_view)).check(hasItemsCount(5));
        // Checking header 1
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("22 June")));
        // Checking adapter item 1
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Running tests")));
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.itemSubTitle))
                .check(matches(withText("refs/heads/master")));
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.buildNumber))
                .check(matches(withText("#2458")));
        // Checking header 2
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(2, R.id.section_text))
                .check(matches(withText("21 June")));
        // Checking adapter item 2
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(3, R.id.itemTitle))
                .check(matches(withText("Success")));
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(3, R.id.itemSubTitle))
                .check(matches(withText("refs/heads/master")));
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(3, R.id.buildNumber))
                .check(matches(withText("#2459")));
        // Checking adapter item 3
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(4, R.id.itemTitle))
                .check(matches(withText("Error with smth")));
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(4, R.id.buildNumber))
                .check(matches(withText("#2460")));
    }

    @Test
    public void testUserCanSeeFailureMessageIfSmthBadHappensInBuildListLoading() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.<Builds>error(new RuntimeException("smth bad happend!")));

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Check error message
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeLoadEmptyListMessageIfBuildListIsEmpty() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Check the list is empty
        onView(withId(android.R.id.empty)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_builds)));
    }

    @Test
    public void testUserCanOpenRunBuild() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
                .perform(click());

        // Check run build activity is opened
        intended(allOf(
                hasComponent(RunBuildActivity.class.getName()),
                hasExtras(hasEntry(equalTo(RunBuildInteractor.EXTRA_BUILD_TYPE_ID), equalTo("build_type_id")))));
    }

    @Test
    public void testUserCanSeeSnackBarIfBuildIsAddedToQueue() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        // Preparing stubbing intent
        Intent resultData = new Intent();
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity.class.getName())).respondWith(result);

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
                .perform(click());

        // Check snack bar text
        onView(withText(R.string.text_build_is_run)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeBuildListIsRefreshedIfBuildIsAddedToQueue() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString()))
                .thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())))
                .thenCallRealMethod();

        // Preparing stubbing intent
        Intent resultData = new Intent();
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Check the list is empty
        onView(withId(android.R.id.empty)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_builds)));

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity.class.getName())).respondWith(result);

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
                .perform(click());

        // Check new items appeared
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Running tests")));
    }

    @Test
    public void testUserCanOpenRecentlyQueuedBuildFromSnackBarIfNoErrors() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        // Preparing stubbing intent
        Intent resultData = new Intent();
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity.class.getName())).respondWith(result);

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
                .perform(click());

        // Mock build call
        Build build = Mocks.runningBuild();
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(build));

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click());

        // Check build is opened
        intended(allOf(
                hasComponent(BuildDetailsActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.BUILD), equalTo(build)))));
    }

    @Test
    public void testUserCanSeeErrorSnackbarWhenOpenRecentlyQueuedBuildIfThereAreErrors() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        // Preparing stubbing intent
        Intent resultData = new Intent();
        resultData.putExtra(RunBuildRouter.EXTRA_HREF, "href");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Set up result stubbing
        intending(hasComponent(RunBuildActivity.class.getName())).respondWith(result);

        // Pressing run build fab
        onView(withId(R.id.floating_action_button))
                .perform(click());

        // Mock build call
        when(mTeamCityService.build(anyString())).thenReturn(Observable.<Build>error(new RuntimeException()));

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click());

        // Check error snack bar
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()));

        // Click on retry button
        onView(withText(R.string.download_artifact_retry_snack_bar_retry_button)).perform(click());

        // Check error snack bar again
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanOpenFilterBuilds() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Check filter builds activity is opened
        intended(allOf(
                hasComponent(FilterBuildsActivity.class.getName()),
                hasExtras(hasEntry(equalTo(RunBuildInteractor.EXTRA_BUILD_TYPE_ID), equalTo("build_type_id")))));
    }

    @Test
    public void testUserCanSeeSnackBarIfBuildFiltersHaveBeenApplied() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        // Preparing stubbing intent
        Intent resultData = new Intent();
        BuildListFilter filter = new BuildListFilterImpl();
        filter.setFilter(FilterBuildsView.FILTER_CANCELLED);
        filter.setBranch("branch");
        filter.setPersonal(true);
        filter.setPinned(true);
        resultData.putExtra(FilterBuildsRouter.EXTRA_FILTER, filter);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        mActivityRule.launchActivity(null);

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Set up result stubbing
        intending(hasComponent(FilterBuildsActivity.class.getName())).respondWith(result);

        // Pressing filter builds toolbar item
        onView(withId(R.id.filter_builds))
                .perform(click());

        // Check snack bar text
        onView(withText(R.string.text_filters_applied)).check(matches(isDisplayed()));

        // Check data was loaded with new filter
        verify(mTeamCityService).listBuilds(eq("build_type_id"), eq("canceled:true,branch:name:branch,personal:true,pinned:true,count:10"));
    }
}