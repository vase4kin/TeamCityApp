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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetItem

/**
 * Menu items factory
 */
interface MenuItemsFactory {

    /**
     * @return list of menu items
     */
    fun createMenuItems(): List<BottomSheetItem>

    companion object {

        /**
         * Default menu type
         */
        const val TYPE_DEFAULT = 0

        /**
         * Branch menu type
         */
        const val TYPE_BRANCH = 1

        /**
         * Artifact default menu type
         */
        const val TYPE_ARTIFACT_DEFAULT = 2

        /**
         * Artifact browser menu type
         */
        const val TYPE_ARTIFACT_BROWSER = 3

        /**
         * Artifact folder menu type
         */
        const val TYPE_ARTIFACT_FOLDER = 4

        /**
         * Artifact full menu type
         */
        const val TYPE_ARTIFACT_FULL = 5

        /**
         * Build type menu type
         */
        const val TYPE_BUILD_TYPE = 6

        /**
         * Project menu type
         */
        const val TYPE_PROJECT = 7
    }
}
