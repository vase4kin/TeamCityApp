/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.home.view;

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
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.BuildTypes;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.navigation.api.Project;
import com.github.vase4kin.teamcityapp.navigation.api.Projects;

import org.junit.Before;
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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HomeActivity}
 */
@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    @Rule
    public DaggerMockRule<RestApiComponent> mDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(restApiComponent -> {
                TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                app.setRestApiInjector(restApiComponent);
            });

    @Rule
    public CustomIntentsTestRule<HomeActivity> mActivityRule = new CustomIntentsTestRule<>(HomeActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.INSTANCE.disableOnboarding();
    }

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getAppInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);
    }

    @Test
    public void testUserCanSeeProjectsData() {
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
        when(mTeamCityService.listBuildTypes(anyString())).thenCallRealMethod().thenReturn(Single.just(navigationNode));

        mActivityRule.launchActivity(null);

        // Checking toolbar title
        TestUtils.INSTANCE.matchToolbarTitle("Projects");

        // Checking projects data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.INSTANCE.hasItemsCount(2));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("Project")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemSubTitle))
                .check(matches(withText("Description")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("build type")));
        // Click on projects
        onView(withText("Project"))
                .perform(click());
        // Check toolbar
        TestUtils.INSTANCE.matchToolbarTitle("Project");
        // Check Project data
        onView(withId(R.id.navigation_recycler_view)).check(TestUtils.INSTANCE.hasItemsCount(2));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("New project")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(0, R.id.itemSubTitle))
                .check(matches(withText("Contains a lof of projects")));
        onView(withRecyclerView(R.id.navigation_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Build and run tests")));
    }
}