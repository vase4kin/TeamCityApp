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

import android.os.Bundle
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.home.view.HomeActivity

/**
 * Impl of [HomeBundleValueManager]
 */
class HomeBundleValueManagerImpl(private val bundle: Bundle?) : HomeBundleValueManager {

    /**
     * {@inheritDoc}
     */
    override val isRequiredToReload: Boolean
        get() = bundle?.getBoolean(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, false) ?: false

    /**
     * {@inheritDoc}
     */
    override val isNewAccountCreated: Boolean
        get() = bundle?.getBoolean(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, false) ?: false

    /**
     * {@inheritDoc}
     */
    override val isTabSelected: Boolean
        get() = bundle?.containsKey(HomeActivity.ARG_TAB) ?: false

    /**
     * {@inheritDoc}
     */
    override val selectedTab: AppNavigationItem
        get() = AppNavigationItem.values()[bundle?.getInt(HomeActivity.ARG_TAB, 0) ?: 0]

    /**
     * {@inheritDoc}
     */
    override val isNullOrEmpty: Boolean
        get() = bundle == null || bundle == Bundle.EMPTY

    /**
     * {@inheritDoc}
     */
    override fun clear() {
        bundle?.clear()
    }
}
