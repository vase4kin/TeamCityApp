/*
 * Copyright 2020 Andrey Tolpeev
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
import androidx.test.platform.app.InstrumentationRegistry
import teamcityapp.libraries.onboarding.OnboardingManagerImpl

/** Shared installed-preference setup for feature-hosted instrumentation. */
class TestUtils {
    companion object {
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
            kotlinx.coroutines.runBlocking {
                onboardingManager.saveShareBuildPromptShown()
                onboardingManager.saveOpenBuildInBrowserPromptShown()
            }
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
            kotlinx.coroutines.runBlocking {
                val onboardingManager = OnboardingManagerImpl(context)
                onboardingManager.saveShareBuildPromptShown(false)
                onboardingManager.saveOpenBuildInBrowserPromptShown(false)
            }
        }
    }
}
