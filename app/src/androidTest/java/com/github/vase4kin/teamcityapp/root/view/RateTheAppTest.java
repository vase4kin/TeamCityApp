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

package com.github.vase4kin.teamcityapp.root.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.BuildTypes;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.navigation.api.Project;
import com.github.vase4kin.teamcityapp.navigation.api.Projects;
import com.github.vase4kin.teamcityapp.remote.RemoteService;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collections;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RootProjectsActivity}
 */
@RunWith(AndroidJUnit4.class)
public class RateTheAppTest {

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

    @Mock
    private RemoteService remoteService;

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.disableOnboarding();
    }

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getAppInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);
        getSharedPreferences().edit().clear().commit();
    }

    @Test
    public void testUserCanSeeRateTheApp() {
        // Prepare data
        Project project = new Project();
        project.setName("New project");
        project.setDescription("Contains a lof of projects");
        BuildType buildType = new BuildType();
        buildType.setId("build_type_id");
        buildType.setName("Build and run tests");
        NavigationNode navigationNode = new NavigationNode(
                new Projects(Collections.singletonList(project)),
                new BuildTypes(Collections.singletonList(buildType)));
        when(mTeamCityService.listBuildTypes(anyString())).thenCallRealMethod().thenReturn(Observable.just(navigationNode));
        when(remoteService.isNotChurn()).thenReturn(true);

        mActivityRule.launchActivity(null);

        // Checking data
        onView(withId(R.id.navigation_recycler_view)).check(hasItemsCount(3));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("Project")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemSubTitle))
                .check(matches(withText("Description")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText(R.string.rate_title)));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemSubTitle))
                .check(matches(withText(R.string.rate_description)));

        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(2, R.id.itemTitle))
                .check(matches(withText("build type")));
    }

    @Test
    public void testUserCanCancelRateTheApp() {
        // Prepare data
        Project project = new Project();
        project.setName("New project");
        project.setDescription("Contains a lof of projects");
        BuildType buildType = new BuildType();
        buildType.setId("build_type_id");
        buildType.setName("Build and run tests");
        NavigationNode navigationNode = new NavigationNode(
                new Projects(Collections.singletonList(project)),
                new BuildTypes(Collections.singletonList(buildType)));
        when(mTeamCityService.listBuildTypes(anyString())).thenCallRealMethod().thenReturn(Observable.just(navigationNode));
        when(remoteService.isNotChurn()).thenReturn(true);

        mActivityRule.launchActivity(null);

        // Cancel rate the app
        onView(withId(R.id.button_cancel)).perform(click());

        // Check the flag is set
        assertThat(getSharedPreferences().getBoolean("rated", false), is(equalTo(true)));

        // Checking data
        onView(withId(R.id.navigation_recycler_view)).check(hasItemsCount(2));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("Project")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemSubTitle))
                .check(matches(withText("Description")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("build type")));


        // Click on projects
        onView(withText("Project"))
                .perform(click());

        // Check Project data
        onView(withId(R.id.navigation_recycler_view)).check(hasItemsCount(2));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("New project")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemSubTitle))
                .check(matches(withText("Contains a lof of projects")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Build and run tests")));
    }

    @Ignore
    @Test
    public void testUserCanRateTheApp() {
        // Prepare data
        Project project = new Project();
        project.setName("New project");
        project.setDescription("Contains a lof of projects");
        BuildType buildType = new BuildType();
        buildType.setId("build_type_id");
        buildType.setName("Build and run tests");
        NavigationNode navigationNode = new NavigationNode(
                new Projects(Collections.singletonList(project)),
                new BuildTypes(Collections.singletonList(buildType)));
        when(mTeamCityService.listBuildTypes(anyString())).thenCallRealMethod().thenReturn(Observable.just(navigationNode));
        when(remoteService.isNotChurn()).thenReturn(true);

        mActivityRule.launchActivity(null);

        // Rate the app
        onView(withId(R.id.button_rate)).perform(click());

        // Check the flag is set
        assertThat(getSharedPreferences().getBoolean("rated", false), is(equalTo(true)));

        // Check filter builds activity is opened
        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("market://details?id=com.github.vase4kin.teamcityapp.mock.debug")));
    }

    @Test
    public void testUserCanCancelRateTheApp_Regression() {
        // Prepare data
        Project project = new Project();
        project.setName("New project");
        project.setDescription("Contains a lof of projects");
        BuildType buildType = new BuildType();
        buildType.setId("build_type_id");
        buildType.setName("Build and run tests");
        NavigationNode navigationNode = new NavigationNode(
                new Projects(Collections.singletonList(project)),
                new BuildTypes(Collections.singletonList(buildType)));
        when(mTeamCityService.listBuildTypes(anyString())).thenCallRealMethod().thenReturn(Observable.just(navigationNode));
        when(remoteService.isNotChurn()).thenReturn(true);

        mActivityRule.launchActivity(null);

        // Cancel rate the app
        onView(withId(R.id.button_cancel)).perform(click());

        // Check the flag is set
        assertThat(getSharedPreferences().getBoolean("rated", false), is(equalTo(true)));

        // Checking data
        onView(withId(R.id.navigation_recycler_view)).check(hasItemsCount(2));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("Project")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemSubTitle))
                .check(matches(withText("Description")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("build type")));

        // Click on projects
        onView(withText("build type"))
                .perform(click());

        // no crash
        // user can see buildtype
        // Check toolbar
        matchToolbarTitle("build type");
    }

    private SharedPreferences getSharedPreferences() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        return app.getSharedPreferences("rateTheAppPref", Context.MODE_PRIVATE);
    }
}