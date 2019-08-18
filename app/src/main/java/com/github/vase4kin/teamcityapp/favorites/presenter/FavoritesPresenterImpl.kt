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

package com.github.vase4kin.teamcityapp.favorites.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractor
import com.github.vase4kin.teamcityapp.favorites.tracker.FavoritesTracker
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesView
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModelImpl
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter
import javax.inject.Inject

/**
 * Presenter to manage logic of favorites list
 */
class FavoritesPresenterImpl @Inject constructor(
    view: FavoritesView,
    interactor: FavoritesInteractor,
    tracker: FavoritesTracker,
    valueExtractor: NavigationValueExtractor,
    private val router: NavigationRouter
) : BaseListPresenterImpl<NavigationDataModel, NavigationItem, FavoritesView, FavoritesInteractor, FavoritesTracker, NavigationValueExtractor>(
    view,
    interactor,
    tracker,
    valueExtractor
), FavoritesView.ViewListener {

    /**
     * {@inheritDoc}
     */
    override fun loadData(loadingListener: OnLoadingListener<List<NavigationItem>>, update: Boolean) {
        dataManager.loadFavorites(loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    override fun initViews() {
        super.initViews()
        view.setViewListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun createModel(data: List<NavigationItem>): NavigationDataModel {
        return NavigationDataModelImpl(data.toMutableList())
    }

    /**
     * {@inheritDoc}
     */
    override fun onClick(navigationItem: NavigationItem) {
        if (navigationItem is BuildType) {
            router.startBuildListActivity(navigationItem.getName(), navigationItem.getId())
            tracker.trackUserOpensBuildType()
        } else {
            router.startNavigationActivity(navigationItem.name, navigationItem.getId())
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onViewsDestroyed() {
        super.onViewsDestroyed()
        dataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        super.onResume()
        view.showRefreshAnimation()
        loadData(loadingListener, false)
    }

    /**
     * On pause activity callback
     */
    fun onPause() {
        view.hideRefreshAnimation()
        dataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun loadDataOnViewsCreated() {
        // Don't load data when view is created, only on resume
    }
}
