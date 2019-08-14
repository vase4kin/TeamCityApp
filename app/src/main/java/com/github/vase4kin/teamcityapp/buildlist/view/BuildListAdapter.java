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

package com.github.vase4kin.teamcityapp.buildlist.view;

import android.view.View;

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseLoadMoreAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;

import java.util.Map;

/**
 * Adapter for builds
 */
public class BuildListAdapter extends BaseLoadMoreAdapter<BuildListDataModel> {

    private OnBuildListPresenterListener mOnBuildListPresenterListener;

    /**
     * Constructor
     *
     * @param viewHolderFactories - view holder factories from DI
     */
    public BuildListAdapter(Map<Integer, ViewHolderFactory<BuildListDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    /**
     * Set {@link OnBuildListPresenterListener}
     *
     * @param onBuildListPresenterListener - listener to set
     */
    public void setOnBuildListPresenterListener(OnBuildListPresenterListener onBuildListPresenterListener) {
        this.mOnBuildListPresenterListener = onBuildListPresenterListener;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<BuildListDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        final int adapterPosition = position;
        // Find the way how to make it through DI
        if (holder instanceof BuildViewHolder) {
            ((BuildViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnBuildListPresenterListener.onBuildClick(dataModel.getBuild(adapterPosition));
                }
            });
        }
    }
}
