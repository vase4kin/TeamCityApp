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

package com.github.vase4kin.teamcityapp.agents.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.joanzapata.iconify.widget.IconTextView

/**
 * Agent view holder
 */
class AgentViewHolder(
    parent: ViewGroup
) : BaseViewHolder<AgentDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_with_title_list,
        parent,
        false
    )
) {

    @BindView(R.id.itemTitle)
    lateinit var textView: TextView
    @BindView(R.id.itemIcon)
    lateinit var icon: IconTextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: AgentDataModel, position: Int) {
        val name = dataModel.getName(position)
        textView.text = name
        icon.text = ICON
    }

    companion object {
        private const val ICON = "{md-directions-railway}"
    }
}
