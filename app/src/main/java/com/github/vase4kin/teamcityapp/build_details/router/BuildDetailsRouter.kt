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

package com.github.vase4kin.teamcityapp.build_details.router

import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter

/**
 * Router for [BuildDetailsActivity]
 */
interface BuildDetailsRouter {

    /**
     * Reopen [BuildDetailsActivity]
     *
     * @param build         - Build is passed as new intent
     * @param buildTypeName - Build type name
     */
    fun reopenBuildTabsActivity(build: Build, buildTypeName: String)

    /**
     * Share build web url
     *
     * @param webUrl - url to share
     */
    fun startShareBuildWebUrlActivity(webUrl: String)

    /**
     * Start build list activity
     *
     * @param name   - build type name
     * @param id     - build type id
     * @param filter - build list filter
     */
    fun startBuildListActivity(name: String, id: String, filter: BuildListFilter?)

    /**
     * Start project activity
     */
    fun startProjectActivity(name: String, id: String)
}
