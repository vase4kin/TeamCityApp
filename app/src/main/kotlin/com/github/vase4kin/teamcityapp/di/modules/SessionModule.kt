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

import android.app.Application
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.navigation.AccountNavigationScope
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import teamcityapp.libraries.accounts.AccountSessionManager
import teamcityapp.libraries.accounts.AccountStorage
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.security.CryptoManager
import teamcityapp.libraries.session.LegacyAndroidAccountStorage

/** Resolves API dependencies from the current account graph when a new screen is injected. */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    @Provides @Singleton
    fun accountStorage(storage: SharedUserStorage, crypto: CryptoManager, @MainDispatcher dispatcher: CoroutineDispatcher): AccountStorage = LegacyAndroidAccountStorage(storage, crypto, dispatcher)

    @Provides @Singleton
    fun accountSessions(storage: AccountStorage, @AccountNavigationScope scope: CoroutineScope): AccountSessionManager = AccountSessionManager(storage, scope.coroutineContext)

    @Provides
    fun repository(application: Application): Repository = (application as TeamCityApplicationBase).restApiInjector.repository()

    @Provides
    fun service(application: Application): TeamCityService = (application as TeamCityApplicationBase).restApiInjector.teamCityService()
}
