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

package com.github.vase4kin.teamcityapp.drawer;

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
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Tests for Drawer
 */
@RunWith(AndroidJUnit4.class)
public class DrawerTest {

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
        app.getRestApiInjector().sharedUserStorage().createNewUserAccountAndSetItAsActive(Mocks.URL);
        mActivityRule.launchActivity(null);
    }

    @Test
    public void testUserCanSeeInfo() throws Exception {
        // Opening drawer
        clickOnBurgerButton();

        // Check userInfo
        onView(allOf(withId(R.id.material_drawer_account_header_name), isDisplayed()))
                .check(matches(withText("Guest user")));

        onView(allOf(withId(R.id.material_drawer_account_header_email), isDisplayed()))
                .check(matches(withText(Mocks.URL)));
    }

    @Test
    public void testUserCanSeeProjectsIsSelectedByDefault() throws Exception {
        // Opening drawer
        clickOnBurgerButton();

        // Check projects is selected
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.projects_drawer_item), isDisplayed()))
                .check(matches(isSelected()));
    }

    @Test
    public void testUserCanNavigateToProjectsScreen() throws Exception {
        // open build type
        onView(withText("build type"))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("build type");

        // Opening drawer
        onView(withId(R.id.materialize_root))
                .perform(open());

        // Check projects is opened
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.projects_drawer_item), isDisplayed()))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("Projects");
    }

    @Test
    public void testUserCanNavigateToRunningBuildsScreen() throws Exception {
        // Opening drawer
        clickOnBurgerButton();

        // Check running builds is opened
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.running_builds_drawer_item), isDisplayed()))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("Running builds (1)");

        // Opening drawer
        clickOnBurgerButton();

        // Check about is selected
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.running_builds_drawer_item), isDisplayed()))
                .check(matches(isSelected()));
    }

    @Test
    public void testUserCanNavigateToQueuedBuildsScreen() throws Exception {
        // Opening drawer
        clickOnBurgerButton();

        // Check queued builds is opened
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.build_queue_drawer_item), isDisplayed()))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("Build queue (3)");

        // Opening drawer
        clickOnBurgerButton();

        // Check about is selected
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.build_queue_drawer_item), isDisplayed()))
                .check(matches(isSelected()));
    }

    @Test
    public void testUserCanNavigateToAgentsScreen() throws Exception {
        // Opening drawer
        clickOnBurgerButton();

        // Check agents is opened
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.agents_drawer_item), isDisplayed()))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("Agents");

        // Opening drawer
        clickOnBurgerButton();

        // Check about is selected
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.agents_drawer_item), isDisplayed()))
                .check(matches(isSelected()));
    }

    @Test
    public void testUserCanNavigateToAboutScreen() throws Exception {
        // Opening drawer
        clickOnBurgerButton();

        // Check about is opened
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.about_drawer_item), isDisplayed()))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("About");

        // Opening drawer
        clickOnBurgerButton();

        // Check about is selected
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.about_drawer_item), isDisplayed()))
                .check(matches(isSelected()));
    }

    @Test
    public void testUserCanNavigateToAccounts() throws Exception {
        // Opening drawer
        clickOnBurgerButton();

        // Click on account
        onView(allOf(withId(R.id.material_drawer_account_header_name), isDisplayed()))
                .perform(click());

        // Opening managing account activity
        onView(allOf(withId(R.id.material_drawer_name), withText(R.string.title_activity_account_list)))
                .perform(click());

        // Checking toolbar title
        matchToolbarTitle("Manage Accounts");
    }

    /**
     * Open drawer by clicking on burger button
     */
    private void clickOnBurgerButton() {
        onView(withContentDescription("Open")).perform(click());
    }
}