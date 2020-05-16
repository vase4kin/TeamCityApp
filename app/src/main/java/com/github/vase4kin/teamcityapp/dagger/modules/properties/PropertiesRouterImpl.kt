/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.dagger.modules.properties

import androidx.appcompat.app.AppCompatActivity
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment
import teamcityapp.features.properties.feature.router.PropertiesRouter
import teamcityapp.features.properties.feature.view.PropertiesFragment

class PropertiesRouterImpl(
    private val fragment: PropertiesFragment
) : PropertiesRouter {

    override fun showCopyValueBottomSheet(title: String, value: String) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            title,
            value,
            MenuItemsFactory.TYPE_DEFAULT
        )
        bottomSheetDialogFragment.show(
            (fragment.requireActivity() as AppCompatActivity).supportFragmentManager,
            "Tag Bottom Sheet"
        )
    }
}