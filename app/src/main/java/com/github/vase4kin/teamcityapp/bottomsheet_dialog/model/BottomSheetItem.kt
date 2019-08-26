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

/**
 * Menu item of bottom sheet
 */
data class BottomSheetItem(
    /**
     * @return menu item type
     */
    val type: Int,
    /**
     * @return title
     */
    val title: String,
    /**
     * @return menu item description
     */
    val description: String,
    /**
     * @return icon
     */
    val icon: Drawable
) {
    companion object {

        const val TYPE_COPY = 0
        const val TYPE_BRANCH = 1
        const val TYPE_ARTIFACT_OPEN = 2
        const val TYPE_ARTIFACT_DOWNLOAD = 3
        const val TYPE_ARTIFACT_OPEN_IN_BROWSER = 4
        const val TYPE_BUILD_TYPE = 5
        const val TYPE_PROJECT = 6
    }
}
