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

package com.github.vase4kin.teamcityapp.root.router;

import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;

/**
 * Router to manage {@link com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity} navigation
 */
public interface RootRouter extends DrawerRouter {

    /**
     * TC root projects id
     */
    String ROOT_PROJECTS_ID = "_Root";

    /**
     * Open root projects
     *
     * @param baseUrl - Url to load projects from
     */
    void openRootProjects(String baseUrl);

    /**
     * Open account list activity
     */
    void openAccountsList();
}
