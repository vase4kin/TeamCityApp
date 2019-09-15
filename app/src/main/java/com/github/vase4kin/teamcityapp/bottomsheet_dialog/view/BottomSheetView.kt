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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.view

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel

/**
 * View for bottom sheet
 */
interface BottomSheetView {

    /**
     * Init views
     */
    fun initViews(listener: OnBottomSheetClickListener, dataModel: BottomSheetDataModel, title: String)

    /**
     * Unbind views
     */
    fun unbindViews()

    /**
     * Close bottom sheet
     */
    fun close()

    /**
     * Click listener
     */
    interface OnBottomSheetClickListener {
        /**
         * On copy action click
         *
         * @param text - text to copy
         */
        fun onCopyActionClick(text: String)

        /**
         * On show builds built on this branch click
         *
         * @param branch - branch name
         */
        fun onShowBuildsActionClick(branch: String)

        /**
         * On show build type click
         */
        fun onShowBuildTypeActionClick()

        /**
         * On show project click
         */
        fun onShowProjectActionClick()

        /**
         * On artifact download click
         *
         * @param fileName - file name
         * @param href - href
         */
        fun onArtifactDownloadActionClick(fileName: String, href: String)

        /**
         * On artifact open click
         *
         * @param href - file href
         */
        fun onArtifactOpenActionClick(fileName: String, href: String)

        /**
         * On artifact open in browser click
         *
         * @param href - file href
         */
        fun onArtifactBrowserOpenActionClick(href: String)
    }
}
