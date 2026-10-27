/*
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.libraries.session

import android.content.Context
import teamcityapp.libraries.accounts.AccountPreferences
import teamcityapp.libraries.onboarding.OnboardingManager

/** Installed global education/rating values seed an account until its portable snapshot is saved. */
class LegacyAccountPreferencesMigration(context: Context, private val onboarding: OnboardingManager) {
    private val context = context.applicationContext
    suspend fun read(): AccountPreferences {
        val prompts = buildSet {
            if (onboarding.isNavigationDrawerPromptShown) add("home.Drawer")
            if (onboarding.isAddFavPromptShown) add("home.Favorites")
            if (onboarding.isRunningBuildsFilterPromptShown) add("home.Running")
            if (onboarding.isBuildsQueueFilterPromptShown) add("home.Queue")
            if (onboarding.isAgentsFilterPromptShown) add("home.Agents")
            if (onboarding.isRunBuildPromptShown) add("history.Run")
            if (onboarding.isFilterBuildsPromptShown) add("history.Filter")
            if (onboarding.isFavPromptShown) add("history.Favorite")
            if (onboarding.isStopBuildPromptShown) add("details.Stop")
            if (onboarding.isRemoveBuildFromQueuePromptShown) add("details.Remove")
            if (onboarding.isRestartBuildPromptShown) add("details.Restart")
            if (onboarding.isShareBuildPromptShown()) add("details.Share")
            if (onboarding.isOpenBuildInBrowserPromptShown()) add("details.Browser")
        }
        return AccountPreferences(shownPrompts = prompts, ratingHandled = context.getSharedPreferences("rateTheAppPref", Context.MODE_PRIVATE).getBoolean("rated", false))
    }
}
