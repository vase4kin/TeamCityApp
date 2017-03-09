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

package com.github.vase4kin.teamcityapp.runbuild.interactor;

import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.properties.api.Properties;

import java.util.List;

/**
 * Run build interactor
 */
public interface RunBuildInteractor {

    /**
     * Forbidden code error
     */
    int CODE_FORBIDDEN = 403;

    /**
     * Extra bundle key
     */
    String EXTRA_BUILD_TYPE_ID = "BuildTypeId";

    /**
     * Queue new build
     *
     * @param branchName      - with branch name
     * @param agent           - agent run with
     * @param isPersonal      - personal build
     * @param queueToTheTop   - queue build to the top
     * @param cleanAllFiles   - clean all files in the checkout directory
     * @param properties      - properties to use for new build
     * @param loadingListener - listener to receive callbacks
     */
    void queueBuild(String branchName,
                    @Nullable Agent agent,
                    boolean isPersonal,
                    boolean queueToTheTop,
                    boolean cleanAllFiles,
                    Properties properties,
                    LoadingListenerWithForbiddenSupport<String> loadingListener);

    /**
     * Queue new build with parameters
     *
     * @param branchName      - with branch name
     * @param properties      - properties to use for new build
     * @param loadingListener - listener to receive callbacks
     */
    void queueBuild(String branchName, Properties properties, LoadingListenerWithForbiddenSupport<String> loadingListener);

    /**
     * Unsubscribe all rx subscriptions
     */
    void unsubscribe();

    /**
     * Load list of agents
     *
     * @param loadingListener - listener to receive load callbacks
     */
    void loadAgents(OnLoadingListener<List<Agent>> loadingListener);
}
