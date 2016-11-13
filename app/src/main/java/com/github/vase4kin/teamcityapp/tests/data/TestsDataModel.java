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

package com.github.vase4kin.teamcityapp.tests.data;

import com.github.vase4kin.teamcityapp.base.list.adapter.ModelLoadMore;
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

/**
 * Data model to manage tests data
 */
public interface TestsDataModel extends BaseDataModel, Iterable<TestOccurrences.TestOccurrence>, ModelLoadMore<TestsDataModel> {

    /**
     * @param position - Adapter position
     * @return boolean
     */
    boolean isFailed(int position);

    /**
     * Get tests title
     *
     * @param position - Adapter position
     * @return String
     */
    String getName(int position);

    /**
     * Get test icon
     *
     * @param position - Adapter position
     * @return String
     */
    String getStatusIcon(int position);

    /**
     * Get test details url
     *
     * @param position - Adapter position
     * @return String
     */
    String getHref(int position);

    /**
     * Get test status
     *
     * @param position - Adapter position
     * @return String
     */
    String getStatus(int position);
}
