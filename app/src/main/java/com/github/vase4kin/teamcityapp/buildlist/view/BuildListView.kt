/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.buildlist.view

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.base.list.adapter.ViewLoadMore
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener
import teamcityapp.libraries.onboarding.OnboardingManager

/**
 * View for handling [BuildListActivity]
 */
interface BuildListView : BaseListView<BuildListDataModel>, ViewLoadMore<BuildListDataModel> {

    /**
     * @return {true} if build list is opened, not running list or queue list
     */
    val isBuildListOpen: Boolean

    /**
     * @param onBuildListPresenterListener - Listener to handle view callbacks
     */
    fun setOnBuildListPresenterListener(onBuildListPresenterListener: OnBuildListPresenterListener)

    /**
     * Show retry load more snack bar
     */
    fun showRetryLoadMoreSnackBar()

    /**
     * Set toolbar title
     *
     * @param title - Title
     */
    fun setTitle(title: String)

    /**
     * Show float action button
     */
    fun showRunBuildFloatActionButton()

    /**
     * Hide float action button
     */
    fun hideRunBuildFloatActionButton()

    /**
     * Show build queued success snack bar
     */
    fun showBuildQueuedSuccessSnackBar()

    /**
     * Show build filters've applied snack bar
     */
    fun showBuildFilterAppliedSnackBar()

    /**
     * Show error opening build snack bar
     */
    fun showOpeningBuildErrorSnackBar()

    /**
     * Show configuration has been added to favorites
     */
    fun showAddToFavoritesSnackBar()

    /**
     * Show configuration has been removed from favorites
     */
    fun showRemoveFavoritesSnackBar()

    /**
     * Show build loading progress
     */
    fun showBuildLoadingProgress()

    /**
     * Hide build loading progress
     */
    fun hideBuildLoadingProgress()

    /**
     * Show filter builds onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showFilterBuildsPrompt(listener: OnboardingManager.OnPromptShownListener)

    /**
     * Show run build onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showRunBuildPrompt(listener: OnboardingManager.OnPromptShownListener)

    /**
     * Show fav onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    fun showFavPrompt(listener: OnboardingManager.OnPromptShownListener)

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onCreateOptionsMenu] )}
     */
    fun createFavOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onCreateOptionsMenu] )}
     */
    fun createNotFavOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * {@inheritDoc}
     *
     *
     * See [Fragment.onOptionsItemSelected]
     */
    fun onOptionsItemSelected(item: MenuItem): Boolean

    /**
     * Hide build filters have been applied snack bar
     */
    fun hideFiltersAppliedSnackBar()
}
