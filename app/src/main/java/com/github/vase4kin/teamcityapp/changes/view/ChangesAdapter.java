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

import android.view.View;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.adapter.ViewLoadMore;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;

import java.util.Map;

/**
 * Changes adapter
 */
public class ChangesAdapter extends BaseAdapter<ChangesDataModel> implements ViewLoadMore<ChangesDataModel> {

    private OnChangeClickListener mOnChangeClickListener;

    public ChangesAdapter(Map<Integer, ViewHolderFactory<ChangesDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    /**
     * Set {@link OnChangeClickListener}
     *
     * @param onChangeClickListener - listener to set
     */
    void setOnChangeClickListener(OnChangeClickListener onChangeClickListener) {
        this.mOnChangeClickListener = onChangeClickListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        // some method to check
        if (mDataModel.isLoadMore(position)) {
            return BaseListView.TYPE_LOAD_MORE;
        } else {
            return BaseListView.TYPE_DEFAULT;
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
    public BaseViewHolder<ChangesDataModel> onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return mViewHolderFactories.get(viewType).createViewHolder(viewGroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final BaseViewHolder<ChangesDataModel> holder, int position) {
        final int adapterPosition = position;
        holder.bind(mDataModel, adapterPosition);
        // Find the way how to make it through DI
        if (holder instanceof ChangesViewHolder) {
            ((ChangesViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnChangeClickListener.onClick(mDataModel.getChange(adapterPosition));
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mDataModel.addLoadMore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mDataModel.removeLoadMore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(ChangesDataModel dataModel) {
        mDataModel.addMoreBuilds(dataModel);
    }
}
