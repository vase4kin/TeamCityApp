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

package com.github.vase4kin.teamcityapp.build_details.data

/**
 * Listener to handle events
 */
interface OnBuildDetailsEventsListener {

    /**
     * Fab is visible
     */
    fun onShow()

    /**
     * Fab is hidden
     */
    fun onHide()

    /**
     * When cancel/remove from queue build action is triggered
     */
    fun onCancelBuildActionTriggered()

    /**
     * When share build action is triggered
     */
    fun onShareBuildActionTriggered()

    /**
     * When restart build action is triggered
     */
    fun onRestartBuildActionTriggered()

    /**
     * When text copied action is triggered
     */
    fun onTextCopiedActionTriggered()

    /**
     * When error downloading artifact action is triggered
     */
    fun onErrorDownloadingArtifactActionTriggered()

    /**
     * When start build list activity filtered by branch event is triggered
     */
    fun onStartBuildListActivityFilteredByBranchEventTriggered(branchName: String)

    /**
     * When start build list activity event is triggered
     */
    fun onStartBuildListActivityEventTriggered()

    /**
     * When start project activity event is triggered
     */
    fun onStartProjectActivityEventTriggered()
}
