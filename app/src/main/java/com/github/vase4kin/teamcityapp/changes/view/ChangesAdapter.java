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

package com.github.vase4kin.teamcityapp.changes.view;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.adapter.LoadMore;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Changes adapter
 */
public class ChangesAdapter extends RecyclerView.Adapter<ChangesAdapter.ChangesViewHolder> implements LoadMore<ChangesDataModel> {

    private ChangesDataModel mDataModel;
    private OnChangeClickListener mOnChangeClickListener;

    private LoadMore mLoadMore = new LoadMore() {
        @Override
        public String getId() {
            return "012345731";
        }
    };

    public ChangesAdapter(ChangesDataModel mDataModel, OnChangeClickListener mOnChangeClickListener) {
        this.mDataModel = mDataModel;
        this.mOnChangeClickListener = mOnChangeClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataModel.isLoadMore(position)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    @Override
    public ChangesViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = viewType == 0
                ? inflater.inflate(R.layout.item_changes_list, viewGroup, false)
                : inflater.inflate(R.layout.item_load_more, viewGroup, false);
        return new ChangesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ChangesViewHolder holder, int position) {
        final int adapterPosition = position;
        if (holder.mIcon != null) {
            holder.mIcon.setText(IconUtils.getCountIcon(mDataModel.getFilesCount(position)));
        }
        if (holder.mItemTitle != null) {
            holder.mItemTitle.setText(mDataModel.getComment(position));
        }
        if (holder.mUserName != null) {
            holder.mUserName.setText(mDataModel.getUserName(position));
        }
        if (holder.mDate != null) {
            holder.mDate.setText(mDataModel.getDate(position));
        }
        if (holder.mItemSubTitle != null) {
            holder.mItemSubTitle.setText(mDataModel.getVersion(position));
        }
        if (holder.mContainer != null) {
            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnChangeClickListener.onClick(mDataModel.getChange(adapterPosition));
                }
            });
        }
    }

    public static class ChangesViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.container)
        FrameLayout mContainer;
        @Nullable
        @BindView(R.id.itemSubTitle)
        TextView mItemSubTitle;
        @Nullable
        @BindView(R.id.itemTitle)
        TextView mItemTitle;
        @Nullable
        @BindView(R.id.itemIcon)
        TextView mIcon;
        @Nullable
        @BindView(R.id.userName)
        TextView mUserName;
        @Nullable
        @BindView(R.id.date)
        TextView mDate;

        public ChangesViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMoreItem() {
        mDataModel.add(mLoadMore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMoreItem() {
        mDataModel.remove(mLoadMore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(ChangesDataModel dataModel) {
        mDataModel.add(dataModel);
    }

    public static class LoadMore extends Changes.Change {
    }
}
