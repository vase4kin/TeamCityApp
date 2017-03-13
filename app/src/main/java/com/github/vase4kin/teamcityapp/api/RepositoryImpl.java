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

package com.github.vase4kin.teamcityapp.api;

import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import io.rx_cache.DynamicKey;
import io.rx_cache.EvictDynamicKey;
import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Impl of {@link Repository}
 */
public class RepositoryImpl implements Repository {

    private final TeamCityService mTeamCityService;
    private final CacheProviders mCacheCacheProviders;

    public RepositoryImpl(TeamCityService teamCityService, CacheProviders cacheProviders) {
        this.mTeamCityService = teamCityService;
        this.mCacheCacheProviders = cacheProviders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Agents> listAgents(@Nullable Boolean includeDisconnected, @Nullable String fields) {
        return mTeamCityService.listAgents(includeDisconnected, fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<NavigationNode> listBuildTypes(String url, boolean update) {
        return mCacheCacheProviders.listBuildTypes(mTeamCityService.listBuildTypes(url), new DynamicKey(url), new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Build> build(String url) {
        return mTeamCityService.build(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listBuilds(String id, String locator) {
        return mTeamCityService.listBuilds(id, locator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listRunningBuilds(String locator, String fields) {
        return mTeamCityService.listRunningBuilds(locator, fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listQueueBuilds(String fields) {
        return mTeamCityService.listQueueBuilds(fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listMoreBuilds(String url) {
        return mTeamCityService.listMoreBuilds(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Files> listArtifacts(String url, String locator) {
        return mTeamCityService.listArtifacts(url, locator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<ResponseBody> downloadFile(String url) {
        return mTeamCityService.downloadFile(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<TestOccurrences> listTestOccurrences(String url) {
        return mTeamCityService.listTestOccurrences(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<TestOccurrences.TestOccurrence> testOccurrence(String url) {
        return mTeamCityService.testOccurrence(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Changes> listChanges(String url) {
        return mTeamCityService.listChanges(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Changes.Change> change(String url) {
        return mTeamCityService.change(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Branches> listBranches(String buildTypeId) {
        return mCacheCacheProviders.listBranches(mTeamCityService.listBranches(buildTypeId), new DynamicKey(buildTypeId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Build> queueBuild(Build build) {
        return mTeamCityService.queueBuild(build);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Build> cancelBuild(String url, BuildCancelRequest buildCancelRequest) {
        return mTeamCityService.cancelBuild(url, buildCancelRequest);
    }
}
