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

package com.github.vase4kin.teamcityapp.overview.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment

/**
 * Data manager for [OverviewFragment]
 */
interface OverViewInteractor : BaseListRxDataManager<Build, BuildElement> {

    /**
     * @return [BuildDetails] passed through intent
     */
    val buildDetails: BuildDetails

    /**
     * Set listener to handle receiving events
     *
     * @param listener - Listener
     */
    fun setListener(listener: OnOverviewEventsListener)

    /**
     * Load build details
     *
     * @param url             - Build url
     * @param update          - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    fun load(url: String,
             loadingListener: OnLoadingListener<BuildDetails>,
             update: Boolean)

    /**
     * Post [StopBuildEvent]
     */
    fun postStopBuildEvent()

    /**
     * Post [ShareBuildEvent]
     */
    fun postShareBuildInfoEvent()

    /**
     * Post [RestartBuildEvent]
     */
    fun postRestartBuildEvent()

    /**
     * Subscribe to event bus events
     */
    fun subscribeToEventBusEvents()

    /**
     * Unsubscribe to event bus events
     */
    fun unsubsribeFromEventBusEvents()

    /**
     * Post [FloatButtonChangeVisibilityEvent] GONE event
     */
    fun postFABGoneEvent()

    /**
     * Post [FloatButtonChangeVisibilityEvent] VISIBLE event
     */
    fun postFABVisibleEvent()

    /**
     * Post [StartBuildsListActivityFilteredByBranchEvent]
     *
     * @param branchName - branch name
     */
    fun postStartBuildListActivityFilteredByBranchEvent(branchName: String)

    /**
     * Post [StartBuildsListActivityEvent]
     */
    fun postStartBuildListActivityEvent()

    /**
     * Post [StartProjectActivityEvent]
     */
    fun postStartProjectActivityEvent()

    /**
     * Event listener
     */
    interface OnOverviewEventsListener {
        /**
         * On post data refresh event
         */
        fun onDataRefreshEvent()

        /**
         * On post [NavigateToBuildListFilteredByBranchEvent]
         */
        fun onNavigateToBuildListEvent(branchName: String)

        /**
         * On post [NavigateToBuildListEvent]
         */
        fun onNavigateToBuildListEvent()

        /**
         * On post [NavigateToProjectEvent]
         */
        fun onNavigateToProjectEvent()
    }

}
