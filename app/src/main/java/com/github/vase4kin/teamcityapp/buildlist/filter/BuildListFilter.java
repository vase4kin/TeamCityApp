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

package com.github.vase4kin.teamcityapp.buildlist.filter;

import java.io.Serializable;

/**
 * Filter for build list
 */
public interface BuildListFilter extends Serializable {

    /**
     * Default build list filter
     */
    String DEFAULT_FILTER_LOCATOR = "branch:default:any,running:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:10";

    /**
     * Set filter type
     *
     * @param filter - filter to set
     */
    void setFilter(int filter);

    /**
     * Set branch
     *
     * @param branch - branch to filter with
     */
    void setBranch(String branch);

    /**
     * Filter personal
     *
     * @param isPersonal - flag
     */
    void setPersonal(boolean isPersonal);

    /**
     * Filter pinned
     *
     * @param isPinned - flag
     */
    void setPinned(boolean isPinned);

    /**
     * @return {String} as param locator
     */
    String toLocator();
}
