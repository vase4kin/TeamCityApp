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

package com.github.vase4kin.teamcityapp.artifact.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager

/**
 * Data manager for [ArtifactDataManager]
 */
interface ArtifactDataManager : BaseListRxDataManager<Files, File> {

    /**
     * {@inheritDoc}
     */
    fun load(
        url: String,
        loadingListener: OnLoadingListener<List<File>>,
        update: Boolean
    )

    /**
     * Download artifact
     *
     * @param url - Artifact url
     * @param name - Artifact name
     * @param loadingListener - Listener to receive server callbacks
     */
    fun downloadArtifact(url: String, name: String, loadingListener: OnLoadingListener<java.io.File>)

    /**
     * Check if file is apk file
     */
    fun isTheFileApk(fileName: String): Boolean

    /**
     * Register event bus
     */
    fun registerEventBus()

    /**
     * Unregister event bus
     */
    fun unregisterEventBus()

    /**
     * @param listener - Listener
     */
    fun setListener(listener: OnArtifactEventListener?)

    /**
     * Post [ArtifactErrorDownloadingEvent]
     */
    fun postArtifactErrorDownloadingEvent()
}
