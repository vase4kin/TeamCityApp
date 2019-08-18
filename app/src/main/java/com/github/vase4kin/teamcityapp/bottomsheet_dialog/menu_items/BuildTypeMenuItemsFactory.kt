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

import android.content.Context
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetItem
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.MaterialCommunityIcons

/**
 * Impl of [MenuItemsFactory] for build type menu
 */
class BuildTypeMenuItemsFactory(context: Context, descriptions: List<String>) :
    DefaultMenuItemsFactory(context, descriptions) {

    /**
     * {@inheritDoc}
     */
    override fun createMenuItems(): List<BottomSheetItem> {
        val list = super.createMenuItems().toMutableList()
        list.add(
            BottomSheetItem(
                BottomSheetItem.TYPE_BUILD_TYPE,
                getString(R.string.build_element_open_build_type),
                description,
                IconDrawable(context, MaterialCommunityIcons.mdi_open_in_app)
            )
        )
        return list
    }
}
