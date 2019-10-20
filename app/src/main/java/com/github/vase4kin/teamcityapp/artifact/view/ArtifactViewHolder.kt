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
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder

class ArtifactViewHolder(parent: ViewGroup) : BaseViewHolder<ArtifactDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_artifact_list,
        parent,
        false
    )
) {

    @BindView(R.id.image)
    lateinit var image: ImageView
    @BindView(R.id.title)
    lateinit var fileName: TextView
    @BindView(R.id.subTitle)
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
            image.setImageResource(R.drawable.ic_folder_black_24dp)
        } else {
            image.setImageResource(R.drawable.ic_insert_drive_file_black_24dp)
        }
        fileName.text = artifactFile.name
        if (dataModel.hasSize(position)) {
            size.text = Formatter.formatFileSize(size.context, artifactFile.size)
        } else {
            size.visibility = View.GONE
        }
    }
}
