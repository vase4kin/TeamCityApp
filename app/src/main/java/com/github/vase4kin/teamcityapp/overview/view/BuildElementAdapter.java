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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel;
import com.github.vase4kin.teamcityapp.properties.view.OnCopyActionAdapterListenerImpl;
import com.github.vase4kin.teamcityapp.properties.view.OnCopyActionClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to handle build elements
 */
public class BuildElementAdapter extends RecyclerView.Adapter<BuildElementAdapter.BuildElementViewHolder> {

    private OverviewDataModel mDataModel;
    private OnCopyActionClickListener mOnCopyActionClickListener;

    public BuildElementAdapter(OverviewDataModel mDataModel, OnCopyActionClickListener listener) {
        this.mDataModel = mDataModel;
        this.mOnCopyActionClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    @Override
    public BuildElementViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = inflater.inflate(R.layout.item_card_element_list, viewGroup, false);
        return new BuildElementViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BuildElementViewHolder holder, final int position) {
        holder.icon.setText(mDataModel.getIcon(position));
        holder.mHeader.setText(mDataModel.getHeaderName(position));
        holder.description.setText(mDataModel.getDescription(position));
        OnCopyActionAdapterListenerImpl listener =
                new OnCopyActionAdapterListenerImpl(
                        mDataModel.getHeaderName(position),
                        mDataModel.getDescription(position),
                        mOnCopyActionClickListener);
        holder.mFrameLayout.setOnClickListener(listener);
        holder.mFrameLayout.setOnLongClickListener(listener);
    }

    public static class BuildElementViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        FrameLayout mFrameLayout;
        @BindView(R.id.itemIcon)
        TextView icon;
        @BindView(R.id.itemTitle)
        TextView description;
        @BindView(R.id.itemHeader)
        TextView mHeader;

        public BuildElementViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
