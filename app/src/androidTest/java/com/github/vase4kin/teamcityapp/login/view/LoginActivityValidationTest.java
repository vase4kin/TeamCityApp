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

package com.github.vase4kin.teamcityapp.login.view;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Validation tests for {@link LoginActivity}
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityValidationTest {

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
    public CustomActivityTestRule<LoginActivity> mActivityRule = new CustomActivityTestRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        mActivityRule.launchActivity(null);
    }

    @Test
    public void testUserCanNotCreateAccountWithEmptyUrl() {
        onView(withId(R.id.teamcity_url)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText(R.string.server_cannot_be_empty)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanNotCreateAccountWithIncorrectProvidedUrl() {
        onView(withId(R.id.teamcity_url)).perform(clearText(), typeText("google.com"), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText(R.string.server_correct_url)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSubmitDataByClickOnActionDone() {
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.teamcity_url)).perform(clearText(), pressImeActionButton());
        onView(withText(R.string.server_cannot_be_empty)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserSeeProtocolInUrlField() {
        onView(withId(R.id.teamcity_url)).check(matches(withText("https://")));
    }

    @Test
    public void testUserCanNotCreateAccountWithEmptyUserName() {
        onView(withId(R.id.btn_login)).perform(scrollTo(), click());
        onView(withText(R.string.server_user_name_cannot_be_empty)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanNotCreateAccountWithEmptyPasswordName() {
        onView(withId(R.id.user_name)).perform(typeText("user"), closeSoftKeyboard());
        onView(withId(R.id.btn_login)).perform(scrollTo(), click());
        onView(withText(R.string.server_password_cannot_be_empty)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserWillNotSeeUserAndPasswordFieldsWhenGuestAccountIsOn() {
        onView(withId(R.id.user_name)).perform(typeText("user"), pressImeActionButton());
        onView(withId(R.id.password)).perform(typeText("pass"), closeSoftKeyboard());
        // Enabling guest mode
        onView(withId(R.id.guest_user_switch)).perform(click());
        // Check input text layouts are not visible
        onView(withId(R.id.user_field_wrapper)).check(matches(not(isDisplayed())));
        onView(withId(R.id.password_field_wrapper)).check(matches(not(isDisplayed())));
        // Enabling user mode
        onView(withId(R.id.guest_user_switch)).perform(click());
        // Check input text layouts are visible
        onView(withId(R.id.user_field_wrapper)).check(matches(isDisplayed()));
        onView(withId(R.id.password_field_wrapper)).check(matches(isDisplayed()));
    }
}