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

package com.github.vase4kin.teamcityapp.account.manage.data;

import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.Collections;
import java.util.List;

/**
 * Impl of {@link AccountDataModel}
 */
public class AccountDataModelImpl implements AccountDataModel {

    private List<UserAccount> mAccounts;

    public AccountDataModelImpl(List<UserAccount> mAccounts) {
        this.mAccounts = mAccounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTeamcityUrl(int position) {
        return mAccounts.get(position).getTeamcityUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName(int position) {
        return mAccounts.get(position).getUserName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sort() {
        Collections.sort(mAccounts, (o1, o2) -> (o1.isActive() == o2.isActive() ? 0 : (o2.isActive() ? 1 : -1)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(UserAccount account) {
        mAccounts.add(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(UserAccount account) {
        mAccounts.remove(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount get(int position) {
        return mAccounts.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mAccounts.size();
    }
}
