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

import com.github.vase4kin.teamcityapp.base.api.BaseObject;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;

import java.io.Serializable;

/**
 * Build type
 */
public class BuildType extends NavigationItem implements Serializable {

    private String projectName;

    private String projectId;

    private Builds builds;

    private BaseObject compatibleAgents;

    public Builds getBuilds() {
        return builds;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public BaseObject getCompatibleAgents() {
        return compatibleAgents;
    }
}
