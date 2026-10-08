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

package com.github.vase4kin.teamcityapp.buildlist.router

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity

/**
 * Build list router
 */
interface BuildListRouter {

    /**
     * Start build details activity [com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity]
     *
     * @param build - Build
     * @param buildTypeName - Build type name
     */
    fun openBuildPage(build: Build, buildTypeName: String?)

    /**
     * Open run build page [com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity]
     *
     * @param buildTypeId - Build type id
     */
    fun openRunBuildPage(buildTypeId: String)

    /**
     * Open filter builds page [FilterBuildsActivity]
     *
     * @param buildTypeId - Build type id
     */
    fun openFilterBuildsPage(buildTypeId: String)

    /**
     * Open favorites
     */
    fun openFavorites()
}
