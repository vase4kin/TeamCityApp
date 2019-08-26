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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel

/**
 * Bottom sheet menu item view holder
 */
internal class BottomSheetItemViewHolder(parent: ViewGroup) : BaseViewHolder<BottomSheetDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_bottom_sheet,
        parent,
        false
    )
) {

    @BindView(R.id.bs_title)
    lateinit var title: TextView
    @BindView(R.id.bs_image)
    lateinit var icon: ImageView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: BottomSheetDataModel, position: Int) {
        val menuTitle = dataModel.getTitle(position)
        val menuIcon = dataModel.getIcon(position)
        title.text = menuTitle
        icon.setImageDrawable(menuIcon)
    }
}
