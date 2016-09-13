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

package com.github.vase4kin.teamcityapp.base.list.extractor;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;

/**
 * Base impl of {@link BaseValueExtractor}
 */
public class BaseValueExtractorImpl implements BaseValueExtractor {

    protected Bundle mBundle;

    public BaseValueExtractorImpl(@NonNull Bundle mBundle) {
        this.mBundle = mBundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return mBundle.getString(BundleExtractorValues.ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return mBundle.getString(BundleExtractorValues.NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Build getBuild() {
        return (Build) mBundle.getSerializable(BundleExtractorValues.BUILD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBundleNull() {
        return mBundle == null;
    }
}
