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

package com.github.vase4kin.teamcityapp.navigation.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;

import java.util.List;

/**
 * Data manager to handle navigation server interactions
 */
public interface NavigationDataManager extends BaseListRxDataManager<NavigationNode, NavigationItem> {

    /**
     * Load navigation items
     *
     * @param id             - Navigation node id
     * @param update          - Force data update
     * @param loadingListener - Listener to receive server callbacks
     */
    void load(@NonNull String id, boolean update, @NonNull OnLoadingListener<List<NavigationItem>> loadingListener);

    /**
     * @return {true} if we need to show to the user rate the app dialog
     */
    boolean showRateTheApp();

    /**
     * Save state
     */
    void saveRateLaterClickedOn();

    /**
     * Save state
     */
    void saveRateNowClickedOn();
}
