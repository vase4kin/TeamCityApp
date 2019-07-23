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

package com.github.vase4kin.teamcityapp.home.extractor;

import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;

/**
 * {@link com.github.vase4kin.teamcityapp.home.view.HomeActivity} bundle value extractor
 */
public interface HomeBundleValueManager extends BaseValueExtractor {

    /**
     * Is projects required to reload
     */
    boolean isRequiredToReload();

    /**
     * Is new account created
     */
    boolean isNewAccountCreated();

    /**
     * Remove is new account create flag from the bundle
     */
    void removeIsNewAccountCreated();

    /**
     * Remove is required to reload flag from the bundle
     */
    void removeIsRequiredToReload();
}
