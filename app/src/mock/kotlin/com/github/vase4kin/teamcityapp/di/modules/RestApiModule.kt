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

package com.github.vase4kin.teamcityapp.di.modules

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
import com.github.vase4kin.teamcityapp.di.modules.AppModule.CLIENT_AUTH
import com.github.vase4kin.teamcityapp.di.scopes.UserScope
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import teamcityapp.libraries.coroutines.IoDispatcher

/**
 * Mocked rest api module
 */
@DisableInstallInCheck
@Module
open class RestApiModule(@Suppress("UNUSED_PARAMETER") baseUrl: String) {

    /**
     * @return [FakeTeamCityServiceImpl]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @UserScope
    open fun provideTeamCityService(@Suppress("UNUSED_PARAMETER") @Named(CLIENT_AUTH) okHttpClient: OkHttpClient): TeamCityService = FakeTeamCityServiceImpl()

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @UserScope
    open fun provideRepository(
        teamCityService: TeamCityService,
        @Suppress("UNUSED_PARAMETER") cacheProviders: CacheProviders,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): Repository = FakeRepositoryImpl(teamCityService, ioDispatcher)
}
