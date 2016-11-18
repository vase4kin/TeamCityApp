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

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel;
import com.github.vase4kin.teamcityapp.properties.view.OnCopyActionAdapterListenerImpl;
import com.github.vase4kin.teamcityapp.properties.view.OnCopyActionClickListener;

import java.util.Map;

/**
 * Adapter to handle build elements
 */
public class OverviewAdapter extends BaseAdapter<OverviewDataModel> {

    private OnCopyActionClickListener mOnCopyActionClickListener;

    public OverviewAdapter(Map<Integer, ViewHolderFactory<OverviewDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    public void setOnCopyActionClickListener(OnCopyActionClickListener onCopyActionClickListener) {
        this.mOnCopyActionClickListener = onCopyActionClickListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(BaseViewHolder<OverviewDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        final OnCopyActionAdapterListenerImpl listener =
                new OnCopyActionAdapterListenerImpl(
                        mDataModel.getHeaderName(position),
                        mDataModel.getDescription(position),
                        mOnCopyActionClickListener);
        ((OverviewViewHolder) holder).mFrameLayout.setOnClickListener(listener);
        ((OverviewViewHolder) holder).mFrameLayout.setOnLongClickListener(listener);
    }

}
