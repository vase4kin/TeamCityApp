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
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel

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
    @BindView(R.id.commitHash)
    lateinit var commitHash: TextView
    @BindView(R.id.commitName)
    lateinit var commitName: TextView
    @BindView(R.id.image)
    lateinit var image: ImageView
    @BindView(R.id.userName)
    lateinit var userName: TextView
    @BindView(R.id.commitDate)
    lateinit var commitDate: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: ChangesDataModel, position: Int) {
        val filesCount = dataModel.getFilesCount(position)
        val iconRes = getCountIcon(filesCount)
        image.setImageResource(iconRes)
        commitName.text = dataModel.getComment(position)
        userName.text = dataModel.getUserName(position)
        commitDate.text = dataModel.getDate(position)
        commitHash.text = dataModel.getVersion(position)
    }

    private fun getCountIcon(count: Int): Int {
        return when (count) {
            0 -> R.drawable.ic_filter_none_black_24dp
            1 -> R.drawable.ic_filter_1_black_24dp
            2 -> R.drawable.ic_filter_2_black_24dp
            3 -> R.drawable.ic_filter_3_black_24dp
            4 -> R.drawable.ic_filter_4_black_24dp
            5 -> R.drawable.ic_filter_5_black_24dp
            6 -> R.drawable.ic_filter_6_black_24dp
            7 -> R.drawable.ic_filter_7_black_24dp
            8 -> R.drawable.ic_filter_8_black_24dp
            9 -> R.drawable.ic_filter_9_black_24dp
            10 -> R.drawable.ic_filter_9_plus_black_24dp
            else -> R.drawable.ic_filter_9_plus_black_24dp
        }
    }
}
