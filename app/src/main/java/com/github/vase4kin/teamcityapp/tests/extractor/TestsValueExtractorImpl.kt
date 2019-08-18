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

package com.github.vase4kin.teamcityapp.tests.extractor

import android.os.Bundle

import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl

/**
 * Impl of [TestsValueExtractor]
 */
class TestsValueExtractorImpl(bundle: Bundle) : BaseValueExtractorImpl(bundle), TestsValueExtractor {

    /**
     * {@inheritDoc}
     */
    override val url: String
        get() = bundle.getString(BundleExtractorValues.URL) ?: ""

    /**
     * {@inheritDoc}
     */
    override val passedCount: Int
        get() = bundle.getInt(BundleExtractorValues.PASSED_COUNT_PARAM)

    /**
     * {@inheritDoc}
     */
    override val failedCount: Int
        get() = bundle.getInt(BundleExtractorValues.FAILED_COUNT_PARAM)

    /**
     * {@inheritDoc}
     */
    override val ignoredCount: Int
        get() = bundle.getInt(BundleExtractorValues.IGNORED_COUNT_PARAM)
}
