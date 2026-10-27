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
import teamcityapp.libraries.accounts.AccountStorage
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.security.CryptoManager
import teamcityapp.libraries.session.AndroidAccountPreferencesStorage
import teamcityapp.libraries.session.LegacyAccountPreferencesMigration
import teamcityapp.libraries.session.LegacyAndroidAccountStorage

private val Context.accountPreferences by preferencesDataStore(name = "account_preferences_v1")

/** Binds account persistence and the common response cache; storage libraries own migration behavior. */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides @Singleton
    fun responseCache(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): teamcityapp.libraries.network.ResponseCacheStore = teamcityapp.libraries.network.AndroidResponseCacheStore(context)

    @Provides @Singleton
    fun accountStorage(storage: SharedUserStorage, crypto: CryptoManager, @dagger.hilt.android.qualifiers.ApplicationContext storageContext: Context, @MainDispatcher dispatcher: CoroutineDispatcher, onboarding: OnboardingManager): AccountStorage = AndroidAccountPreferencesStorage(
        LegacyAndroidAccountStorage(storage, crypto, dispatcher),
        storage,
        storageContext.accountPreferences,
        dispatcher,
        LegacyAccountPreferencesMigration(storageContext, onboarding)::read
    )
}
