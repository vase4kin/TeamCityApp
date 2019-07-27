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

package com.github.vase4kin.teamcityapp.home.view

import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.Filter
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager

/**
 * Root drawer view interactions
 */
interface HomeView : DrawerView {

    /**
     * Select drawer selection
     *
     * @param selection - Drawer selection
     */
    fun setDrawerSelection(selection: Int)

    /**
     * Show navigation drawer onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showNavigationDrawerPrompt(listener: OnboardingManager.OnPromptShownListener)

    /**
     * Set listener
     */
    fun setListener(listener: ViewListener?)

    /**
     * Show add favorites info snack bar
     */
    fun showFavoritesInfoSnackbar()

    /**
     * Dismiss snackbar
     */
    fun dimissSnackbar()

    /**
     * Show filter bottom sheet
     */
    fun showFilterBottomSheet(filter: Filter)

    /**
     * Show filter applied snack bar
     */
    fun showFilterAppliedSnackBar()

    /**
     * Show add fav onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showAddFavPrompt(listener: OnboardingManager.OnPromptShownListener)

    interface ViewListener {
        fun onFavoritesSnackBarActionClicked()
    }
}
