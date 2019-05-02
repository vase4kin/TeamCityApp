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

import androidx.annotation.Nullable;

import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.OverviewValueExtractor;

/**
 * Base bundle value extractor interface
 */
public interface BaseValueExtractor extends OverviewValueExtractor {

    /**
     * @return Build type id
     */
    String getId();

    /**
     * @return Name
     */
    String getName();

    /**
     * @return {@link BuildDetails}
     */
    BuildDetails getBuildDetails();

    /**
     * @return {@link BuildListFilter}
     */
    @Nullable
    BuildListFilter getBuildListFilter();

    /**
     * @return determines if the bundle is null or not
     */
    boolean isBundleNullOrEmpty();

    /**
     * Base value extractor stub
     */
    BaseValueExtractor STUB = new BaseValueExtractor() {
        @Override
        public String getId() {
            return "id";
        }

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public BuildDetails getBuildDetails() {
            return BuildDetails.STUB;
        }

        @Override
        public boolean isBundleNullOrEmpty() {
            return true;
        }

        @Nullable
        @Override
        public BuildListFilter getBuildListFilter() {
            return null;
        }
    };
}