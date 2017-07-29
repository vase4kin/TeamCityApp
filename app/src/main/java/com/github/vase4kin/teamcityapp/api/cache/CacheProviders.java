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

package com.github.vase4kin.teamcityapp.api.cache;

import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import java.util.concurrent.TimeUnit;

import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.LifeCache;
import rx.Observable;

/**
 * Cache providers
 */
public interface CacheProviders {

    /**
     * Cache build types for one day
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.DAYS)
    Observable<NavigationNode> listBuildTypes(Observable<NavigationNode> navigationNodeObservable,
                                              DynamicKey buildTypeUrl,
                                              EvictDynamicKey evictDynamicKey);

    /**
     * Cache build type for 1 day
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.DAYS)
    Observable<BuildType> buildType(Observable<BuildType> buildTypeObservable,
                                    DynamicKey buildTypeId,
                                    EvictDynamicKey evictDynamicKey);

    /**
     * Cache branches for one minute
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    Observable<Branches> listBranches(Observable<Branches> branchesObservable,
                                      DynamicKey buildTypeId);

    /**
     * Cache changes list for three minute
     */
    @LifeCache(duration = 3, timeUnit = TimeUnit.MINUTES)
    Observable<Changes> listChanges(Observable<Changes> changesObservable,
                                    DynamicKey changesUrl,
                                    EvictDynamicKey evictDynamicKey);

    /**
     * Cache single change for one hour
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    Observable<Changes.Change> change(Observable<Changes.Change> changeObservable,
                                      DynamicKey changeUrl);

    /**
     * Cache test list for three minute
     */
    @LifeCache(duration = 3, timeUnit = TimeUnit.MINUTES)
    Observable<TestOccurrences> listTestOccurrences(Observable<TestOccurrences> occurrencesObservable,
                                                    DynamicKey testsUrl,
                                                    EvictDynamicKey evictDynamicKey);

    /**
     * Cache single test for one hour
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    Observable<TestOccurrences.TestOccurrence> testOccurrence(Observable<TestOccurrences.TestOccurrence> testOccurrenceObservable,
                                                              DynamicKey testUrl);

    /**
     * Cache artifacts for one hour
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.HOURS)
    Observable<Files> listArtifacts(Observable<Files> filesObservable,
                                    DynamicKey artifactsUrl,
                                    EvictDynamicKey evictDynamicKey);

    /**
     * Cache build list for 1 minutes
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    Observable<Builds> listBuilds(Observable<Builds> buildsObservable,
                                  DynamicKeyGroup buildsInfo,
                                  EvictDynamicKeyGroup evictDynamicKeyGroup);

    /**
     * Cache build list for 10 minutes (we cache only builds which have finished state)
     */
    @LifeCache(duration = 10, timeUnit = TimeUnit.MINUTES)
    Observable<Build> build(Observable<Build> buildObservable,
                            DynamicKey buildUrl,
                            EvictDynamicKey evictDynamicKey);

    /**
     * Cache running build list for 1 minutes
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    Observable<Builds> listRunningBuilds(Observable<Builds> buildObservable,
                                         DynamicKey locatorPlusFields,
                                         EvictDynamicKey evictDynamicKey);

    /**
     * Cache queued build list for 1 minutes
     */
    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    Observable<Builds> listQueuedBuilds(Observable<Builds> buildObservable,
                                        DynamicKey fields,
                                        EvictDynamicKey evictDynamicKey);

    /**
     * Cache agent list for 3 minutes
     */
    @LifeCache(duration = 3, timeUnit = TimeUnit.MINUTES)
    Observable<Agents> listAgents(Observable<Agents> agentsObservable,
                                  DynamicKey includeDisconnectedPlusFields,
                                  EvictDynamicKey evictDynamicKey);
}
