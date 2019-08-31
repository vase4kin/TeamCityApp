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

package com.github.vase4kin.teamcityapp.dagger.modules

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_AUTH
import com.github.vase4kin.teamcityapp.dagger.scopes.UserScope
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Named

/**
 * Mocked rest api module
 */
@Module
open class RestApiModule(@Suppress("UNUSED_PARAMETER") baseUrl: String) {

    /**
     * @return [FakeTeamCityServiceImpl]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @UserScope
    open fun provideTeamCityService(@Suppress("UNUSED_PARAMETER") @Named(CLIENT_AUTH) okHttpClient: OkHttpClient): TeamCityService {
        return FakeTeamCityServiceImpl()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @UserScope
    open fun provideRepository(
        teamCityService: TeamCityService,
        @Suppress("UNUSED_PARAMETER") cacheProviders: CacheProviders
    ): Repository {
        return FakeRepositoryImpl(teamCityService)
    }
}
