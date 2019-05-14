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

package com.github.vase4kin.teamcityapp.buildlog.view

/**
 * Handle view interactions of [BuildLogFragment]
 */
interface BuildLogView {

    /**
     * Init view
     *
     * @param listener - Listener to receive view callbacks
     */
    fun initViews(listener: OnBuildLogLoadListener)

    /**
     * Unbind views
     */
    fun unBindViews()

    /**
     * Load build log
     *
     * @param buildLogUrl - Build log url
     */
    fun loadBuildLog(buildLogUrl: String)

    /**
     * Show need auth view
     */
    fun showAuthView()

    /**
     * Hide need authDialog
     */
    fun hideAuthView()

    /**
     * Show ssl warning view
     */
    fun showSslWarningView()
}
