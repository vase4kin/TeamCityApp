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

package com.github.vase4kin.teamcityapp.properties.view

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel

/**
 * View to manage properties interactions
 */
class PropertiesViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: PropertiesAdapter
) : BaseListViewImpl<PropertiesDataModel, PropertiesAdapter>(view, activity, emptyMessage, adapter),
    PropertiesView {

    private var listener: PropertiesView.Listener? = null

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: PropertiesDataModel) {
        adapter.dataModel = dataModel
        adapter.listener = listener
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.properties_recycler_view
    }

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: PropertiesView.Listener) {
        this.listener = listener
    }

    override fun showCopyValueBottomSheet(title: String, value: String) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            title,
            value,
            MenuItemsFactory.TYPE_DEFAULT
        )
        bottomSheetDialogFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            "Tag Bottom Sheet"
        )
    }
}
