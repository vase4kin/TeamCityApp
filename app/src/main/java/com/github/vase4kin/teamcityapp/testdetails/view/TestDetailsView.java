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

package com.github.vase4kin.teamcityapp.testdetails.view;

import tr.xip.errorview.ErrorView;

/**
 * View to manage interactions of {@link TestDetailsActivity}
 */
public interface TestDetailsView {

    /**
     * Init views
     *
     * @param retryListener - Listener to handle on retry callback
     */
    void initViews(ErrorView.RetryListener retryListener);

    /**
     * Show loading progress
     */
    void showProgress();

    /**
     * Hide loading progress
     */
    void hideProgress();

    /**
     * Set views after loading
     *
     * @param testDetails - Test details data
     */
    void showTestDetails(String testDetails);

    /**
     * Show empty data message if no test details is available
     */
    void showEmptyData();

    /**
     * Show error view
     *
     * @param errorMessage - Error message to show
     */
    void showRetryView(String errorMessage);

    /**
     * Unbind views
     */
    void unBindViews();

    /**
     * Finish activity
     */
    void finish();
}
