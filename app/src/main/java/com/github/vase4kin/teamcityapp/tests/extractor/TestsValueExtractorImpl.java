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

package com.github.vase4kin.teamcityapp.tests.extractor;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl;

/**
 * Impl of {@link TestsValueExtractor}
 */
public class TestsValueExtractorImpl extends BaseValueExtractorImpl implements TestsValueExtractor {

    public TestsValueExtractorImpl(@NonNull Bundle mBundle) {
        super(mBundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return mBundle.getString(BundleExtractorValues.URL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPassedCount() {
        return mBundle.getInt(BundleExtractorValues.PASSED_COUNT_PARAM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFailedCount() {
        return mBundle.getInt(BundleExtractorValues.FAILED_COUNT_PARAM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIgnoredCount() {
        return mBundle.getInt(BundleExtractorValues.IGNORED_COUNT_PARAM);
    }
}
