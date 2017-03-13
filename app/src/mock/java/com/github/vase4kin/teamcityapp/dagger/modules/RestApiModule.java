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

package com.github.vase4kin.teamcityapp.dagger.modules;

import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders;
import com.github.vase4kin.teamcityapp.dagger.scopes.UserScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_AUTH;

/**
 * Mocked rest api module
 */
@Module
public class RestApiModule {

    String mBaseUrl;

    public RestApiModule(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    /**
     * @return {@link FakeTeamCityServiceImpl}
     */
    @VisibleForTesting
    @Provides
    @UserScope
    public TeamCityService provideTeamCityService(@Named(CLIENT_AUTH) OkHttpClient okHttpClient) {
        return new FakeTeamCityServiceImpl();
    }

    @Provides
    @UserScope
    Repository provideRepository(TeamCityService teamCityService, CacheProviders cacheProviders) {
        return new FakeRepositoryImpl(teamCityService);
    }
}
