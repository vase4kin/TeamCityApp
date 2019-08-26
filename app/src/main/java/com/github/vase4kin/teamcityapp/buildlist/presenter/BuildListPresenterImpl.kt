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

package com.github.vase4kin.teamcityapp.buildlist.presenter

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModelImpl
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import javax.inject.Inject

open class BuildListPresenterImpl<V : BuildListView, DM : BuildListDataManager> @Inject
constructor(
    view: V,
    dataManager: DM,
    tracker: BuildListTracker,
    valueExtractor: BaseValueExtractor,
    private val router: BuildListRouter,
    private val buildInteractor: BuildInteractor,
    private val onboardingManager: OnboardingManager
) : BaseListPresenterImpl<BuildListDataModel, BuildDetails, V, DM, BuildListTracker, BaseValueExtractor>(
    view,
    dataManager,
    tracker,
    valueExtractor
), BuildListPresenter, OnBuildListPresenterListener {

    @VisibleForTesting
    internal var isLoadMoreLoading = false
    /**
     * Saved local queued build href
     */
    @VisibleForTesting
    internal var queuedBuildHref: String? = null

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<BuildDetails>>, update: Boolean) {
        val buildTypeId = valueExtractor.id
        val filter = valueExtractor.buildListFilter
        if (filter != null) {
            dataManager.load(buildTypeId, filter, loadingListener, update)
        } else {
            dataManager.load(buildTypeId, loadingListener, update)
        }
    }

    /**
     * {@inheritDoc}
     */
    public override fun initViews() {
        super.initViews()
        if (!valueExtractor.isBundleNullOrEmpty) {
            view.setTitle(valueExtractor.name)
        }
        view.setOnBuildListPresenterListener(this)
        view.showRunBuildFloatActionButton()
    }

    override fun onResume() {
        super.onResume()
        showRunBuildPrompt()
    }

    /**
     * Show run build prompt
     */
    private fun showRunBuildPrompt() {
        if (view.isBuildListOpen && !onboardingManager.isRunBuildPromptShown) {
            view.showRunBuildPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveRunBuildPromptShown()
                    showFilterBuildsPrompt()
                }
            })
        }
    }

    /**
     * Show filter builds prompt
     */
    private fun showFilterBuildsPrompt() {
        if (!onboardingManager.isFilterBuildsPromptShown) {
            view.showFilterBuildsPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveFilterBuildsPromptShown()
                    showFavPrompt()
                }
            })
        }
    }

    /**
     * Show fav prompt
     */
    private fun showFavPrompt() {
        if (!onboardingManager.isFavPromptShown) {
            view.showFavPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveFavPromptShown()
                }
            })
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onBuildClick(build: Build) {
        if (valueExtractor.isBundleNullOrEmpty) {
            router.openBuildPage(build, null)
        } else {
            val buildTypeName = valueExtractor.name
            router.openBuildPage(build, buildTypeName)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onRunBuildFabClick() {
        router.openRunBuildPage(valueExtractor.id)
        tracker.trackRunBuildButtonPressed()
    }

    /**
     * {@inheritDoc}
     */
    override fun onShowQueuedBuildSnackBarClick() {
        val queuedBuildHref = this.queuedBuildHref ?: return
        tracker.trackUserWantsToSeeQueuedBuildDetails()
        view.showBuildLoadingProgress()
        buildInteractor.loadBuild(queuedBuildHref, object : OnLoadingListener<Build> {
            override fun onSuccess(data: Build) {
                view.hideBuildLoadingProgress()
                val buildTypeName = valueExtractor.name
                router.openBuildPage(data, buildTypeName)
            }

            override fun onFail(errorMessage: String) {
                view.hideBuildLoadingProgress()
                view.showOpeningBuildErrorSnackBar()
            }
        })
    }

    /**
     * {@inheritDoc}
     */
    override fun onNavigateToFavorites() {
        router.openFavorites()
    }

    /**
     * {@inheritDoc}
     */
    override fun onFilterBuildsOptionMenuClick() {
        router.openFilterBuildsPage(valueExtractor.id)
    }

    override fun onAddToFavoritesOptionMenuClick() {
        val buildTypeId = valueExtractor.id
        if (dataManager.hasBuildTypeAsFavorite(buildTypeId)) {
            dataManager.removeFromFavorites(buildTypeId)
            view.showRemoveFavoritesSnackBar()
        } else {
            dataManager.addToFavorites(valueExtractor.id)
            view.showAddToFavoritesSnackBar()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onResetFiltersSnackBarActionClick() {
        view.disableSwipeToRefresh()
        view.showRefreshAnimation()
        view.hideErrorView()
        view.hideEmpty()
        view.showData(BuildListDataModelImpl(mutableListOf()))
        dataManager.load(valueExtractor.id, loadingListener, true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onLoadMore() {
        isLoadMoreLoading = true
        view.addLoadMore()
        dataManager.loadMore(object : OnLoadingListener<List<BuildDetails>> {
            override fun onSuccess(data: List<BuildDetails>) {
                view.removeLoadMore()
                view.addMoreBuilds(BuildListDataModelImpl(data.toMutableList()))
                isLoadMoreLoading = false
            }

            override fun onFail(errorMessage: String) {
                view.removeLoadMore()
                view.showRetryLoadMoreSnackBar()
                isLoadMoreLoading = false
            }
        })
    }

    /**
     * {@inheritDoc}
     */
    override fun isLoading(): Boolean {
        return isLoadMoreLoading
    }

    /**
     * {@inheritDoc}
     */
    override fun hasLoadedAllItems(): Boolean {
        return !dataManager.canLoadMore()
    }

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: List<BuildDetails>): BuildListDataModel {
        return BuildListDataModelImpl(data.toMutableList())
    }

    /**
     * {@inheritDoc}
     */
    public override fun onSuccessCallBack(data: List<BuildDetails>) {
        super.onSuccessCallBack(data)
        view.showRunBuildFloatActionButton()
    }

    /**
     * {@inheritDoc}
     */
    public override fun onFailCallBack(errorMessage: String) {
        super.onFailCallBack(errorMessage)
        view.hideRunBuildFloatActionButton()
    }

    /**
     * {@inheritDoc}
     */
    override fun onRunBuildActivityResult(queuedBuildHref: String) {
        this.queuedBuildHref = queuedBuildHref
        view.showBuildQueuedSuccessSnackBar()
        view.showRefreshAnimation()
        onSwipeToRefresh()
    }

    /**
     * {@inheritDoc}
     */
    override fun onFilterBuildsActivityResult(filter: BuildListFilter) {
        view.showBuildFilterAppliedSnackBar()
        view.disableSwipeToRefresh()
        view.showRefreshAnimation()
        view.hideErrorView()
        view.hideEmpty()
        view.showData(BuildListDataModelImpl(mutableListOf()))
        dataManager.load(valueExtractor.id, filter, loadingListener, true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onViewsDestroyed() {
        super.onViewsDestroyed()
        buildInteractor.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val buildTypeId = valueExtractor.id
        if (dataManager.hasBuildTypeAsFavorite(buildTypeId)) {
            view.createFavOptionsMenu(menu, inflater)
        } else {
            view.createNotFavOptionsMenu(menu, inflater)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onPrepareOptionsMenu(menu: Menu) {}

    /**
     * {@inheritDoc}
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return view.onOptionsItemSelected(item)
    }

    /**
     * {@inheritDoc}
     */
    public override fun onSwipeToRefresh() {
        super.onSwipeToRefresh()
        view.hideFiltersAppliedSnackBar()
    }
}
