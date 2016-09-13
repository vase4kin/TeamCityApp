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

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

/**
 * Manages data in {@link com.github.vase4kin.teamcityapp.account.manage.view.AccountAdapter}
 */
public interface AccountDataModel extends BaseDataModel {

    /**
     * Get TeamCity server url
     *
     * @param position adapter position
     * @return TeamCity server url
     */
    String getTeamcityUrl(int position);

    /**
     * Get account user name
     *
     * @param position adapter position
     * @return Account user name
     */
    String getUserName(int position);

    /**
     * Sort model data with active account first
     */
    void sort();

    /**
     * Add TeamCity account to the model
     *
     * @param account TeamCity account
     */
    void add(UserAccount account);

    /**
     * Remove TeamCity account from the model
     *
     * @param account TeamCity account
     */
    void remove(UserAccount account);

    /**
     * Get User account object
     *
     * @param position adapter postion
     * @return {@link UserAccount}
     */
    UserAccount get(int position);
}
