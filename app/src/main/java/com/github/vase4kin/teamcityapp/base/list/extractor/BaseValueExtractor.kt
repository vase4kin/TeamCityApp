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

package com.github.vase4kin.teamcityapp.base.list.extractor

import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails

/**
 * Base bundle value extractor interface
 */
interface BaseValueExtractor {

    /**
     * @return Build type id
     */
    val id: String

    /**
     * @return Name
     */
    val name: String

    /**
     * @return [BuildDetails]
     */
    val buildDetails: BuildDetails

    /**
     * @return [BuildListFilter]
     */
    val buildListFilter: BuildListFilter?

    /**
     * @return determines if the bundle is null or not
     */
    val isBundleNullOrEmpty: Boolean

    companion object {

        /**
         * Base value extractor stub
         */
        val STUB: BaseValueExtractor = object : BaseValueExtractor {
            override val id: String
                get() = "id"

            override val name: String
                get() = "name"

            override val buildDetails: BuildDetails
                get() = BuildDetails.STUB

            override val isBundleNullOrEmpty: Boolean
                get() = true

            override val buildListFilter: BuildListFilter?
                get() = null
        }
    }
}