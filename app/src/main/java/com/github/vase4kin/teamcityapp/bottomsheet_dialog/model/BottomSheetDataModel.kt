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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.model

import android.graphics.drawable.Drawable

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel

/**
 * Model for handling botton sheet data
 */
interface BottomSheetDataModel : BaseDataModel {

    /**
     * Get title of menu item
     *
     * @param position - adapter position
     * @return title
     */
    fun getTitle(position: Int): String

    /**
     * Get description
     *
     * @param position - adapter position
     * @return description
     */
    fun getDescription(position: Int): String

    /**
     * Get file name
     *
     * @param position - adapter position
     * @return description
     */
    fun getFileName(position: Int): String

    /**
     * Get icon for menu item
     *
     * @param position - adapter position
     * @return menu item icon
     */
    fun getIcon(position: Int): Drawable

    /**
     * Has copy action
     *
     * @param position - adapter position
     * @return action of menu item
     */
    fun hasCopyAction(position: Int): Boolean

    /**
     * Has branch action
     *
     * @param position - adapter position
     * @return action of menu item
     */
    fun hasBranchAction(position: Int): Boolean

    /**
     * Has build type action
     *
     * @param position - adapter position
     * @return action of menu item
     */
    fun hasBuildTypeAction(position: Int): Boolean

    /**
     * Has project action
     *
     * @param position - adapter position
     * @return action of menu item
     */
    fun hasProjectAction(position: Int): Boolean

    /**
     * Has artifact open action
     *
     * @param position - adapter position
     * @return action of menu item
     */
    fun hasArtifactOpenAction(position: Int): Boolean

    /**
     * Has artifact download action
     *
     * @param position - adapter position
     * @return action of menu item
     */
    fun hasArtifactDownloadAction(position: Int): Boolean

    /**
     * Has artifact open in browser action
     *
     * @param position - adapter position
     * @return action of menu item
     */
    fun hasArtifactOpenInBrowserAction(position: Int): Boolean
}
