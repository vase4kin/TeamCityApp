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

package com.github.vase4kin.teamcityapp.artifact.view;

import android.view.View;

import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;

import java.util.Map;

/**
 * Adapter for artifact files
 */
public class ArtifactAdapter extends BaseAdapter<ArtifactDataModel> {

    private OnArtifactPresenterListener mOnClickListener;

    /**
     * Constructor
     *
     * @param viewHolderFactories - view holder factories from DI
     */
    public ArtifactAdapter(Map<Integer, ViewHolderFactory<ArtifactDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    /**
     * Set {@link OnArtifactPresenterListener}
     *
     * @param onArtifactPresenterListener - listener to set
     */
    public void setOnClickListener(OnArtifactPresenterListener onArtifactPresenterListener) {
        this.mOnClickListener = onArtifactPresenterListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(BaseViewHolder<ArtifactDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        final File artifactFile = mDataModel.getFile(position);
        // Find the way how to make it through DI
        ((ArtifactViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onClick(artifactFile);
            }
        });
        ((ArtifactViewHolder) holder).mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnClickListener.onLongClick(artifactFile);
                return true;
            }
        });
    }

}
