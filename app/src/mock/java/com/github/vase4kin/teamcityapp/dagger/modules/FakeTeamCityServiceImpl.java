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

package com.github.vase4kin.teamcityapp.dagger.modules;

import androidx.annotation.Nullable;

import com.github.vase4kin.teamcityapp.BuildConfig;
import com.github.vase4kin.teamcityapp.about.ServerInfo;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.runbuild.api.Branch;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Fake TC REST impl
 */
public class FakeTeamCityServiceImpl implements TeamCityService {

    @Override
    public Single<Agents> listAgents(
            @Nullable @Query("includeDisconnected") Boolean includeDisconnected,
            @Nullable @Query("fields") String fields,
            @Nullable @Query("locator") String locator) {
        if (includeDisconnected != null && includeDisconnected) {
            return Single
                    .just(Mocks.disconnectedAgents());
        } else if (includeDisconnected != null && !includeDisconnected) {
            return Single
                    .just(Mocks.connectedAgents());
        } else {
            List<Agent> merged = new ArrayList<>();
            merged.addAll(Mocks.connectedAgents().getObjects());
            merged.addAll(Mocks.disconnectedAgents().getObjects());
            return Single
                    .just(new Agents(4, merged));
        }
    }

    @Override
    public Single<NavigationNode> listBuildTypes(@Url String url) {
        return Single.just(Mocks.navigationNode());
    }

    @Override
    public Single<BuildType> buildType(@Path("id") String id) {
        return Single.just(Mocks.buildTypeMock());
    }

    @Override
    public Single<Build> build(@Url String url) {
        switch (url) {
            // queued builds
            case "/guestAuth/app/rest/buildQueue/id:823050":
                return Single.just(Mocks.queuedBuild1());
            case "/guestAuth/app/rest/buildQueue/id:823051":
                return Single.just(Mocks.queuedBuild2());
            case "/guestAuth/app/rest/buildQueue/id:823052":
                return Single.just(Mocks.queuedBuild3());
            // running build
            case "/guestAuth/app/rest/builds/id:783911":
                return Single.just(Mocks.runningBuild());
            case "/guestAuth/app/rest/builds/id:7839123":
                return Single.just(Mocks.runningBuild2());
            case "/guestAuth/app/rest/builds/id:783912":
                return Single.just(Mocks.successBuild());
            case "/guestAuth/app/rest/builds/id:783913":
                return Single.just(Mocks.failedBuild());
            default:
                return Observable.<Build>empty().singleOrError();
        }
    }

    @Override
    public Single<Builds> listBuilds(@Path("id") String id, @Query("locator") String locator) {
        List<Build> builds = new ArrayList<>();
        builds.add(Mocks.runningBuild());
        builds.add(Mocks.successBuild());
        builds.add(Mocks.failedBuild());
        return Single.just(new Builds(3, builds));
    }

    @Override
    public Single<Builds> listRunningBuilds(@Query("locator") String locator, @Query("fields") String fields) {
        List<Build> builds = new ArrayList<>();
        builds.add(Mocks.runningBuild());
        return Single.just(new Builds(1, builds));
    }

    @Override
    public Single<Builds> listQueueBuilds(@Query("locator") String locator, @Query("fields") String fields) {
        List<Build> builds = new ArrayList<>();
        builds.add(Mocks.queuedBuild1());
        builds.add(Mocks.queuedBuild2());
        builds.add(Mocks.queuedBuild3());
        return Single.just(new Builds(3, builds));
    }

    @Override
    public Single<Builds> listBuilds(String locator) {
        return listQueueBuilds(null, null);
    }

    @Override
    public Single<Builds> listMoreBuilds(@Url String url) {
        return Observable.<Builds>empty().singleOrError();
    }

    @Override
    public Single<Files> listArtifacts(@Url String url, @Query("locator") String locator) {
        switch (url) {
            case "/guestAuth/app/rest/builds/id:92912/artifacts/children/":
                return Single.just(Mocks.artifacts());
            default:
                return Observable.<Files>empty().singleOrError();
        }
    }

    @Override
    public Single<ResponseBody> downloadFile(@Url String url) {
        return Single.just(ResponseBody.create(MediaType.parse("text"), "text"));
    }

    @Override
    public Single<TestOccurrences> listTestOccurrences(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10":
                return Single.just(Mocks.failedTests());
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:SUCCESS,count:10":
                return Single.just(Mocks.passedTests());
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:UNKNOWN,count:10":
                return Single.just(Mocks.ignoredTests());
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),count:2147483647&fields=count":
                return Single.just(new TestOccurrences(16));
            default:
                return Observable.<TestOccurrences>empty().singleOrError();
        }
    }

    @Override
    public Single<TestOccurrences.TestOccurrence> testOccurrence(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)":
                return Single.just(new TestOccurrences.TestOccurrence("exception error"));
            case "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)":
                return Single.error(new RuntimeException("Something bad happens!"));
            default:
                return Observable.<TestOccurrences.TestOccurrence>empty().singleOrError();
        }
    }

    @Override
    public Single<Changes> listChanges(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/changes?locator=build:(id:826073),count:10":
            case "/guestAuth/app/rest/changes?locator=build:(id:826073),count:2147483647&fields=count":
                return Single.just(Mocks.changes());
            default:
                return Observable.<Changes>empty().singleOrError();
        }
    }

    @Override
    public Single<Changes.Change> change(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/changes/id:2503722":
                return Single.just(Mocks.singleChange());
            default:
                return Observable.<Changes.Change>empty().singleOrError();
        }
    }

    @Override
    public Single<Branches> listBranches(@Path("id") String id) {
        return Single.just(new Branches(Collections.singletonList(new Branch("master"))));
    }

    @Override
    public Single<Build> queueBuild(@Body Build build) {
        return Single.just(Mocks.queuedBuild1());
    }

    @Override
    public Single<Build> cancelBuild(@Url String url, @Body BuildCancelRequest buildCancelRequest) {
        return Single.just(Mocks.failedBuild());
    }

    @NotNull
    @Override
    public Single<ServerInfo> serverInfo() {
        return Single.just(new ServerInfo(BuildConfig.VERSION_NAME, Mocks.URL));
    }
}
