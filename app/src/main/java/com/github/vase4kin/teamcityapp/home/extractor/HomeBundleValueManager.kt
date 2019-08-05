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

package com.github.vase4kin.teamcityapp.home.extractor

import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem

/**
 * [com.github.vase4kin.teamcityapp.home.view.HomeActivity] bundle value extractor
 */
interface HomeBundleValueManager {

    /**
     * Is projects required to reload
     */
    val isRequiredToReload: Boolean

    /**
     * Is new account created
     */
    val isNewAccountCreated: Boolean

    /**
     * If tab selected
     */
    val isTabSelected: Boolean

    /**
     * Selected tab
     */
    val selectedTab: AppNavigationItem

    /**
     * If null or empty
     */
    val isNullOrEmpty: Boolean

    /**
     * Clear bundle
     */
    fun clear()
}
