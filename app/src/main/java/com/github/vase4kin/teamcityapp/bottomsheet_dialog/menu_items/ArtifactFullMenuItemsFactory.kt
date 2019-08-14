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
import com.joanzapata.iconify.fonts.MaterialIcons
import java.util.*

/**
 * Impl of [MenuItemsFactory] for branch menu
 */
class ArtifactFullMenuItemsFactory(context: Context, descriptions: List<String>) : BaseMenuItemsFactory(context, descriptions) {

    /**
     * {@inheritDoc}
     */
    override fun createMenuItems(): List<BottomSheetItem> {
        val list = ArrayList<BottomSheetItem>()
        list.add(BottomSheetItem(BottomSheetItem.TYPE_ARTIFACT_DOWNLOAD, getString(R.string.artifact_download), getDescription(0), IconDrawable(context, MaterialIcons.md_file_download)))
        list.add(BottomSheetItem(BottomSheetItem.TYPE_ARTIFACT_OPEN, getString(R.string.artifact_open), getDescription(1), IconDrawable(context, MaterialIcons.md_open_in_new)))
        return list
    }
}
