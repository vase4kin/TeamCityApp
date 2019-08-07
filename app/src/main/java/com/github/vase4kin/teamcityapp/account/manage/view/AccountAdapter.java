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

import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.Map;

/**
 * Adapter to handle data for {@link AccountListActivity}
 */
public class AccountAdapter extends BaseAdapter<AccountDataModel> {

    private AccountsView.ViewListener listener;

    public AccountAdapter(Map<Integer, ViewHolderFactory<AccountDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    public void setListener(AccountsView.ViewListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<AccountDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        UserAccount userAccount = mDataModel.get(position);
        ((AccountViewHolder) holder).mContainer.setOnClickListener(v -> listener.onAccountClick(userAccount));
        ((AccountViewHolder) holder).mContainer.setOnLongClickListener(v -> {
            listener.onAccountClick(userAccount);
            return true;
        });
    }

}
