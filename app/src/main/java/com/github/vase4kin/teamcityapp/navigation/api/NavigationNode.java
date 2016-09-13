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

package com.github.vase4kin.teamcityapp.navigation.api;

import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;
import com.github.vase4kin.teamcityapp.base.api.CommonJsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Navigation node
 */
public class NavigationNode extends CommonJsonObject implements Collectible<NavigationItem> {

    private Projects projects;

    private BuildTypes buildTypes;

    @Override
    public List<NavigationItem> getObjects() {
        List<NavigationItem> allObjects = new ArrayList<>();
        if (projects.getProjects() != null) {
            allObjects.addAll(projects.getProjects());
        }
        if (buildTypes.getBuildTypes() != null) {
            allObjects.addAll(buildTypes.getBuildTypes());
        }
        return allObjects;
    }

    @VisibleForTesting
    public NavigationNode(Projects projects, BuildTypes buildTypes) {
        this.projects = projects;
        this.buildTypes = buildTypes;
    }
}
