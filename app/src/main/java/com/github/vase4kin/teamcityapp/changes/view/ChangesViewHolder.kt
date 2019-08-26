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

package com.github.vase4kin.teamcityapp.changes.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel
import com.github.vase4kin.teamcityapp.utils.IconUtils

/**
 * Changes single item view holder
 */
class ChangesViewHolder(parent: ViewGroup) : BaseViewHolder<ChangesDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_changes_list,
        parent,
        false
    )
) {
    @BindView(R.id.container)
    lateinit var container: FrameLayout
    @BindView(R.id.itemSubTitle)
    lateinit var itemSubTitle: TextView
    @BindView(R.id.itemTitle)
    lateinit var itemTitle: TextView
    @BindView(R.id.itemIcon)
    lateinit var icon: TextView
    @BindView(R.id.userName)
    lateinit var userName: TextView
    @BindView(R.id.date)
    lateinit var date: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: ChangesDataModel, position: Int) {
        icon.text = IconUtils.getCountIcon(dataModel.getFilesCount(position))
        itemTitle.text = dataModel.getComment(position)
        userName.text = dataModel.getUserName(position)
        date.text = dataModel.getDate(position)
        itemSubTitle.text = dataModel.getVersion(position)
    }
}
