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
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;

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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.mockito.Matchers.anyString;
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
    public CustomActivityTestRule<RootProjectsActivity> mActivityRule = new CustomActivityTestRule<>(RootProjectsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getRestApiInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL);
    }

    @Test
    public void testUserCanSeeSuccessFullyLoadedBuilds() throws Exception {
        mActivityRule.launchActivity(null);

        // open build type
        onView(withText("build type"))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("build type");

        // List has item with header
        onView(withId(R.id.build_recycler_view)).check(hasItemsCount(5));
        // Checking header 1
        onView(withRecyclerView(R.id.build_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("22 June")));
        // Checking adapter item 1
        // Checking adapter item
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

        // open build type
        onView(withText("build type"))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("build type");
        onView(withText("smth bad happend!")).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeLoadEmptyListMessageIfBuildListIsEmpty() throws Exception {
        when(mTeamCityService.listBuilds(anyString(), anyString())).thenReturn(Observable.just(new Builds(0, Collections.<Build>emptyList())));

        mActivityRule.launchActivity(null);

        // open build type
        onView(withText("build type"))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("build type");
        onView(withId(android.R.id.empty)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_builds)));
    }
}