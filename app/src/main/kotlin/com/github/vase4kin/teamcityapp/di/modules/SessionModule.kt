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

package com.github.vase4kin.teamcityapp.di.modules

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import teamcityapp.libraries.accounts.AccountPreferences
import teamcityapp.libraries.accounts.AccountStorage
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.security.CryptoManager
import teamcityapp.libraries.session.AndroidAccountPreferencesStorage
import teamcityapp.libraries.session.LegacyAndroidAccountStorage

private val Context.accountPreferences by preferencesDataStore(name = "account_preferences_v1")

/** Resolves API dependencies from the current account graph when a new screen is injected. */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides @Singleton
    fun responseCache(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): teamcityapp.libraries.network.ResponseCacheStore = com.github.vase4kin.teamcityapp.navigation.AndroidResponseCacheStore(context)

    @Provides @Singleton
    fun accountStorage(storage: SharedUserStorage, crypto: CryptoManager, @dagger.hilt.android.qualifiers.ApplicationContext storageContext: Context, @MainDispatcher dispatcher: CoroutineDispatcher, onboarding: OnboardingManager): AccountStorage = AndroidAccountPreferencesStorage(LegacyAndroidAccountStorage(storage, crypto, dispatcher), storage, storageContext.accountPreferences, dispatcher) {
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
        AccountPreferences(shownPrompts = prompts, ratingHandled = storageContext.getSharedPreferences("rateTheAppPref", Context.MODE_PRIVATE).getBoolean("rated", false))
    }
}
