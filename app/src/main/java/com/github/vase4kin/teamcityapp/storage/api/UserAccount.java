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

    private String teamcityUrl;
    private String userName;
    private byte[] password;
    private boolean isGuestUser;
    private boolean isActive;
    private boolean isSslDisabled;

    public UserAccount(String teamcityUrl,
                       String userName,
                       byte[] password,
                       boolean isGuestUser,
                       boolean isActive) {
        this.userName = userName;
        this.teamcityUrl = teamcityUrl;
        this.password = password;
        this.isGuestUser = isGuestUser;
        this.isActive = isActive;
        this.isSslDisabled = false;
    }

    public String getTeamcityUrl() {
        return teamcityUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPasswordAsString() {
        return new String(password);
    }

    public byte[] getPasswordAsBytes() {
        return password;
    }

    public boolean isGuestUser() {
        return isGuestUser;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setSslDisabled(boolean sslDisabled) {
        isSslDisabled = sslDisabled;
    }

    public boolean isSslDisabled() {
        return isSslDisabled;
    }

    @Override
    public String getId() {
        return teamcityUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount userAccount = (UserAccount) o;
        return teamcityUrl.equals(userAccount.getTeamcityUrl())
                && userName.equals(userAccount.getUserName());
    }

    @Override
    public int hashCode() {
        int result = 17;
        return 31 * result + teamcityUrl.length() + userName.length();
    }
}
