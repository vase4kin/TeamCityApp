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

package com.github.vase4kin.teamcityapp.storage.api;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import teamcityapp.libraries.api.Jsonable;

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
    private List<String> buildTypeIds = new ArrayList<>();

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

    @NonNull
    public String getTeamcityUrl() {
        return teamcityUrl;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    @NonNull
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

    public void addBuildType(String buildTypeId) {
        if (buildTypeIds == null) {
            buildTypeIds = new ArrayList<>();
            buildTypeIds.add(buildTypeId);
        } else {
            buildTypeIds.add(buildTypeId);
        }
    }

    @NonNull
    public List<String> getBuildTypeIds() {
        return buildTypeIds == null ? Collections.<String>emptyList() : buildTypeIds;
    }

    public void setBuildTypeIds(List<String> buildTypeIds) {
        this.buildTypeIds = buildTypeIds;
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
