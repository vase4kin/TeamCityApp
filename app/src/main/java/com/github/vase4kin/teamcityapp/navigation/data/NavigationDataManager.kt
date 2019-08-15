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

package com.github.vase4kin.teamcityapp.navigation.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode

/**
 * Data manager to handle navigation server interactions
 */
interface NavigationDataManager : BaseListRxDataManager<NavigationNode, NavigationItem> {

    /**
     * Load navigation items
     *
     * @param id             - Navigation node id
     * @param update          - Force data update
     * @param loadingListener - Listener to receive server callbacks
     */
    fun load(id: String, update: Boolean, loadingListener: OnLoadingListener<List<NavigationItem>>)

    /**
     * @return {true} if we need to show to the user rate the app dialog
     */
    fun showRateTheApp(): Boolean

    /**
     * Save state
     */
    fun saveRateCancelClickedOn()

    /**
     * Save state
     */
    fun saveRateNowClickedOn()
}
