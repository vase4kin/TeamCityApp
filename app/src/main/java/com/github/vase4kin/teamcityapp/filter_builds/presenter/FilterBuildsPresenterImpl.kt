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

package com.github.vase4kin.teamcityapp.filter_builds.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FilterBuildsTracker
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView

import javax.inject.Inject

/**
 * Impl of [FilterBuildsPresenter]
 */
class FilterBuildsPresenterImpl @Inject constructor(
        private val view: FilterBuildsView,
        private val router: FilterBuildsRouter,
        private val branchesInteractor: BranchesInteractor,
        private val branchesComponentView: BranchesComponentView,
        private val tracker: FilterBuildsTracker
) : FilterBuildsPresenter, FilterBuildsView.ViewListener {

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        view.initViews(this)
        branchesComponentView.initViews()
        branchesInteractor.loadBranches(object : OnLoadingListener<List<String>> {
            override fun onSuccess(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") branches: List<String>) {
                branchesComponentView.hideBranchesLoadingProgress()
                if (branches.isEmpty() || branches.size == 1) {
                    // If branch is single no need to show it, cause it can be default one, and you can not filter by default one
                    branchesComponentView.showNoBranchesAvailableToFilter()
                } else {
                    branchesComponentView.setupAutoComplete(branches)
                    branchesComponentView.setAutocompleteHintForFilter()
                    branchesComponentView.showBranchesAutoComplete()
                }
            }

            override fun onFail(errorMessage: String) {
                branchesComponentView.hideBranchesLoadingProgress()
                branchesComponentView.showNoBranchesAvailable()
            }
        })
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        tracker.trackView()
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        view.unbindViews()
        branchesComponentView.unbindViews()
        branchesInteractor.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onBackPressed() {
        router.closeOnBackButtonPressed()
    }

    /**
     * {@inheritDoc}
     */
    override fun onClick() {
        router.closeOnCancel()
    }

    override fun onFilterFabClick(filterType: Int, isPersonal: Boolean, isPinned: Boolean) {
        val filter = BuildListFilterImpl()
        filter.setFilter(filterType)
        filter.setBranch(branchesComponentView.branchName)
        filter.setPersonal(isPersonal)
        filter.setPinned(isPinned)
        tracker.trackUserFilteredBuilds()
        router.closeOnSuccess(filter)
    }

    /**
     * {@inheritDoc}
     */
    override fun onQueuedFilterSelected() {
        view.hideSwitchForPinnedFilter()
    }

    /**
     * {@inheritDoc}
     */
    override fun onOtherFiltersSelected() {
        view.showSwitchForPinnedFilter()
    }
}
