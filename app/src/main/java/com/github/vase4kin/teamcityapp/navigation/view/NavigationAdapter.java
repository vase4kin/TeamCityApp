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

package com.github.vase4kin.teamcityapp.navigation.view;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Navigation items adapter
 */
public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder> {

    private static final String PROJECT = "{md-filter-none}";
    private static final String BUILD_TYPE = "{md-crop-din}";

    private OnNavigationItemClickListener mOnClickListener;
    private NavigationDataModel mDataModel;

    public NavigationAdapter(NavigationDataModel mDataModel) {
        this.mDataModel = mDataModel;
    }

    public void setOnClickListener(OnNavigationItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        // here you can item can return the view type (For example: project = 1, buildtype = 2)
        if (mDataModel.getDescription(position) == null) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    @Override
    public NavigationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // here you can choose what item you need to add to adapter (For example: project = 1, buildtype = 2)
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = inflater.inflate((viewType == 0) ? R.layout.item_with_title_list : R.layout.item_with_title_and_sub_title_list, viewGroup, false);
        return new NavigationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NavigationViewHolder holder, int position) {
        final int adapterPosition = position;
        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onClick(mDataModel.getNavigationItem(adapterPosition));
            }
        });
        holder.mTextView.setText(mDataModel.getName(position));
        if (holder.mDescription != null) {
            holder.mDescription.setText(mDataModel.getDescription(position));
        }
        if (mDataModel.isProject(position)) {
            holder.mIcon.setText(PROJECT);
        } else {
            holder.mIcon.setText(BUILD_TYPE);
        }
    }

    public static class NavigationViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        FrameLayout mContainer;
        @BindView(R.id.itemTitle)
        TextView mTextView;
        @Nullable
        @BindView(R.id.itemSubTitle)
        TextView mDescription;
        @BindView(R.id.itemIcon)
        TextView mIcon;

        public NavigationViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
