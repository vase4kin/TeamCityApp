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

package com.github.vase4kin.teamcityapp.navigation.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel

private const val PROJECT = "{md-filter-none}"
private const val BUILD_TYPE = "{md-crop-din}"

/**
 * Changes single item view holder
 */
class NavigationViewHolder(parent: ViewGroup) : BaseViewHolder<NavigationDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_with_title_and_sub_title_list,
        parent,
        false
    )
) {

    @BindView(R.id.container)
    lateinit var container: FrameLayout
    @BindView(R.id.itemTitle)
    lateinit var textView: TextView
    @BindView(R.id.itemSubTitle)
    lateinit var description: TextView
    @BindView(R.id.itemIcon)
    lateinit var icon: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: NavigationDataModel, position: Int) {
        textView.text = dataModel.getName(position)
        if (dataModel.hasDescription(position)) {
            description.text = dataModel.getDescription(position)
            description.visibility = View.VISIBLE
        } else {
            description.visibility = View.GONE
        }
        if (dataModel.isProject(position)) {
            icon.text = PROJECT
        } else {
            icon.text = BUILD_TYPE
        }
    }
}
