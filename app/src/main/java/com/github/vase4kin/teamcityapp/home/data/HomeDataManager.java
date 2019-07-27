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

package com.github.vase4kin.teamcityapp.home.data;

import androidx.annotation.Nullable;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.Filter;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

/**
 * Data manager to handle root data operations
 */
public interface HomeDataManager extends DrawerDataManager {

    /**
     * @return Active user account
     */
    UserAccount getActiveUser();

    /**
     * Init TC rest service
     */
    void initTeamCityService();

    /**
     * Clear all webview cookies
     */
    void clearAllWebViewCookies();

    /**
     * Evict all cache data
     */
    void evictAllCache();

    /**
     * Set listener
     */
    void setListener(@Nullable Listener listener);

    /**
     * Subscribe to event bus events
     */
    void subscribeToEventBusEvents();

    /**
     * Unsubsribe to event bus events
     */
    void unsubscribeOfEventBusEvents();

    /**
     * Load the number of running builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    void loadRunningBuildsCount(OnLoadingListener<Integer> loadingListener);

    /**
     * Load the number of favorite running builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    void loadFavoriteRunningBuildsCount(OnLoadingListener<Integer> loadingListener);

    /**
     * Load the number queued builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    void loadBuildQueueCount(OnLoadingListener<Integer> loadingListener);

    /**
     * Load the number favorite queued builds
     *
     * @param loadingListener - Listener to receive callbacks
     */
    void loadFavoriteBuildQueueCount(OnLoadingListener<Integer> loadingListener);

    /**
     * @return the count of favorite build types
     */
    int getFavoritesCount();

    interface Listener {
        void onFilterApplied(Filter filter);
    }
}
