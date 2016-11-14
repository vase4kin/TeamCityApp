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

package com.github.vase4kin.teamcityapp.account.manage.view;

import android.view.View;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;

import java.util.Map;

/**
 * Adapter to handle data for {@link AccountListActivity}
 */
public class AccountAdapter extends BaseAdapter<AccountDataModel> {

    private OnUserAccountClickListener mListener;
    private MultiSelector mMultiSelector;

    public AccountAdapter(Map<Integer, ViewHolderFactory<AccountDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    public void setListener(OnUserAccountClickListener onUserAccountClickListener) {
        this.mListener = onUserAccountClickListener;
    }

    public void setMultiSelector(MultiSelector multiSelector) {
        this.mMultiSelector = multiSelector;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<AccountDataModel> holder, int position) {
        mMultiSelector.bindHolder(((AccountViewHolder) holder), holder.getAdapterPosition(), holder.getItemId());
        super.onBindViewHolder(holder, position);
        final int adapterPosition = position;
        ((AccountViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.click(adapterPosition);
            }
        });
        ((AccountViewHolder) holder).mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.click(adapterPosition);
                return true;
            }
        });
    }

}
