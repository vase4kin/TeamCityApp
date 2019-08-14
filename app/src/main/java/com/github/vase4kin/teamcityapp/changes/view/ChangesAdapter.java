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

package com.github.vase4kin.teamcityapp.changes.view;

import android.view.View;

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseLoadMoreAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;

import java.util.Map;

/**
 * Changes adapter
 */
public class ChangesAdapter extends BaseLoadMoreAdapter<ChangesDataModel> {

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
    public void onBindViewHolder(BaseViewHolder<ChangesDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        final int adapterPosition = position;
        // Find the way how to make it through DI
        if (holder instanceof ChangesViewHolder) {
            ((ChangesViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnChangeClickListener.onClick(dataModel.getChange(adapterPosition));
                }
            });
        }
    }
}
