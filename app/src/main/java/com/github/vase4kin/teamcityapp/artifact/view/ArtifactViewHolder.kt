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

package com.github.vase4kin.teamcityapp.artifact.view

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.joanzapata.iconify.widget.IconTextView

private const val FILE_ICON = "{mdi-file}"
private const val FOLDER_ICON = "{mdi-folder}"

class ArtifactViewHolder(parent: ViewGroup) : BaseViewHolder<ArtifactDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_artifact_list,
        parent,
        false
    )
) {

    @BindView(R.id.container)
    lateinit var container: FrameLayout
    @BindView(R.id.itemIcon)
    lateinit var icon: IconTextView
    @BindView(R.id.itemTitle)
    lateinit var fileName: TextView
    @BindView(R.id.itemSubTitle)
    lateinit var size: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: ArtifactDataModel, position: Int) {
        val artifactFile = dataModel.getFile(position)
        if (artifactFile.children != null) {
            icon.text = FOLDER_ICON
        } else {
            icon.text = FILE_ICON
        }
        fileName.text = artifactFile.name
        if (dataModel.hasSize(position)) {
            size.text = Formatter.formatFileSize(size.context, artifactFile.size)
        } else {
            size.visibility = View.GONE
        }
    }
}
