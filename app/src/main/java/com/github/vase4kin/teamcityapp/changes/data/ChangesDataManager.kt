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

package com.github.vase4kin.teamcityapp.changes.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager
import com.github.vase4kin.teamcityapp.changes.api.Changes

/**
 * Data manager for [com.github.vase4kin.teamcityapp.changes.view.ChangesFragment]
 */
interface ChangesDataManager : BaseListRxDataManager<Changes, Changes.Change> {

    /**
     * Load tab title with number of changes
     *
     * @param url - Url to load
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadTabTitle(url: String, loadingListener: OnLoadingListener<Int>)

    /**
     * {@inheritDoc}
     */
    fun load(
        url: String,
        loadingListener: OnLoadingListener<List<Changes.Change>>,
        update: Boolean
    )

    /**
     * Load limited number of changes (10)
     *
     * @param url - Url to load
     * @param update - force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadLimited(
        url: String,
        loadingListener: OnLoadingListener<List<Changes.Change>>,
        update: Boolean
    )

    /**
     * Load more changes
     *
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadMore(loadingListener: OnLoadingListener<List<Changes.Change>>)

    /**
     * Is it possible to load more
     */
    fun canLoadMore(): Boolean

    /**
     * Post change tab title event to [de.greenrobot.event.EventBus]
     * @param size - Size of items to update in the title
     */
    fun postChangeTabTitleEvent(size: Int?)
}
