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

package com.github.vase4kin.teamcityapp.overview.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel

/**
 * Overview single item view holder
 */
class OverviewViewHolder(parent: ViewGroup) : BaseViewHolder<OverviewDataModel>(LayoutInflater.from(parent.context).inflate(R.layout.item_card_element_list, parent, false)) {
    @BindView(R.id.container)
    lateinit var frameLayout: FrameLayout
    @BindView(R.id.itemIcon)
    lateinit var icon: TextView
    @BindView(R.id.itemTitle)
    lateinit var description: TextView
    @BindView(R.id.itemHeader)
    lateinit var header: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: OverviewDataModel, position: Int) {
        icon.text = dataModel.getIcon(position)
        header.text = dataModel.getHeaderName(position)
        description.text = dataModel.getDescription(position)
    }
}
