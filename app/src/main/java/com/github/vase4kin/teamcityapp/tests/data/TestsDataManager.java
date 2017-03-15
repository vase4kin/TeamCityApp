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

package com.github.vase4kin.teamcityapp.tests.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import java.util.List;

/**
 * Data manager to manage data operations for {@link com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment}
 */
public interface TestsDataManager extends BaseListRxDataManager<TestOccurrences, TestOccurrences.TestOccurrence> {

    /**
     * Load tests
     *
     * @param url             - Tests url
     * @param update          - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    void load(@NonNull String url,
              @NonNull OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener,
              boolean update);

    /**
     * Load failed tests url
     *
     * @param url             - Failed tests url
     * @param update          - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    void loadFailedTests(@NonNull String url,
                         @NonNull OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener,
                         boolean update);

    /**
     * Load failed tests
     *
     * @param url - Failed tests url
     */
    void loadFailedTests(@NonNull String url);

    /**
     * Load ignored tests
     *
     * @param url - Ignored tests url
     */
    void loadIgnoredTests(@NonNull String url);

    /**
     * Load passed tests
     *
     * @param url- Passed tests url
     */
    void loadPassedTests(@NonNull String url);

    /**
     *
     * @param url
     * @param loadingListener - Listener to receive server callbacks
     */
    void loadTestDetails(@NonNull String url, @NonNull OnLoadingListener<Integer> loadingListener);

    /**
     * Load more builds
     *
     * @param loadingListener - Listener to receive server callbacks
     */
    void loadMore(@NonNull OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener);

    /**
     * @return Boolean which indicates can load more builds or not
     */
    boolean canLoadMore();

    /**
     * Post change tab title event
     *
     * @param size - Items size
     */
    void postChangeTabTitleEvent(Integer size);
}
