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

import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory

/**
 * Adapter for artifact files
 */
class ArtifactAdapter
/**
 * Constructor
 *
 * @param viewHolderFactories - view holder factories from DI
 */
    (viewHolderFactories: Map<Int, ViewHolderFactory<ArtifactDataModel>>) :
    BaseAdapter<ArtifactDataModel>(viewHolderFactories) {

    private var onClickListener: OnArtifactPresenterListener? = null

    /**
     * Set [OnArtifactPresenterListener]
     *
     * @param onArtifactPresenterListener - listener to set
     */
    fun setOnClickListener(onArtifactPresenterListener: OnArtifactPresenterListener) {
        this.onClickListener = onArtifactPresenterListener
    }

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: BaseViewHolder<ArtifactDataModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        val artifactFile = dataModel.getFile(position)
        // Find the way how to make it through DI
        (holder as ArtifactViewHolder).itemView.setOnClickListener { onClickListener?.onClick(artifactFile) }
        holder.itemView.setOnLongClickListener {
            onClickListener?.onLongClick(artifactFile)
            true
        }
    }
}
