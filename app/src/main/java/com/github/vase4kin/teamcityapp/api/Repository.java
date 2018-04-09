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
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
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
     * List agents (cache's supported)
     *
     * @param includeDisconnected - determine what agents to return
     * @param fields              - additional TC fields
     * @param locator             - TC locator
     * @param update              - update cache
     * @return {@link Observable} with {@link Agents}
     */
    Observable<Agents> listAgents(@Nullable Boolean includeDisconnected,
                                  @Nullable String fields,
                                  @Nullable String locator,
                                  boolean update);

    /**
     * List build types by url (cache's supported)
     *
     * @param id    - Id of build type
     * @param update - Update cache
     * @return @return {@link Observable} with {@link NavigationNode}
     */
    Observable<NavigationNode> listBuildTypes(String id, boolean update);

    /**
     * Get build type (cache's supported)
     *
     * @param id     - BuildType id
     * @param update - Update cache
     * @return {@link Observable} with {@link Builds}
     */
    Observable<BuildType> buildType(String id, boolean update);

    /**
     * Get single build details (cache's supported)
     *
     * @param url - Build url
     * @param update  - Update cache
     * @return {@link Observable} with {@link Build}
     */
    Observable<Build> build(String url, boolean update);

    /**
     * List builds (cache's supported)
     *
     * @param id      - BuildType id
     * @param locator - TC locator
     * @param update  - Update cache
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listBuilds(String id, String locator, boolean update);

    /**
     * List running builds (cache's supported)
     *
     * @param locator - TC build locator
     * @param fields  - Additional TC fields
     * @param update  - Update cache
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listRunningBuilds(String locator, String fields, boolean update);

    /**
     * List queued builds (cache's supported)
     *
     * @param fields - Additional TC fields
     * @param update  - Update cache
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listQueueBuilds(String fields, boolean update);

    /**
     * List more builds (no cache's supported)
     *
     * @param url - More builds url
     * @return {@link Observable} with {@link Builds}
     */
    Observable<Builds> listMoreBuilds(String url);

    /**
     * List build artifacts (cache's supported)
     *
     * @param url     - Artifacts url
     * @param locator - TC locator
     * @param update  - Update cache
     * @return {@link Observable} with {@link Files}
     */
    Observable<Files> listArtifacts(String url, String locator, boolean update);

    /**
     * Download artifact file by url (no cache's supported)
     *
     * @param url - Artifact file url
     * @return {@link Observable} with {@link ResponseBody}
     */
    Observable<ResponseBody> downloadFile(String url);

    /**
     * List tests (cache's supported)
     *
     * @param url - Build tests url
     * @param update - Update cache
     * @return {@link Observable} with {@link TestOccurrences}
     */
    Observable<TestOccurrences> listTestOccurrences(String url, boolean update);

    /**
     * Get single test info by url (cache's supported)
     *
     * @param url - Test url
     * @return {@link Observable} with {@link TestOccurrences.TestOccurrence}
     */
    Observable<TestOccurrences.TestOccurrence> testOccurrence(String url);

    /**
     * List changes (cache's supported)
     *
     * @param url - Build changes url
     * @param update - Update cache
     * @return {@link Observable} with {@link Changes}
     */
    Observable<Changes> listChanges(String url, boolean update);

    /**
     * Get single change (cache's supported)
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
