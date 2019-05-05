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
 * Build log view listener
 */
interface OnBuildLogViewListener {

    /**
     * Show build log web view
     */
    fun showWebView()

    /**
     * Hide build log web view
     */
    fun hideWebView()

    /**
     * Show progress loading
     */
    fun showProgressWheel()

    /**
     * Hide progress loading
     */
    fun hideProgressWheel()

    /**
     * Show error view
     */
    fun showError()

    /**
     * Hide error view
     */
    fun hideError()

    /**
     * Evaluate js script
     *
     * @param script - Script
     */
    fun evaluateScript(script: String)
}
