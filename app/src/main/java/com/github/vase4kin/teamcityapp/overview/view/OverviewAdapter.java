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

package com.github.vase4kin.teamcityapp.overview.view;

import android.view.View;

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel;

import java.util.Map;

/**
 * Adapter to handle build elements
 */
public class OverviewAdapter extends BaseAdapter<OverviewDataModel> {

    private OverviewView.ViewListener mViewListener;

    public OverviewAdapter(Map<Integer, ViewHolderFactory<OverviewDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    public void setViewListener(OverviewView.ViewListener onViewListener) {
        this.mViewListener = onViewListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(BaseViewHolder<OverviewDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        final String header = mDataModel.getHeaderName(position);
        final String description = mDataModel.getDescription(position);
        if (mDataModel.isBranchCard(position)) {
            ((OverviewViewHolder) holder).mFrameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewListener.onBranchCardClick(description);
                }
            });
            ((OverviewViewHolder) holder).mFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mViewListener.onBranchCardClick(description);
                    return true;
                }
            });
        } else if (mDataModel.isBuildTypeCard(position)) {
            ((OverviewViewHolder) holder).mFrameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewListener.onBuildTypeCardClick(description);
                }
            });
            ((OverviewViewHolder) holder).mFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mViewListener.onBuildTypeCardClick(description);
                    return true;
                }
            });
        } else if (mDataModel.isProjectCard(position)) {
            ((OverviewViewHolder) holder).mFrameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewListener.onProjectCardClick(description);
                }
            });
            ((OverviewViewHolder) holder).mFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mViewListener.onProjectCardClick(description);
                    return true;
                }
            });
        } else {
            ((OverviewViewHolder) holder).mFrameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewListener.onCardClick(header, description);
                }
            });
            ((OverviewViewHolder) holder).mFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mViewListener.onCardClick(header, description);
                    return true;
                }
            });
        }
    }

}
