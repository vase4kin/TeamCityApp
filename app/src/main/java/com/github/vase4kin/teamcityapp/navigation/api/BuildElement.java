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

package com.github.vase4kin.teamcityapp.navigation.api;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.base.api.Jsonable;

/**
 * Single build element
 */
public class BuildElement implements Jsonable {

    private int icon;
    private String description;
    private String sectionName;

    public BuildElement(int icon, String description, String sectionName) {
        this.icon = icon;
        this.description = description;
        this.sectionName = sectionName;
    }

    public int getIcon() {
        return icon;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    @Override
    public String getId() {
        return description;
    }

    @NonNull
    public String getSectionName() {
        return sectionName;
    }
}
