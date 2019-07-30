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

package com.github.vase4kin.teamcityapp.helper;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManagerImpl;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.github.vase4kin.teamcityapp.onboarding.OnboardingManagerImpl.PREF_NAME;
import static org.hamcrest.core.Is.is;

/**
 * Useful test utils
 */
public class TestUtils {

    /**
     * http://blog.sqisland.com/2015/05/espresso-match-toolbar-title.html
     *
     * @param title
     * @return
     */
    public static void matchToolbarTitle(
            CharSequence title) {
        onView(isAssignableFrom(Toolbar.class))
                .check(matches(withToolbarTitle(is(title))));
    }

    public static void matchToolbarSubTitle(
            CharSequence title) {
        onView(isAssignableFrom(Toolbar.class))
                .check(matches(withToolbarSubTitle(is(title))));
    }

    /**
     * http://blog.sqisland.com/2015/05/espresso-match-toolbar-title.html
     *
     * @param textMatcher
     * @return
     */
    private static Matcher<Object> withToolbarTitle(
            final Matcher<CharSequence> textMatcher) {
        return new BoundedMatcher<Object, Toolbar>(Toolbar.class) {
            @Override
            public boolean matchesSafely(Toolbar toolbar) {
                return textMatcher.matches(toolbar.getTitle());
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("with toolbar title: ");
                textMatcher.describeTo(description);
            }
        };
    }

    private static Matcher<Object> withToolbarSubTitle(
            final Matcher<CharSequence> textMatcher) {
        return new BoundedMatcher<Object, Toolbar>(Toolbar.class) {
            @Override
            public boolean matchesSafely(Toolbar toolbar) {
                return textMatcher.matches(toolbar.getSubtitle());
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("with toolbar subtitle: ");
                textMatcher.describeTo(description);
            }
        };
    }

    /**
     * https://gist.github.com/chemouna/00b10369eb1d5b00401b
     *
     * @param count
     * @return
     */
    public static ViewAssertion hasItemsCount(final int count) {
        return new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException e) {
                if (!(view instanceof RecyclerView)) {
                    throw e;
                }
                RecyclerView rv = (RecyclerView) view;
                assertThat(rv.getAdapter().getItemCount(), is(count));
            }
        };
    }

    /**
     * Helper method to disable all onboarding
     */
    public static void disableOnboarding() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        OnboardingManager onboardingManager = new OnboardingManagerImpl(context);
        onboardingManager.saveNavigationDrawerPromptShown();
        onboardingManager.saveFilterBuildsPromptShown();
        onboardingManager.saveRunBuildPromptShown();
        onboardingManager.saveRemoveBuildFromQueuePromptShown();
        onboardingManager.saveStopBuildPromptShown();
        onboardingManager.saveRestartBuildPromptShown();
        onboardingManager.saveAddFavPromptShown();
        onboardingManager.saveFavPromptShown();
        onboardingManager.saveFilterBuildsPromptShown();
    }

    public static void enableOnboarding() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }
}
