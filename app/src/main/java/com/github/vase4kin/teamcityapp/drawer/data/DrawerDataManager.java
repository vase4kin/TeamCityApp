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

package com.github.vase4kin.teamcityapp.drawer.data;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.List;

/**
 * Data manager for drawer
 */
public interface DrawerDataManager {

    /**
     * {@inheritDoc}
     */
    void load(OnLoadingListener<List<UserAccount>> loadingListener);

    /**
     * Set user account as active
     *
     * @param url - User with this TC url
     * @param userName - user name
     */
    void setActiveUser(String url, String userName);

    /**
     * @param url - TC url of the user account
     * @param userName - user name
     */
    boolean isActiveUser(String url, String userName);

    /**
     * Load the number of running builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    void loadRunningBuildsCount(OnLoadingListener<Integer> loadingListener);

    /**
     * Load the number of connected agents
     *
     * @param loadingListener - Listener to receive callbacks
     */
    void loadConnectedAgentsCount(OnLoadingListener<Integer> loadingListener);

    /**
     * Load the number queued builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    void loadBuildQueueCount(OnLoadingListener<Integer> loadingListener);
}
