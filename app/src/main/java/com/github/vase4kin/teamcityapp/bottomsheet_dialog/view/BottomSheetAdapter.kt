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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.view

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel

/**
 * Adapter to manage bottom sheet items
 */
class BottomSheetAdapter(viewHolderFactories: Map<Int, ViewHolderFactory<BottomSheetDataModel>>) :
    BaseAdapter<BottomSheetDataModel>(viewHolderFactories) {

    var listener: BottomSheetView.OnBottomSheetClickListener? = null

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: BaseViewHolder<BottomSheetDataModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        val fileName = dataModel.getFileName(position)
        val description = dataModel.getDescription(position)
        if (dataModel.hasCopyAction(position)) {
            holder.itemView.setOnClickListener { listener?.onCopyActionClick(description) }
        }
        if (dataModel.hasBranchAction(position)) {
            holder.itemView.setOnClickListener {
                listener?.onShowBuildsActionClick(
                    description
                )
            }
        }
        if (dataModel.hasBuildTypeAction(position)) {
            holder.itemView.setOnClickListener { listener?.onShowBuildTypeActionClick() }
        }
        if (dataModel.hasProjectAction(position)) {
            holder.itemView.setOnClickListener { listener?.onShowProjectActionClick() }
        }
        if (dataModel.hasArtifactDownloadAction(position)) {
            holder.itemView.setOnClickListener {
                listener?.onArtifactDownloadActionClick(
                    fileName,
                    description
                )
            }
        }
        if (dataModel.hasArtifactOpenAction(position)) {
            holder.itemView.setOnClickListener {
                listener?.onArtifactOpenActionClick(
                    fileName,
                    description
                )
            }
        }
        if (dataModel.hasArtifactOpenInBrowserAction(position)) {
            holder.itemView.setOnClickListener {
                listener?.onArtifactBrowserOpenActionClick(
                    description
                )
            }
        }
    }
}
