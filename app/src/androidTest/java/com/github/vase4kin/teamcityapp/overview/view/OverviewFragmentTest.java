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

package com.github.vase4kin.teamcityapp.overview.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.CanceledInfo;
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered;
import com.github.vase4kin.teamcityapp.buildlist.api.User;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.buildlist.api.Triggered.TRIGGER_TYPE_BUILD_TYPE;
import static com.github.vase4kin.teamcityapp.buildlist.api.Triggered.TRIGGER_TYPE_RESTARTED;
import static com.github.vase4kin.teamcityapp.buildlist.api.Triggered.TRIGGER_TYPE_USER;
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
 * Tests for {@link OverviewFragment}
 */
@RunWith(AndroidJUnit4.class)
public class OverviewFragmentTest {

    private final static String CANCELED_TIME_STAMP = "20161223T151154+0300";
    private static final String USER_NAME = "john.117";
    private static final String NAME = "John one one seven";
    private static final String BUILD_TYPE_NAME = "name";

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
    public CustomIntentsTestRule<BuildDetailsActivity> mActivityRule = new CustomIntentsTestRule<>(BuildDetailsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Spy
    private Build mBuild = Mocks.successBuild();

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.disableOnboarding();
    }

    @Test
    public void testUserCanSeeSuccessBuildDetails() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking toolbar title
        matchToolbarTitle("#2459 (21 Jun 16 23:00)");
        // List has item with header
        onView(withId(R.id.overview_recycler_view)).check(hasItemsCount(7));
        // Checking Result
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(0, R.id.itemHeader)).check(matches(withText("Result")));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(0, R.id.itemTitle)).check(matches(withText("Success")));
        // Checking Time
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemHeader)).check(matches(withText("Time")));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemTitle)).check(matches(withText("21 Jun 16 23:00 - 23:30 (30m:)")));
        // Checking Branch
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(2, R.id.itemHeader)).check(matches(withText("Branch")));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(2, R.id.itemTitle)).check(matches(withText("refs/heads/master")));
        // Checking Agent
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(3, R.id.itemHeader)).check(matches(withText("Agent")));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(3, R.id.itemTitle)).check(matches(withText("agent-love")));

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking Triggered by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText("Triggered by")));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText("code-lover")));
    }

    @Test
    public void testUserCanSeeFailureMessageIfSmthBadHappens() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.<Build>error(new RuntimeException("Fake error happened!")));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Checking error
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanCopyElementValueFromTheList() throws Exception {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Click on Result view
        onView(withRecyclerView(R.id.overview_recycler_view).atPosition(0)).perform(click());

        // Clicking on copy
        onView(withText(R.string.build_element_copy)).perform(click());

        // Checking toast message
        onView(withText(R.string.build_element_copy_text))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeCanceledInfoAsUserRealName() throws Exception {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        mBuild.setCanceledInfo(new CanceledInfo(CANCELED_TIME_STAMP, new User("user.name", "User name")));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking Canceled by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemHeader)).check(matches(withText(R.string.build_canceled_by_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemTitle)).check(matches(withText("User name")));
    }

    @Test
    public void testUserCanSeeCanceledInfoAsUserName() throws Exception {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        mBuild.setCanceledInfo(new CanceledInfo(CANCELED_TIME_STAMP, new User("user.name", null)));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking Canceled by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemHeader)).check(matches(withText(R.string.build_canceled_by_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(1, R.id.itemTitle)).check(matches(withText("user.name")));
    }

    @Test
    public void testUserCanSeeCanceledInfoAsTimeStampOfCancellation() throws Exception {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        mBuild.setCanceledInfo(new CanceledInfo(CANCELED_TIME_STAMP, new User("user.name", null)));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking time stamp
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(2, R.id.itemHeader)).check(matches(withText(R.string.build_cancellation_time_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(2, R.id.itemTitle)).check(matches(withText("23 Dec 16 15:11")));
    }

    @Test
    public void testUserCanSeeUserWhatUserTriggeredBuildIfUserHasName() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_USER, null, new User(USER_NAME, NAME));
        mBuild.setTriggered(triggered);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText(R.string.build_triggered_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText(NAME)));
    }

    @Test
    public void testUserCanSeeUserWhatUserTriggeredBuildIfUserHasOnLyName() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_USER, null, new User(USER_NAME, null));
        mBuild.setTriggered(triggered);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText(R.string.build_triggered_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText(USER_NAME)));
    }

    @Test
    public void testUserCanSeeWhatUserRestartedBuildIfUserHasName() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_RESTARTED, null, new User(USER_NAME, NAME));
        mBuild.setTriggered(triggered);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText(R.string.build_restarted_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText(NAME)));
    }

    @Test
    public void testUserCanSeeWhatUserRestartedBuildIfUserHasOnlyUserName() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_RESTARTED, null, new User(USER_NAME, null));
        mBuild.setTriggered(triggered);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText(R.string.build_restarted_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText(USER_NAME)));
    }

    @Test
    public void testUserCanSeeWhatConfigurationTriggeredBuildIfConfigurationIsDeleted() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_BUILD_TYPE, null, null);
        mBuild.setTriggered(triggered);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText(R.string.build_triggered_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText(R.string.triggered_deleted_configuration_text)));
    }

    @Test
    public void testUserCanSeeWhatUserRestartedBuildIfUserWasDeleted() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_RESTARTED, null, null);
        mBuild.setTriggered(triggered);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText(R.string.build_restarted_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText(R.string.triggered_deleted_user_text)));
    }

    @Test
    public void testUserCanSeeWhatUserTriggeredBuildIfUserWasDeleted() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_USER, null, null);
        mBuild.setTriggered(triggered);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(4));

        // Checking restarted by
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemHeader)).check(matches(withText(R.string.build_triggered_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(4, R.id.itemTitle)).check(matches(withText(R.string.triggered_deleted_user_text)));
    }

    @Test
    public void testUserCanSeePersonalInfoIfBuildIsPersonal() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        Triggered triggered = new Triggered(TRIGGER_TYPE_USER, null, new User(USER_NAME, null));
        mBuild.setTriggered(triggered);
        mBuild.setPersonal(true);

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        Build successBuild = Mocks.successBuild();
        successBuild.setPersonal(true);
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(5));

        // Checking Personal of
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(5, R.id.itemHeader)).check(matches(withText(R.string.build_personal_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(5, R.id.itemTitle)).check(matches(withText(USER_NAME)));
    }

    @Test
    public void testUserCanViewBuildListFilteredByBranch() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Click on Branch card
        onView(withRecyclerView(R.id.overview_recycler_view).atPosition(2)).perform(click());

        // Clicking on show all builds
        onView(withText(R.string.build_element_show_all_builds_built_branch)).perform(click());

        // Check builds list activity is opened
        intended(hasComponent(BuildListActivity.class.getName()));

        // Check buid list filter is applied
        verify(mTeamCityService).listBuilds(eq("Checkstyle_IdeaInspectionsPullRequest"), eq("state:any,canceled:any,failedToStart:any,branch:name:refs/heads/master,personal:false,pinned:false,count:10"));
    }

    @Test
    public void testUserCanViewBuildTypeFromCard() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(6));

        // Checking configuration  details
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(5, R.id.itemHeader)).check(matches(withText(R.string.build_type_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(5, R.id.itemTitle)).check(matches(withText("build type name")));

        // Click on that card
        onView(withRecyclerView(R.id.overview_recycler_view).atPosition(5)).perform(click());

        // Clicking on show build type
        onView(withText(R.string.build_element_open_build_type)).perform(click());

        intended(allOf(
                hasComponent(BuildListActivity.class.getName()),
                hasExtras(allOf(
                        hasEntry(equalTo(BundleExtractorValues.BUILD_LIST_FILTER), equalTo(null)),
                        hasEntry(equalTo(BundleExtractorValues.ID), equalTo("Checkstyle_IdeaInspectionsPullRequest")),
                        hasEntry(equalTo(BundleExtractorValues.NAME), equalTo("build type name"))))));

        // Check buid list filter is applied
        verify(mTeamCityService).listBuilds(eq("Checkstyle_IdeaInspectionsPullRequest"), eq("state:any,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:10"));
    }

    @Test
    public void testUserCanViewProjectFromCard() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        //Scrolling to last item to make it visible
        onView(withId(R.id.overview_recycler_view)).perform(scrollToPosition(6));
        onView(withId(android.R.id.content)).perform(swipeUp());

        // Checking configuration  details
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(6, R.id.itemHeader)).check(matches(withText(R.string.build_project_by_section_text)));
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(6, R.id.itemTitle)).check(matches(withText("project name")));

        // Click on that card
        onView(withRecyclerView(R.id.overview_recycler_view).atPositionOnView(6, R.id.itemHeader)).perform(click());

        // Clicking on show project
        onView(withText(R.string.build_element_open_project)).perform(click());

        intended(allOf(
                hasComponent(NavigationActivity.class.getName()),
                hasExtras(allOf(
                        hasEntry(equalTo(BundleExtractorValues.ID), equalTo("projectId")),
                        hasEntry(equalTo(BundleExtractorValues.NAME), equalTo("project name"))))));

        // Check buid list filter is applied
        verify(mTeamCityService).listBuildTypes(eq("projectId"));
    }

}