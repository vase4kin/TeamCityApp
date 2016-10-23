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

package com.github.vase4kin.teamcityapp.storage.api;

import com.github.vase4kin.teamcityapp.base.api.Jsonable;

/**
 * User account
 */
public class UserAccount implements Jsonable {

    private String mTeamcityUrl;
    private String mUserName;
    private String mPassword;
    private boolean mIsGuestUser;
    private boolean mIsActive;

    public UserAccount(String teamcityUrl,
                       String userName,
                       String password,
                       boolean isGuestUser,
                       boolean isActive) {
        this.mUserName = userName;
        this.mTeamcityUrl = teamcityUrl;
        this.mPassword = password;
        this.mIsGuestUser = isGuestUser;
        this.mIsActive = isActive;
    }

    public String getTeamcityUrl() {
        return mTeamcityUrl;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getPassword() {
        return mPassword;
    }

    public boolean isGuestUser() {
        return mIsGuestUser;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setIsActive(boolean isActive) {
        this.mIsActive = isActive;
    }

    @Override
    public String getId() {
        return mTeamcityUrl;
    }
}
