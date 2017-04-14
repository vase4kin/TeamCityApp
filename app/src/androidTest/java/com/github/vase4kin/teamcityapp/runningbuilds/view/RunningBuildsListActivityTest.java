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

package com.github.vase4kin.teamcityapp.runningbuilds.view;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.util.Collections;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RunningBuildsListActivity}
 */
@RunWith(AndroidJUnit4.class)
public class RunningBuildsListActivityTest {

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
    public CustomActivityTestRule<RunningBuildsListActivity> mActivityRule = new CustomActivityTestRule<>(RunningBuildsListActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    /**
     * java.lang.RuntimeException: Could not launch intent Intent { act=android.intent.action.MAIN flg=0x10000000 cmp=com.github.vase4kin.teamcityapp.mock/com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListActivity } within 45 seconds. Perhaps the main thread has not gone idle within a reasonable amount of time? There could be an animation or something constantly repainting the screen. Or the activity is doing network calls on creation? See the threaddump logs. For your reference the last time the event queue was idle before your activity launch request was 1469282567161 and now the last time the queue went idle was: 1469282572365. If these numbers are the same your activity might be hogging the event queue.
     *
     * FIX ME
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testUserCanSeeSuccessFullyLoadedRunningBuilds() throws Exception {
        mActivityRule.launchActivity(null);

        // Checking toolbar title
        matchToolbarTitle("Running builds (1)");
        // List has item with header
        onView(withId(R.id.my_recycler_view)).check(hasItemsCount(2));
        // Checking header
        onView(withId(R.id.section_text)).check(matches(withText("Checkstyle_IdeaInspectionsPullRequest")));
        // Checking adapter item
        onView(withRecyclerView(R.id.my_recycler_view).atPositionOnView(1, R.id.itemTitle)).check(matches(withText("Running tests")));
        onView(withRecyclerView(R.id.my_recycler_view).atPositionOnView(1, R.id.itemSubTitle)).check(matches(withText("refs/heads/master")));
        onView(withRecyclerView(R.id.my_recycler_view).atPositionOnView(1, R.id.buildNumber)).check(matches(withText("#2458")));
    }

    @Test
    public void testUserCanSeeFailureMessageIfSmthHappendsOnRunningBuildsLoading() throws Exception {
        when(mTeamCityService.listRunningBuilds(anyString(), anyString())).thenReturn(Observable.<Builds>error(new RuntimeException("smth bad happend!")));

        mActivityRule.launchActivity(null);

        matchToolbarTitle("Running builds (0)");
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeEmptyDataMessageIfRunningBuildListIsEmpty() throws Exception {
        when(mTeamCityService.listRunningBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        mActivityRule.launchActivity(null);

        matchToolbarTitle("Running builds (0)");
        onView(withId(android.R.id.empty)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_running_builds)));
    }
}