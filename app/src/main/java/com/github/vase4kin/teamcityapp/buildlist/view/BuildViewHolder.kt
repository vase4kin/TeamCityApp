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

package com.github.vase4kin.teamcityapp.buildlist.view

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import teamcityapp.libraries.utils.getThemeColor

/**
 * Changes single item view holder
 */
class BuildViewHolder(parent: ViewGroup) : BaseViewHolder<BuildListDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_build_list,
        parent,
        false
    )
) {
    @BindView(R.id.branchName)
    lateinit var branchName: TextView

    @BindView(R.id.buildStatus)
    lateinit var buildStatus: TextView

    @BindView(R.id.buildStatusImage)
    lateinit var buildStatusImage: ImageView

    @BindView(R.id.buildNumber)
    lateinit var buildNumber: TextView

    @BindView(R.id.isPersonal)
    lateinit var isPersonalImage: View

    @BindView(R.id.isPinned)
    lateinit var isPinnedImage: View

    @BindView(R.id.buildStatusProgress)
    lateinit var buildStatusProgress: ProgressBar

    init {
        ButterKnife.bind(this, itemView)
    }

    override fun bind(dataModel: BuildListDataModel, position: Int) {
        if (dataModel.isRunning(position)) {
            buildStatusProgress.visibility = View.VISIBLE
            buildStatusImage.visibility = View.GONE
        } else {
            buildStatusProgress.visibility = View.GONE
            buildStatusImage.visibility = View.VISIBLE
            val iconImageRes = dataModel.getBuildStatusIcon(position)
            buildStatusImage.setImageResource(iconImageRes)
            when {
                dataModel.isSuccess(position) -> {
                    val color = itemView.context.getThemeColor(R.attr.colorSuccessState)
                    ImageViewCompat.setImageTintList(
                        buildStatusImage,
                        ColorStateList.valueOf(color)
                    )
                }
                dataModel.isFailed(position) -> {
                    val color = itemView.context.getThemeColor(R.attr.colorFailedState)
                    ImageViewCompat.setImageTintList(
                        buildStatusImage,
                        ColorStateList.valueOf(color)
                    )
                }
                else -> {
                    val color = ContextCompat.getColor(
                        itemView.context,
                        R.color.material_on_surface_emphasis_high_type
                    )
                    ImageViewCompat.setImageTintList(
                        buildStatusImage,
                        ColorStateList.valueOf(color)
                    )
                }
            }
        }
        buildStatus.text = dataModel.getStatusText(position)
        val buildNumber = dataModel.getBuildNumber(position)
        if (buildNumber.isEmpty()) {
            this.buildNumber.visibility = View.GONE
        } else {
            this.buildNumber.text = dataModel.getBuildNumber(position)
            this.buildNumber.visibility = View.VISIBLE
        }
        if (dataModel.isPersonal(position)) {
            isPersonalImage.visibility = View.VISIBLE
        } else {
            isPersonalImage.visibility = View.GONE
        }
        if (dataModel.isPinned(position)) {
            isPinnedImage.visibility = View.VISIBLE
        } else {
            isPinnedImage.visibility = View.GONE
        }
        val branchNameValue = dataModel.getBranchName(position)
        if (branchNameValue.isNotEmpty()) {
            branchName.text = branchNameValue
            branchName.visibility = View.VISIBLE
        } else {
            branchName.visibility = View.GONE
        }
    }
}
