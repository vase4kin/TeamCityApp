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

import android.os.Bundle

import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl

/**
 * Base impl of [BaseValueExtractor]
 */
open class BaseValueExtractorImpl(protected val bundle: Bundle) : BaseValueExtractor {

    /**
     * {@inheritDoc}
     */
    override val id: String
        get() = bundle.getString(BundleExtractorValues.ID) ?: ""

    /**
     * {@inheritDoc}
     */
    override val name: String
        get() = bundle.getString(BundleExtractorValues.NAME) ?: ""

    /**
     * {@inheritDoc}
     */
    override val buildDetails: BuildDetails
        get() {
            val build = bundle.getSerializable(BundleExtractorValues.BUILD) as Build
            return BuildDetailsImpl(build)
        }

    /**
     * {@inheritDoc}
     */
    override val buildListFilter: BuildListFilter?
        get() = bundle.getSerializable(BundleExtractorValues.BUILD_LIST_FILTER) as BuildListFilter

    /**
     * {@inheritDoc}
     */
    override val isBundleNullOrEmpty: Boolean
        get() = bundle.isEmpty
}
