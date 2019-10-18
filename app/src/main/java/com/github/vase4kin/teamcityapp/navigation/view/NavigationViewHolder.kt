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
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel

/**
 * Changes single item view holder
 */
class NavigationViewHolder(parent: ViewGroup) : BaseViewHolder<NavigationDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_project_configuration_list,
        parent,
        false
    )
) {

    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subTitle)
    lateinit var description: TextView
    @BindView(R.id.image)
    lateinit var image: ImageView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: NavigationDataModel, position: Int) {
        title.text = dataModel.getName(position)
        if (dataModel.hasDescription(position)) {
            description.text = dataModel.getDescription(position)
            description.visibility = View.VISIBLE
        } else {
            description.visibility = View.GONE
        }
        if (dataModel.isProject(position)) {
            image.setImageResource(R.drawable.ic_filter_none_black_24dp)
        } else {
            image.setImageResource(R.drawable.ic_crop_din_black_24dp)
        }
    }
}
