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

import com.github.vase4kin.teamcityapp.BuildConfig
import com.github.vase4kin.teamcityapp.about.ServerInfo
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import teamcityapp.features.test_details.api.models.TestOccurrence
import java.util.ArrayList

/**
 * Fake TC REST impl
 */
open class FakeTeamCityServiceImpl : TeamCityService {

    override fun listAgents(
        @Query("includeDisconnected") includeDisconnected: Boolean?,
        @Query("fields") fields: String?,
        @Query("locator") locator: String?
    ): Single<Agents> {
        return if (includeDisconnected != null && includeDisconnected) {
            Single
                .just(Mocks.disconnectedAgents())
        } else if (includeDisconnected != null && !includeDisconnected) {
            Single
                .just(Mocks.connectedAgents())
        } else {
            val merged = ArrayList<Agent>()
            merged.addAll(Mocks.connectedAgents().objects)
            merged.addAll(Mocks.disconnectedAgents().objects)
            Single
                .just(Agents(4, merged))
        }
    }

    override fun listBuildTypes(@Url id: String): Single<NavigationNode> {
        return Single.just(Mocks.navigationNode())
    }

    override fun buildType(@Path("id") id: String): Single<BuildType> {
        return Single.just(Mocks.buildTypeMock())
    }

    override fun build(@Url url: String): Single<Build> {
        return when (url) {
            // queued builds
            "/guestAuth/app/rest/buildQueue/id:823050" -> Single.just(Mocks.queuedBuild1())
            "/guestAuth/app/rest/buildQueue/id:823051" -> Single.just(Mocks.queuedBuild2())
            "/guestAuth/app/rest/buildQueue/id:823052" -> Single.just(Mocks.queuedBuild3())
            // running build
            "/guestAuth/app/rest/builds/id:783911" -> Single.just(Mocks.runningBuild())
            "/guestAuth/app/rest/builds/id:7839123" -> Single.just(Mocks.runningBuild2())
            "/guestAuth/app/rest/builds/id:783912" -> Single.just(Mocks.successBuild())
            "/guestAuth/app/rest/builds/id:783913" -> Single.just(Mocks.failedBuild())
            else -> Observable.empty<Build>().singleOrError()
        }
    }

    override fun listBuilds(@Path("id") id: String, @Query("locator") locator: String): Single<Builds> {
        val builds = ArrayList<Build>()
        builds.add(Mocks.runningBuild())
        builds.add(Mocks.successBuild())
        builds.add(Mocks.failedBuild())
        return Single.just(Builds(3, builds))
    }

    override fun listRunningBuilds(@Query("locator") locator: String, @Query("fields") fields: String?): Single<Builds> {
        val builds = ArrayList<Build>()
        builds.add(Mocks.runningBuild())
        return Single.just(Builds(1, builds))
    }

    override fun listQueueBuilds(@Query("locator") locator: String?, @Query("fields") fields: String?): Single<Builds> {
        val builds = ArrayList<Build>()
        builds.add(Mocks.queuedBuild1())
        builds.add(Mocks.queuedBuild2())
        builds.add(Mocks.queuedBuild3())
        return Single.just(Builds(3, builds))
    }

    override fun listBuilds(locator: String): Single<Builds> {
        return listQueueBuilds(null, null)
    }

    override fun listMoreBuilds(@Url url: String): Single<Builds> {
        return Observable.empty<Builds>().singleOrError()
    }

    override fun listArtifacts(@Url url: String, @Query("locator") locator: String): Single<Files> {
        return when (url) {
            "/guestAuth/app/rest/builds/id:92912/artifacts/children/" -> Single.just(Mocks.artifacts())
            else -> Observable.empty<Files>().singleOrError()
        }
    }

    override fun downloadFile(@Url url: String): Single<ResponseBody> {
        return Single.just(ResponseBody.create(MediaType.parse("text"), "text"))
    }

    override fun listTestOccurrences(@Url url: String): Single<TestOccurrences> {
        return when (url) {
            "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10" -> Single.just(
                Mocks.failedTests()
            )
            "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:SUCCESS,count:10" -> Single.just(
                Mocks.passedTests()
            )
            "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:UNKNOWN,count:10" -> Single.just(
                Mocks.ignoredTests()
            )
            "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),count:2147483647&fields=count" -> Single.just(
                TestOccurrences(16)
            )
            else -> Observable.empty<TestOccurrences>().singleOrError()
        }
    }

    override fun testOccurrence(@Url url: String): Single<TestOccurrence> {
        return when (url) {
            "/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)" -> Single.just(
                TestOccurrence("exception error")
            )
            "/guestAuth/app/rest/testOccurrences/id:4484,build:(id:835695)" -> Single.error(
                RuntimeException("Something bad happens!")
            )
            else -> Observable.empty<TestOccurrence>().singleOrError()
        }
    }

    override fun listChanges(@Url url: String): Single<Changes> {
        return when (url) {
            "/guestAuth/app/rest/changes?locator=build:(id:826073),count:10", "/guestAuth/app/rest/changes?locator=build:(id:826073),count:2147483647&fields=count" -> Single.just(
                Mocks.changes()
            )
            else -> Observable.empty<Changes>().singleOrError()
        }
    }

    override fun change(@Url url: String): Single<Changes.Change> {
        return when (url) {
            "/guestAuth/app/rest/changes/id:2503722" -> Single.just(Mocks.singleChange())
            else -> Observable.empty<Changes.Change>().singleOrError()
        }
    }

    override fun listBranches(@Path("id") id: String): Single<Branches> {
        return Single.just(Branches(listOf(Branch("master"))))
    }

    override fun queueBuild(@Body build: Build): Single<Build> {
        return Single.just(Mocks.queuedBuild1())
    }

    override fun cancelBuild(@Url url: String, @Body buildCancelRequest: BuildCancelRequest): Single<Build> {
        return Single.just(Mocks.failedBuild())
    }

    override fun serverInfo(): Single<ServerInfo> {
        return Single.just(ServerInfo(BuildConfig.VERSION_NAME, Mocks.URL))
    }
}
