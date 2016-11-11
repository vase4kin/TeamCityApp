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

package com.github.vase4kin.teamcityapp.buildlist.view;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.adapter.ViewLoadMore;
import com.github.vase4kin.teamcityapp.buildlist.api.LoadMoreBuild;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for builds
 */
public class BuildListAdapter extends RecyclerView.Adapter<BuildListAdapter.BuildViewHolder> implements ViewLoadMore<BuildListDataModel> {

    private BuildListDataModel mDataModel;

    private LoadMoreBuild mLoadMoreBuild = new LoadMoreBuild() {
        @Override
        public String getId() {
            return "04532341";
        }
    };

    private OnBuildListPresenterListener mOnBuildListPresenterListener;

    public BuildListAdapter(BuildListDataModel dataModel, OnBuildListPresenterListener listener) {
        this.mDataModel = dataModel;
        this.mOnBuildListPresenterListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        if (mDataModel.isLoadMoreBuild(position)) {
            return 2;
        } else {
            return (mDataModel.getBranchName(position) != null) ? 0 : 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = null;
        switch (viewType) {
            case 0:
                v = inflater.inflate(R.layout.item_build_list_with_sub_title, viewGroup, false);
                break;
            case 1:
                v = inflater.inflate(R.layout.item_build_list, viewGroup, false);
                break;
            case 2:
                v = inflater.inflate(R.layout.item_load_more, viewGroup, false);
                break;
        }
        return new BuildViewHolder(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final BuildViewHolder holder, int position) {
        final int adapterPosition = position;
        if (!(mDataModel.isLoadMoreBuild(position))) {
            if (holder.mContainer != null) {
                holder.mContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnBuildListPresenterListener.onClick(mDataModel.getBuild(adapterPosition));
                    }
                });
            }
            if (mDataModel.getBranchName(position) != null) {
                if (holder.mBranchName != null) {
                    holder.mBranchName.setText(mDataModel.getBranchName(position));
                }
            }
            if (holder.mIcon != null) {
                holder.mIcon.setText(mDataModel.getBuildStatusIcon(position));
            }
            if (holder.mStatusText != null) {
                holder.mStatusText.setText(mDataModel.getStatusText(position));
            }
            if (holder.mBuildNumber != null) {
                String buildNumber = mDataModel.getBuildNumber(position);
                if (TextUtils.isEmpty(buildNumber)) {
                    holder.mBuildNumber.setVisibility(View.GONE);
                } else {
                    holder.mBuildNumber.setText(mDataModel.getBuildNumber(position));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mDataModel.add(mLoadMoreBuild);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mDataModel.remove(mLoadMoreBuild);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(BuildListDataModel dataModel) {
        mDataModel.add(dataModel);
    }

    public static class BuildViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.container)
        FrameLayout mContainer;
        @Nullable
        @BindView(R.id.itemSubTitle)
        TextView mBranchName;
        @Nullable
        @BindView(R.id.itemTitle)
        TextView mStatusText;
        @Nullable
        @BindView(R.id.itemIcon)
        TextView mIcon;
        @Nullable
        @BindView(R.id.buildNumber)
        TextView mBuildNumber;


        public BuildViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public BuildListDataModel getDataModel() {
        return mDataModel;
    }
}
