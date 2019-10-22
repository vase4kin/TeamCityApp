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
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel

/**
 * Overview single item view holder
 */
class OverviewViewHolder(parent: ViewGroup) : BaseViewHolder<OverviewDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_build_overview_list,
        parent,
        false
    )
) {
    @BindView(R.id.image)
    lateinit var icon: ImageView
    @BindView(R.id.progress)
    lateinit var progress: ProgressBar
    @BindView(R.id.title)
    lateinit var header: TextView
    @BindView(R.id.subTitle)
    lateinit var description: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: OverviewDataModel, position: Int) {
        if (dataModel.isRunning(position)) {
            icon.visibility = View.GONE
            progress.visibility = View.VISIBLE
        } else {
            icon.visibility = View.VISIBLE
            progress.visibility = View.GONE
            icon.setImageResource(dataModel.getIcon(position))
        }
        header.text = dataModel.getHeaderName(position)
        description.text = dataModel.getDescription(position)
    }
}
