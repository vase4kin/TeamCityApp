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

import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Fake repository impl for UI tests without cache support
 */
public class FakeRepositoryImpl implements Repository {

    private TeamCityService mTeamCityService;

    public FakeRepositoryImpl(TeamCityService teamCityService) {
        this.mTeamCityService = teamCityService;
    }

    @Override
    public Observable<Agents> listAgents(@Nullable Boolean includeDisconnected, @Nullable String fields) {
        return mTeamCityService.listAgents(includeDisconnected, fields);
    }

    @Override
    public Observable<NavigationNode> listBuildTypes(String url, boolean update) {
        return mTeamCityService.listBuildTypes(url);
    }

    @Override
    public Observable<Build> build(String url) {
        return mTeamCityService.build(url);
    }

    @Override
    public Observable<Builds> listBuilds(String id, String locator) {
        return mTeamCityService.listBuilds(id, locator);
    }

    @Override
    public Observable<Builds> listRunningBuilds(String locator, String fields) {
        return mTeamCityService.listRunningBuilds(locator, fields);
    }

    @Override
    public Observable<Builds> listQueueBuilds(String fields) {
        return mTeamCityService.listQueueBuilds(fields);
    }

    @Override
    public Observable<Builds> listMoreBuilds(String url) {
        return mTeamCityService.listMoreBuilds(url);
    }

    @Override
    public Observable<Files> listArtifacts(String url, String locator) {
        return mTeamCityService.listArtifacts(url, locator);
    }

    @Override
    public Observable<ResponseBody> downloadFile(String url) {
        return mTeamCityService.downloadFile(url);
    }

    @Override
    public Observable<TestOccurrences> listTestOccurrences(String url) {
        return mTeamCityService.listTestOccurrences(url);
    }

    @Override
    public Observable<TestOccurrences.TestOccurrence> testOccurrence(String url) {
        return mTeamCityService.testOccurrence(url);
    }

    @Override
    public Observable<Changes> listChanges(String url) {
        return mTeamCityService.listChanges(url);
    }

    @Override
    public Observable<Changes.Change> change(String url) {
        return mTeamCityService.change(url);
    }

    @Override
    public Observable<Branches> listBranches(String buildTypeId) {
        return mTeamCityService.listBranches(buildTypeId);
    }

    @Override
    public Observable<Build> queueBuild(Build build) {
        return mTeamCityService.queueBuild(build);
    }

    @Override
    public Observable<Build> cancelBuild(String url, BuildCancelRequest buildCancelRequest) {
        return mTeamCityService.cancelBuild(url, buildCancelRequest);
    }
}
