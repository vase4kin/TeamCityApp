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

package com.github.vase4kin.teamcityapp.tests.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import teamcityapp.features.test_details.api.models.TestOccurrence

/**
 * Data manager to manage data operations for [com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment]
 */
interface TestsDataManager : BaseListRxDataManager<TestOccurrences, TestOccurrence> {

    /**
     * Load tests
     *
     * @param url - Tests url
     * @param update - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    fun load(
        url: String,
        loadingListener: OnLoadingListener<List<TestOccurrence>>,
        update: Boolean
    )

    /**
     * Load failed tests url
     *
     * @param url - Failed tests url
     * @param update - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadFailedTests(
        url: String,
        loadingListener: OnLoadingListener<List<TestOccurrence>>,
        update: Boolean
    )

    /**
     * Load failed tests
     *
     * @param url - Failed tests url
     */
    fun loadFailedTests(url: String)

    /**
     * Load ignored tests
     *
     * @param url - Ignored tests url
     */
    fun loadIgnoredTests(url: String)

    /**
     * Load passed tests
     *
     * @param url- Passed tests url
     */
    fun loadPassedTests(url: String)

    /**
     *
     * @param url
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadTestDetails(url: String, loadingListener: OnLoadingListener<Int>)

    /**
     * Load more builds
     *
     * @param loadingListener - Listener to receive server callbacks
     */
    fun loadMore(loadingListener: OnLoadingListener<List<TestOccurrence>>)

    /**
     * @return Boolean which indicates can load more builds or not
     */
    fun canLoadMore(): Boolean

    /**
     * Post change tab title event
     *
     * @param size - Items size
     */
    fun postChangeTabTitleEvent(size: Int)
}
