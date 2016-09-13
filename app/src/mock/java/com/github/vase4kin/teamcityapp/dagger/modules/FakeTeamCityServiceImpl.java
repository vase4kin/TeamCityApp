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

import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Fake TC REST impl
 */
public class FakeTeamCityServiceImpl implements TeamCityService {

    @Override
    public Observable<Agents> listAgents(
            @Nullable @Query("includeDisconnected") Boolean includeDisconnected,
            @Nullable @Query("fields") String fields) {
        if (includeDisconnected != null && includeDisconnected) {
            return Observable
                    .just(Mocks.disconnectedAgents());
        } else if (includeDisconnected != null && !includeDisconnected) {
            return Observable
                    .just(Mocks.connectedAgents());
        } else {
            List<Agent> merged = new ArrayList<>();
            merged.addAll(Mocks.connectedAgents().getObjects());
            merged.addAll(Mocks.disconnectedAgents().getObjects());
            return Observable
                    .just(new Agents(4, merged));
        }
    }

    @Override
    public Observable<NavigationNode> listBuildTypes(@Url String url) {
        return Observable.just(Mocks.navigationNode());
    }

    @Override
    public Observable<Build> build(@Url String url) {
        switch (url) {
            // queued builds
            case "/guestAuth/app/rest/buildQueue/id:823050":
                return Observable.just(Mocks.queuedBuild1());
            case "/guestAuth/app/rest/buildQueue/id:823051":
                return Observable.just(Mocks.queuedBuild2());
            case "/guestAuth/app/rest/buildQueue/id:823052":
                return Observable.just(Mocks.queuedBuild3());
            // running build
            case "/guestAuth/app/rest/builds/id:783911":
                return Observable.just(Mocks.runningBuild());
            case "/guestAuth/app/rest/builds/id:783912":
                return Observable.just(Mocks.successBuild());
            case "/guestAuth/app/rest/builds/id:783913":
                return Observable.just(Mocks.failedBuild());
            default:
                return Observable.empty();
        }
    }

    @Override
    public Observable<Builds> listBuilds(@Path("id") String id, @Query("locator") String locator) {
        List<Build> builds = new ArrayList<>();
        builds.add(Mocks.runningBuild());
        builds.add(Mocks.successBuild());
        builds.add(Mocks.failedBuild());
        return Observable.just(new Builds(3, builds));
    }

    @Override
    public Observable<Builds> listRunningBuilds(@Query("locator") String locator, @Query("fields") String fields) {
        List<Build> builds = new ArrayList<>();
        builds.add(Mocks.runningBuild());
        return Observable.just(new Builds(1, builds));
    }

    @Override
    public Observable<Builds> listQueueBuilds(@Query("fields") String fields) {
        List<Build> builds = new ArrayList<>();
        builds.add(Mocks.queuedBuild1());
        builds.add(Mocks.queuedBuild2());
        builds.add(Mocks.queuedBuild3());
        return Observable.just(new Builds(3, builds));
    }

    @Override
    public Observable<Builds> listMoreBuilds(@Url String url) {
        return Observable.empty();
    }

    @Override
    public Observable<Files> listArtifacts(@Url String url, @Query("locator") String locator) {
        switch (url) {
            case "/guestAuth/app/rest/builds/id:92912/artifacts/children/":
                return Observable.just(Mocks.artifacts());
            default:
                return Observable.empty();
        }
    }

    @Override
    public Observable<ResponseBody> downloadFile(@Url String url) {
        return Observable.empty();
    }

    @Override
    public Observable<TestOccurrences> listTestOccurrences(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10":
                return Observable.just(Mocks.failedTests());
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:SUCCESS,count:10":
                return Observable.just(Mocks.passedTests());
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:UNKNOWN,count:10":
                return Observable.just(Mocks.ignoredTests());
            case "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),count:2147483647&fields=count":
                return Observable.just(new TestOccurrences(16));
            default:
                return Observable.empty();
        }
    }

    @Override
    public Observable<TestOccurrences.TestOccurrence> testOccurrence(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)":
                return Observable.just(new TestOccurrences.TestOccurrence("exception error"));
            case "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)":
                return Observable.error(new RuntimeException("Something bad happens!"));
            default:
                return Observable.empty();
        }
    }

    @Override
    public Observable<Changes> listChanges(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/changes?locator=build:(id:826073),count:10":
            case "/guestAuth/app/rest/changes?locator=build:(id:826073),count:2147483647&fields=count":
                return Observable.just(Mocks.changes());
            default:
                return Observable.empty();
        }
    }

    @Override
    public Observable<Changes.Change> change(@Url String url) {
        switch (url) {
            case "/guestAuth/app/rest/changes/id:2503722":
                return Observable.just(Mocks.singleChange());
            default:
                return Observable.empty();
        }
    }
}
