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
 * Repository to manage api
 */
public interface Repository {

    /**
     * List agents
     * <p>
     * TODO: Add Cache
     *
     * @param includeDisconnected - determine what agents to return
     * @param fields              - additional TC fields
     * @return {@link Observable} with {@link Agents}
     */
    Observable<Agents> listAgents(@Nullable Boolean includeDisconnected,
                                  @Nullable String fields);

    /**
     * List build types by url (cache's supported)
     *
     * @param url    - Url of build type
     * @param update - Update cache
     * @return @return {@link Observable} with {@link NavigationNode}
     */
    Observable<NavigationNode> listBuildTypes(String url, boolean update);

    /**
     * Get single build details
     * <p>
     * TODO: Add Cache
     *
     * @param url - Build url
     * @return {@link Observable} with {@link Build}
     */
    Observable<Build> build(String url);

    /**
     * List builds
     * <p>
     * TODO: Add Cache
     *
     * @param id      - BuildType id
     * @param locator - TC locator
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listBuilds(String id, String locator);

    /**
     * List running builds
     *
     * @param locator - TC build locator
     * @param fields  - Additional TC fields
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listRunningBuilds(String locator, String fields);

    /**
     * List queued builds
     *
     * @param fields - Additional TC fields
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listQueueBuilds(String fields);

    /**
     * List more builds
     *
     * @param url - More builds url
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listMoreBuilds(String url);

    /**
     * List build artifacts
     * <p>
     * TODO: Add Cache (Artifacts are loaded after build's finished)
     *
     * @param url     - Artifacts url
     * @param locator - TC locator
     * @return {@link Observable} with {@link Files}
     */
    Observable<Files> listArtifacts(String url, String locator);

    /**
     * Download artifact file by url
     *
     * @param url - Artifact file url
     * @return {@link Observable} with {@link ResponseBody}
     */
    Observable<ResponseBody> downloadFile(String url);

    /**
     * List tests
     *
     * @param url - Build tests url
     * @return {@link Observable} with {@link TestOccurrences}
     */
    Observable<TestOccurrences> listTestOccurrences(String url);

    /**
     * Get single test info by url
     * <p>
     * TODO: Add Cache
     *
     * @param url - Test url
     * @return {@link Observable} with {@link TestOccurrences.TestOccurrence}
     */
    Observable<TestOccurrences.TestOccurrence> testOccurrence(String url);

    /**
     * List changes
     * <p>
     * TODO: Add Cache (Changes are loaded once when build's started or queued)
     *
     * @param url - Build changes url
     * @return {@link Observable} with {@link Changes}
     */
    Observable<Changes> listChanges(String url);

    /**
     * Get single change
     * <p>
     * TODO: Add Cache (Changes are loaded once when build's started or queued)
     *
     * @param url - Change url
     * @return {@link Observable} with {@link com.github.vase4kin.teamcityapp.changes.api.Changes.Change}
     */
    Observable<Changes.Change> change(String url);

    /**
     * List branches (cache's supported)
     *
     * @param buildTypeId - Build type id
     * @return {@link Observable} with {@link Branches}
     */
    Observable<Branches> listBranches(String buildTypeId);

    /**
     * Queue build (no cache's supported)
     *
     * @param build - Build to post
     * @return {@link Observable} with {@link Build}
     */
    Observable<Build> queueBuild(Build build);

    /**
     * Cancel/Removing build (no cache's supported)
     *
     * @param buildCancelRequest - request to stop a build
     * @return {@link Observable} with {@link Build}
     */
    Observable<Build> cancelBuild(String url, BuildCancelRequest buildCancelRequest);
}
