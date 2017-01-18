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

package com.github.vase4kin.teamcityapp.filter_builds.router;

import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;

/**
 * Router to manage {@link com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity} navigation
 */
public interface FilterBuildsRouter {

    /**
     * Bundle extra key
     */
    String EXTRA_FILTER = "filter";

    /**
     * Close activity with success result
     *
     * @param filter - filter locator
     */
    void closeOnSuccess(BuildListFilter filter);

    /**
     * Close activity with cancel result
     */
    void closeOnCancel();

    /**
     * On back button pressed
     */
    void closeOnBackButtonPressed();
}
