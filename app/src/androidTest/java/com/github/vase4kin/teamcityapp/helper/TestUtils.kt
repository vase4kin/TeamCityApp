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

package com.github.vase4kin.teamcityapp.helper

import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManagerImpl
import org.hamcrest.Matcher
import org.hamcrest.core.Is.`is`

/**
 * Useful test utils
 */
class TestUtils {
    companion object {
        /**
         * http://blog.sqisland.com/2015/05/espresso-match-toolbar-title.html
         *
         * @param title
         * @return
         */
        @JvmStatic
        fun matchToolbarTitle(
            title: CharSequence
        ) {
            onView(isAssignableFrom(Toolbar::class.java))
                .check(matches(withToolbarTitle(`is`(title))))
        }

        @JvmStatic
        fun matchToolbarSubTitle(
            title: CharSequence
        ) {
            onView(isAssignableFrom(Toolbar::class.java))
                .check(matches(withToolbarSubTitle(`is`(title))))
        }

        /**
         * http://blog.sqisland.com/2015/05/espresso-match-toolbar-title.html
         *
         * @param textMatcher
         * @return
         */
        private fun withToolbarTitle(
            textMatcher: Matcher<CharSequence>
        ): Matcher<Any> {
            return object : BoundedMatcher<Any, Toolbar>(Toolbar::class.java) {
                public override fun matchesSafely(toolbar: Toolbar): Boolean {
                    return textMatcher.matches(toolbar.title)
                }

                override fun describeTo(description: org.hamcrest.Description) {
                    description.appendText("with toolbar title: ")
                    textMatcher.describeTo(description)
                }
            }
        }

        private fun withToolbarSubTitle(
            textMatcher: Matcher<CharSequence>
        ): Matcher<Any> {
            return object : BoundedMatcher<Any, Toolbar>(Toolbar::class.java) {
                public override fun matchesSafely(toolbar: Toolbar): Boolean {
                    return textMatcher.matches(toolbar.subtitle)
                }

                override fun describeTo(description: org.hamcrest.Description) {
                    description.appendText("with toolbar subtitle: ")
                    textMatcher.describeTo(description)
                }
            }
        }

        /**
         * https://gist.github.com/chemouna/00b10369eb1d5b00401b
         *
         * @param count
         * @return
         */
        @JvmStatic
        fun hasItemsCount(count: Int): ViewAssertion {
            return ViewAssertion { view, noViewFoundException ->
                if (view !is RecyclerView) {
                    throw noViewFoundException
                }
                assertThat(view.adapter?.itemCount ?: 0, `is`(count))
            }
        }

        /**
         * Helper method to disable all onboarding
         */
        @JvmStatic
        fun disableOnboarding() {
            val context =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            val onboardingManager = OnboardingManagerImpl(context)
            onboardingManager.saveNavigationDrawerPromptShown()
            onboardingManager.saveFilterBuildsPromptShown()
            onboardingManager.saveRunBuildPromptShown()
            onboardingManager.saveRemoveBuildFromQueuePromptShown()
            onboardingManager.saveStopBuildPromptShown()
            onboardingManager.saveRestartBuildPromptShown()
            onboardingManager.saveAddFavPromptShown()
            onboardingManager.saveFavPromptShown()
            onboardingManager.saveRunningBuildsFilterPromptShown()
            onboardingManager.saveBuildsQueueFilterPromptShown()
            onboardingManager.saveAgentsFilterPromptShown()
        }

        @JvmStatic
        fun enableOnboarding() {
            val context =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            context.getSharedPreferences(OnboardingManagerImpl.PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear().commit()
        }
    }
}
