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

package com.github.vase4kin.teamcityapp.root.presenter

import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenter
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManager

/**
 * Custom [DrawerPresenter] for [com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity] management
 */
interface RootDrawerPresenter : DrawerPresenter {

    /**
     * On resume activity
     */
    fun onResume()

    /**
     * On new intent activity
     */
    fun onNewIntent()

    /**
     * On account switch
     */
    fun onAccountSwitch()

    /**
     * Update RootBundleValueManager with new one
     *
     * @param rootBundleValueManager - RootBundleValueManager to update
     */
    fun updateRootBundleValueManager(rootBundleValueManager: RootBundleValueManager)
}
