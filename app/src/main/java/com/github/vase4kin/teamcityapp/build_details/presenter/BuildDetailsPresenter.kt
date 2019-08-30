/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.build_details.presenter

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.tabs.presenter.BaseTabsPresenter

/**
 * Presenter to handle [BuildDetailsPresenter] logic
 */
interface BuildDetailsPresenter : BaseTabsPresenter {

    /**
     * On save activity state
     *
     * @param outState - Bundle with state to save
     */
    fun onSaveInstanceState(outState: Bundle?)

    /**
     * On restore activity state
     *
     * @param savedInstanceState - Bundle with saved state
     */
    fun onRestoreInstanceState(savedInstanceState: Bundle?)

    companion object {

        /**
         * Changes tab position
         */
        const val CHANGES_TAB = 1

        /**
         * Tests tab position
         */
        const val TESTS_TAB = 2
    }
}
