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

import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter;
import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Impl of {@link Repository}
 */
public class RepositoryImpl implements Repository {

    private final TeamCityService mTeamCityService;
    private final CacheProviders mCacheCacheProviders;
    private final UrlFormatter urlFormatter;

    public RepositoryImpl(TeamCityService teamCityService,
                          CacheProviders cacheProviders,
                          UrlFormatter urlFormatter) {
        this.mTeamCityService = teamCityService;
        this.mCacheCacheProviders = cacheProviders;
        this.urlFormatter = urlFormatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Agents> listAgents(@Nullable Boolean includeDisconnected,
                                         @Nullable String fields,
                                         @Nullable String locator,
                                         boolean update) {
        String dynamicKey = String.format("includeDisconnected:%s,fields:%s,locator:%s",
                includeDisconnected != null ? includeDisconnected : "empty",
                fields != null ? fields : "empty",
                locator != null ? locator : "empty");
        return mCacheCacheProviders.listAgents(
                mTeamCityService.listAgents(includeDisconnected, fields, locator),
                new DynamicKey(dynamicKey),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<NavigationNode> listBuildTypes(String id, boolean update) {
        return mCacheCacheProviders.listBuildTypes(
                mTeamCityService.listBuildTypes(id),
                new DynamicKey(id),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<BuildType> buildType(String id, boolean update) {
        return mCacheCacheProviders.buildType(
                mTeamCityService.buildType(id),
                new DynamicKey(id),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Build> build(String url, boolean update) {
        return mCacheCacheProviders.build(
                mTeamCityService.build(urlFormatter.formatBasicUrl(url)),
                new DynamicKey(url),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listBuilds(String id, String locator, boolean update) {
        return mCacheCacheProviders.listBuilds(
                mTeamCityService.listBuilds(id, locator),
                new DynamicKeyGroup(locator, id),
                new EvictDynamicKeyGroup(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listRunningBuilds(String locator, String fields, boolean update) {
        return mCacheCacheProviders.listRunningBuilds(
                mTeamCityService.listRunningBuilds(locator, fields),
                new DynamicKey(locator + fields),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listQueueBuilds(String fields, boolean update) {
        return mCacheCacheProviders.listQueuedBuilds(
                mTeamCityService.listQueueBuilds(fields),
                new DynamicKey(fields != null ? fields : "empty"),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Builds> listMoreBuilds(String url) {
        return mTeamCityService.listMoreBuilds(urlFormatter.formatBasicUrl(url));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Files> listArtifacts(String url, String locator, boolean update) {
        return mCacheCacheProviders.listArtifacts(
                mTeamCityService.listArtifacts(urlFormatter.formatBasicUrl(url), locator),
                new DynamicKey(url),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<ResponseBody> downloadFile(String url) {
        return mTeamCityService.downloadFile(urlFormatter.formatBasicUrl(url));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<TestOccurrences> listTestOccurrences(String url, boolean update) {
        return mCacheCacheProviders.listTestOccurrences(
                mTeamCityService.listTestOccurrences(urlFormatter.formatBasicUrl(url)),
                new DynamicKey(url),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<TestOccurrences.TestOccurrence> testOccurrence(String url) {
        return mCacheCacheProviders.testOccurrence(
                mTeamCityService.testOccurrence(urlFormatter.formatBasicUrl(url)),
                new DynamicKey(url));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Changes> listChanges(String url, boolean update) {
        return mCacheCacheProviders.listChanges(
                mTeamCityService.listChanges(urlFormatter.formatBasicUrl(url)),
                new DynamicKey(url),
                new EvictDynamicKey(update));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Changes.Change> change(String url) {
        return mCacheCacheProviders.change(mTeamCityService.change(urlFormatter.formatBasicUrl(url)), new DynamicKey(url));
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
        return mTeamCityService.cancelBuild(urlFormatter.formatBasicUrl(url), buildCancelRequest);
    }
}
