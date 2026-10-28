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

import android.app.Application
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import teamcityapp.libraries.onboarding.OnboardingManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LegacyAccountPreferencesMigrationTest {
    @Test fun absentLegacyFlagsDoNotInventCompletedEducationOrRating() = runTest {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("rateTheAppPref", 0).edit().clear().commit()
        val result = LegacyAccountPreferencesMigration(context, onboarding()).read()
        assertTrue(result.shownPrompts.isEmpty())
        assertFalse(result.ratingHandled)
    }

    @Test fun everyInstalledPromptAndRatingFlagKeepsItsPortableIdentityWithoutChangingTheLegacyValues() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val preferences = context.getSharedPreferences("rateTheAppPref", 0)
        preferences.edit().putBoolean("rated", true).commit()
        val onboarding = onboarding()
        `when`(onboarding.isNavigationDrawerPromptShown).thenReturn(true)
        `when`(onboarding.isAddFavPromptShown).thenReturn(true)
        `when`(onboarding.isRunningBuildsFilterPromptShown).thenReturn(true)
        `when`(onboarding.isBuildsQueueFilterPromptShown).thenReturn(true)
        `when`(onboarding.isAgentsFilterPromptShown).thenReturn(true)
        `when`(onboarding.isRunBuildPromptShown).thenReturn(true)
        `when`(onboarding.isFilterBuildsPromptShown).thenReturn(true)
        `when`(onboarding.isFavPromptShown).thenReturn(true)
        `when`(onboarding.isStopBuildPromptShown).thenReturn(true)
        `when`(onboarding.isRemoveBuildFromQueuePromptShown).thenReturn(true)
        `when`(onboarding.isRestartBuildPromptShown).thenReturn(true)
        `when`(onboarding.isShareBuildPromptShown()).thenReturn(true)
        `when`(onboarding.isOpenBuildInBrowserPromptShown()).thenReturn(true)
        val result = LegacyAccountPreferencesMigration(context, onboarding).read()
        assertEquals(setOf("home.Drawer", "home.Favorites", "home.Running", "home.Queue", "home.Agents", "history.Run", "history.Filter", "history.Favorite", "details.Stop", "details.Remove", "details.Restart", "details.Share", "details.Browser"), result.shownPrompts)
        assertTrue(result.ratingHandled)
        assertTrue(preferences.getBoolean("rated", false))
        assertTrue(result.favoriteIds.isEmpty())
    }

    @Test fun partiallyCompletedEducationDoesNotAcknowledgeOtherPrompts() = runTest {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("rateTheAppPref", 0).edit().clear().commit()
        val onboarding = onboarding()
        `when`(onboarding.isNavigationDrawerPromptShown).thenReturn(true)
        `when`(onboarding.isShareBuildPromptShown()).thenReturn(true)
        assertEquals(setOf("home.Drawer", "details.Share"), LegacyAccountPreferencesMigration(context, onboarding).read().shownPrompts)
    }
    private suspend fun onboarding(): OnboardingManager = mock(OnboardingManager::class.java).also {
        `when`(it.isShareBuildPromptShown()).thenReturn(false)
        `when`(it.isOpenBuildInBrowserPromptShown()).thenReturn(false)
    }
}
