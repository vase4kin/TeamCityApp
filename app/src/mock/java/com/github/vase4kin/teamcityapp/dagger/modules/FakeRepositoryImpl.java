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

import androidx.annotation.Nullable;

import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Single;
import okhttp3.ResponseBody;

/**
 * Fake repository impl for UI tests without cache support
 */
public class FakeRepositoryImpl implements Repository {

    private TeamCityService mTeamCityService;

    public FakeRepositoryImpl(TeamCityService teamCityService) {
        this.mTeamCityService = teamCityService;
    }

    @Override
    public Single<Agents> listAgents(@Nullable Boolean includeDisconnected, @Nullable String fields, @Nullable String locator, boolean update) {
        return mTeamCityService.listAgents(includeDisconnected, fields, locator);
    }

    @Override
    public Single<NavigationNode> listBuildTypes(String url, boolean update) {
        return mTeamCityService.listBuildTypes(url);
    }

    @Override
    public Single<BuildType> buildType(String id, boolean update) {
        return mTeamCityService.buildType(id);
    }

    @Override
    public Single<Build> build(String url, boolean update) {
        return mTeamCityService.build(url);
    }

    @Override
    public Single<Builds> listBuilds(String id, String locator, boolean update) {
        return mTeamCityService.listBuilds(id, locator);
    }

    @Override
    public Single<Builds> listRunningBuilds(String locator, String fields, boolean update) {
        return mTeamCityService.listRunningBuilds(locator, fields);
    }

    @Override
    public Single<Builds> listQueueBuilds(String fields, boolean update) {
        return mTeamCityService.listQueueBuilds(fields);
    }

    @Override
    public Single<Builds> listSnapshotBuilds(String id, boolean update) {
        return mTeamCityService.listBuilds(id);
    }

    @Override
    public Single<Builds> listMoreBuilds(String url) {
        return mTeamCityService.listMoreBuilds(url);
    }

    @Override
    public Single<Files> listArtifacts(String url, String locator, boolean update) {
        return mTeamCityService.listArtifacts(url, locator);
    }

    @Override
    public Single<ResponseBody> downloadFile(String url) {
        return mTeamCityService.downloadFile(url);
    }

    @Override
    public Single<TestOccurrences> listTestOccurrences(String url, boolean update) {
        return mTeamCityService.listTestOccurrences(url);
    }

    @Override
    public Single<TestOccurrences.TestOccurrence> testOccurrence(String url) {
        return mTeamCityService.testOccurrence(url);
    }

    @Override
    public Single<Changes> listChanges(String url, boolean update) {
        return mTeamCityService.listChanges(url);
    }

    @Override
    public Single<Changes.Change> change(String url) {
        return mTeamCityService.change(url);
    }

    @Override
    public Single<Branches> listBranches(String buildTypeId) {
        return mTeamCityService.listBranches(buildTypeId);
    }

    @Override
    public Single<Build> queueBuild(Build build) {
        return mTeamCityService.queueBuild(build);
    }

    @Override
    public Single<Build> cancelBuild(String url, BuildCancelRequest buildCancelRequest) {
        return mTeamCityService.cancelBuild(url, buildCancelRequest);
    }

    @NotNull
    @Override
    public Single<Build> cacheBuild(@NotNull Build serverBuild) {
        return mTeamCityService.build(serverBuild.getHref());
    }
}
