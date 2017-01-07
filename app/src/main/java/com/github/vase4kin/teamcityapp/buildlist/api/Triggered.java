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

package com.github.vase4kin.teamcityapp.buildlist.api;

import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.navigation.api.BuildType;

import java.io.Serializable;

/**
 * Triggered
 */
public class Triggered implements Serializable {

    private static final String TRIGGER_TYPE_VCS = "vcs";
    @VisibleForTesting
    public static final String TRIGGER_TYPE_USER = "user";
    private static final String TRIGGER_TYPE_UNKNOWN = "unknown";
    @VisibleForTesting
    public static final String TRIGGER_TYPE_BUILD_TYPE = "buildType";
    @VisibleForTesting
    public static final String TRIGGER_TYPE_RESTARTED = "restarted";

    private String type;
    private String date;
    private String details;

    private User user;
    private BuildType buildType;

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public String getDetails() {
        return details;
    }

    @VisibleForTesting
    public Triggered(String type, String details, User user) {
        this.type = type;
        this.details = details;
        this.user = user;
    }

    public boolean isVcs() {
        return TRIGGER_TYPE_VCS.equals(type);
    }

    public boolean isUser() {
        return TRIGGER_TYPE_USER.equals(type);
    }

    public boolean isUnknown() {
        return TRIGGER_TYPE_UNKNOWN.equals(type);
    }

    public boolean isBuildType() {
        return TRIGGER_TYPE_BUILD_TYPE.equals(type);
    }

    public boolean isRestarted() {
        return TRIGGER_TYPE_RESTARTED.equals(type);
    }
}
