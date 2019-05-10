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

package com.github.vase4kin.teamcityapp.splash.view;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.login.view.LoginActivity;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Tests for {@link SplashActivity}
 */
@RunWith(AndroidJUnit4.class)
public class SplashActivityTest {

    @Rule
    public DaggerMockRule<AppComponent> mDaggerRule = new DaggerMockRule<>(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<AppComponent>() {
                @Override
                public void setComponent(AppComponent appComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setAppInjector(appComponent);
                }
            });

    @Rule
    public CustomIntentsTestRule<SplashActivity> mActivityRule = new CustomIntentsTestRule<>(SplashActivity.class);

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getAppInjector().sharedUserStorage().clearAll();
    }

    /**
     * Espresso can't record intents in case if there's no UI interactions
     */
    @Ignore
    @Test
    public void testUserNavigatesToRootProjectsActivityIgnored() throws Exception {
        // Prepate data
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getAppInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);

        // Launch activity
        mActivityRule.launchActivity(null);

        // Check launched intent
        intended(allOf(
                hasComponent(RootProjectsActivity.class.getName()),
                toPackage("com.github.vase4kin.teamcityapp.mock.debug")));
    }

    /**
     * Espresso can't record intents in case if there's no UI interactions
     */
    @Ignore
    @Test
    public void testUserNavigatesToLoginActivityIgnored() throws Exception {
        // Launch activity
        mActivityRule.launchActivity(null);

        // Check launched intent
        intended(allOf(
                hasComponent(LoginActivity.class.getName()),
                toPackage("com.github.vase4kin.teamcityapp.mock.debug")));
    }

    /**
     * Workaround test to test that root projects activity is opened
     */
    @Ignore
    @Test
    public void testUserNavigatesToRootProjectsActivity() throws Exception {
        // Prepate data
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getAppInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);

        // Launch activity
        mActivityRule.launchActivity(null);

        // Check that root projects activity is opened
        matchToolbarTitle("Projects");
    }

    /**
     * Workaround test to test that login activity is opened
     */
    @Test
    public void testUserNavigatesToLoginActivity() throws Exception {
        // Launch activity
        mActivityRule.launchActivity(null);

        //Checking that Login Activity is opened
        onView(withText(R.string.text_app_description)).check(matches(isDisplayed()));
    }
}