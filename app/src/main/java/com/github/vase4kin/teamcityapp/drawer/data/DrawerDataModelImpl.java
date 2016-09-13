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

package com.github.vase4kin.teamcityapp.drawer.data;

import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.Iterator;
import java.util.List;

/**
 * Impl of {@link DrawerDataModel}
 */
public class DrawerDataModelImpl implements DrawerDataModel {

    private List<UserAccount> mAccounts;

    public DrawerDataModelImpl(List<UserAccount> mAccounts) {
        this.mAccounts = mAccounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(int position) {
        return mAccounts.get(position).getUserName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTeamCityUrl(int position) {
        return mAccounts.get(position).getTeamcityUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return mAccounts.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<UserAccount> iterator() {
        return mAccounts.iterator();
    }
}
