/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.view;

import android.view.View;

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;

import java.util.Map;

/**
 * Adapter to manage bottom sheet items
 */
public class BottomSheetAdapter extends BaseAdapter<BottomSheetDataModel> {

    private BottomSheetView.OnBottomSheetClickListener listener;

    public BottomSheetAdapter(Map<Integer, ViewHolderFactory<BottomSheetDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    public void setListener(BottomSheetView.OnBottomSheetClickListener listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(BaseViewHolder<BottomSheetDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        final String title = mDataModel.getTitle(position);
        final String description = mDataModel.getDescription(position);
        if (mDataModel.hasCopyAction(position)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onCopyActionClick(description);
                }
            });
        }
        if (mDataModel.hasBranchAction(position)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onShowBuildsActionClick(description);
                }
            });
        }
        if (mDataModel.hasArtifactDownloadAction(position)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onArtifactDownloadActionClick(title, description);
                }
            });
        }
        if (mDataModel.hasArtifactOpenAction(position)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onArtifactOpenActionClick(description);
                }
            });
        }
        if (mDataModel.hasArtifactOpenInBrowserAction(position)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onArtifactBrowserOpenActionClick(description);
                }
            });
        }
    }

}
