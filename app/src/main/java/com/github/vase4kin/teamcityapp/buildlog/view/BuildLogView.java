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

package com.github.vase4kin.teamcityapp.buildlog.view;

/**
 * Handle view interactions of {@link BuildLogFragment}
 */
public interface BuildLogView {

    /**
     * Init view
     *
     * @param listener - Listener to receive view callbacks
     */
    void initViews(final OnBuildLogLoadListener listener);

    /**
     * Unbind views
     */
    void unBindViews();

    /**
     * Load build log
     *
     * @param buildLogUrl - Build log url
     */
    void loadBuildLog(String buildLogUrl);

    /**
     * Show need auth view
     */
    void showAuthView();

    /**
     * Hide need authDialog
     */
    void hideAuthView();

    /**
     * Show ssl warning view
     */
    void showSslWarningView();
}
