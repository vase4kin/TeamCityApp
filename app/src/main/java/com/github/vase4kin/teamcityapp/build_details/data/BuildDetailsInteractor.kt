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

package com.github.vase4kin.teamcityapp.build_details.data

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport

/**
 * Interactor for [BuildDetailsActivity]
 */
interface BuildDetailsInteractor : BaseTabsDataManager {

    /**
     * @return {true} if the build was triggered by active user
     */
    fun isBuildTriggeredByMe(): Boolean

    /**
     * @return build details
     */
    fun getBuildDetails(): BuildDetails

    /**
     * @return build type name
     */
    fun getBuildTypeName(): String

    /**
     * @return project id
     */
    fun getProjectId(): String

    /**
     * @return project name
     */
    fun getProjectName(): String

    /**
     * return {String} web url to share build
     */
    fun getWebUrl(): String

    /**
     * Set listener to handle floating action button visibility states
     *
     * @param listener - Listener to receive callbacks
     */
    fun setOnBuildTabsEventsListener(listener: OnBuildDetailsEventsListener)

    /**
     * Post [OnOverviewRefreshDataEvent]
     */
    fun postRefreshOverViewDataEvent()

    /**
     * Cancel/Remove from queue the build
     *
     * @param loadingListener   - Listener to receive callbacks on UI
     * @param isReAddToTheQueue - Re-add build to the queue flag
     */
    fun cancelBuild(loadingListener: LoadingListenerWithForbiddenSupport<Build>, isReAddToTheQueue: Boolean)

    /**
     * Unsubscribe Rx subscriptions
     */
    fun unsubsribe()
}
