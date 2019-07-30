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

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.home.view.HomeActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.util.Collections;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class RunningBuildsListActivityTest {

    @Rule
    public DaggerMockRule<RestApiComponent> daggerMockRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(restApiComponent -> {
                TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                app.setRestApiInjector(restApiComponent);
            });

    @Rule
    public CustomIntentsTestRule<HomeActivity> activityRule = new CustomIntentsTestRule<>(HomeActivity.class);

    @Spy
    private TeamCityService teamCityService = new FakeTeamCityServiceImpl();

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.disableOnboarding();
    }

    @Test
    public void testUserCanSeeUpdatedToolbar() throws Exception {
        activityRule.launchActivity(null);

        // Click on running builds tab
        clickOnRunningbuildsTab();

        // Checking toolbar title
        matchToolbarTitle("Running builds");
    }

    @Test
    public void testUserCanSeeSuccessFullyLoadedRunningBuilds() throws Exception {
        activityRule.launchActivity(null);

        // Click on running builds tab
        clickOnRunningbuildsTab();

        // List has item with header
        onView(withId(R.id.running_builds_recycler_view)).check(hasItemsCount(2));
        // Checking header
        onView(withId(R.id.section_text)).check(matches(withText("project name - build type name")));
        // Checking adapter item
        onView(withRecyclerView(R.id.running_builds_recycler_view).atPositionOnView(1, R.id.itemTitle)).check(matches(withText("Running tests")));
        onView(withRecyclerView(R.id.running_builds_recycler_view).atPositionOnView(1, R.id.itemSubTitle)).check(matches(withText("refs/heads/master")));
        onView(withRecyclerView(R.id.running_builds_recycler_view).atPositionOnView(1, R.id.buildNumber)).check(matches(withText("#2458")));
    }

    @Test
    public void testUserCanClickOnSection() throws Exception {
        activityRule.launchActivity(null);

        // Click on running builds tab
        clickOnRunningbuildsTab();

        // Click on header header
        onView(withId(R.id.section_text))
                .check(matches(withText("project name - build type name")))
                .perform(click());

        intended(allOf(
                hasComponent(BuildListActivity.class.getName()),
                hasExtras(allOf(
                        hasEntry(equalTo(BundleExtractorValues.BUILD_LIST_FILTER), equalTo(null)),
                        hasEntry(equalTo(BundleExtractorValues.ID), equalTo("Checkstyle_IdeaInspectionsPullRequest")),
                        hasEntry(equalTo(BundleExtractorValues.NAME), equalTo("build type name"))))));
    }

    @Test
    public void testUserCanSeeFailureMessageIfSmthHappendsOnRunningBuildsLoading() throws Exception {
        when(teamCityService.listRunningBuilds(anyString(), anyString())).thenReturn(Single.<Builds>error(new RuntimeException("smth bad happend!")));

        activityRule.launchActivity(null);

        // Click on running builds tab
        clickOnRunningbuildsTab();

        checkRunningTabBadgeCount("0");
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeEmptyDataMessageIfRunningBuildListIsEmpty() throws Exception {
        when(teamCityService.listRunningBuilds(anyString(), anyString())).thenReturn(Single.just(new Builds(0, Collections.<Build>emptyList())));

        activityRule.launchActivity(null);

        // Click on running builds tab
        clickOnRunningbuildsTab();

        onView(withId(R.id.running_empty_title_view)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_favorite_running_builds)));
    }

    private void clickOnRunningbuildsTab() {
        onView(withChild(allOf(withId(R.id.bottom_navigation_small_item_title), withText(R.string.running_builds_drawer_item))))
                .perform(click());
    }

    private void checkRunningTabBadgeCount(String count) {
        onView(allOf(
                withChild(allOf(withId(R.id.bottom_navigation_notification), withText(count))),
                withChild(allOf(withId(R.id.bottom_navigation_small_item_title), withText(R.string.running_builds_drawer_item))))
        )
                .check(matches(isDisplayed()));
    }
}