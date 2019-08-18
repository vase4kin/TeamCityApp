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
import androidx.annotation.StringRes
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetItem

/**
 * Impl of [MenuItemsFactory]
 */
abstract class BaseMenuItemsFactory constructor(
    val context: Context,
    private val descriptions: List<String>
) : MenuItemsFactory {

    val description: String
        get() = descriptions[0]

    abstract override fun createMenuItems(): List<BottomSheetItem>

    protected fun getString(@StringRes title: Int): String {
        return context.getString(title)
    }

    fun getDescription(position: Int): String {
        return descriptions[position]
    }
}
