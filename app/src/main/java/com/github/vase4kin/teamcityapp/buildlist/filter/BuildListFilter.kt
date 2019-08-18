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

package com.github.vase4kin.teamcityapp.buildlist.filter

import java.io.Serializable

/**
 * Filter for build list
 */
interface BuildListFilter : Serializable {

    /**
     * Set filter type
     *
     * @param filter - filter to set
     */
    fun setFilter(filter: Int)

    /**
     * Set branch
     *
     * @param branch - branch to filter with
     */
    fun setBranch(branch: String)

    /**
     * Filter personal
     *
     * @param isPersonal - flag
     */
    fun setPersonal(isPersonal: Boolean)

    /**
     * Filter pinned
     *
     * @param isPinned - flag
     */
    fun setPinned(isPinned: Boolean)

    /**
     * @return {String} as param locator
     */
    fun toLocator(): String

    companion object {

        /**
         * Default build list filter
         */
        const val DEFAULT_FILTER_LOCATOR = "state:any,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:10"
    }
}
