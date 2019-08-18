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

package com.github.vase4kin.teamcityapp.navigation.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.api.RateTheApp
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModelImpl
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView
import com.github.vase4kin.teamcityapp.navigation.view.OnNavigationItemClickListener
import javax.inject.Inject

/**
 * Present to handle logic of [com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity]
 */
class NavigationPresenterImpl @Inject constructor(
    view: NavigationView,
    dataManager: NavigationDataManager,
    tracker: NavigationTracker,
    valueExtractor: NavigationValueExtractor,
    private val router: NavigationRouter
) : BaseListPresenterImpl<NavigationDataModel, NavigationItem, NavigationView, NavigationDataManager, NavigationTracker, NavigationValueExtractor>(
    view,
    dataManager,
    tracker,
    valueExtractor
), OnNavigationItemClickListener {

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<NavigationItem>>, update: Boolean) {
        dataManager.load(valueExtractor.id, update, loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    public override fun initViews() {
        super.initViews()
        view.setTitle(valueExtractor.name)
        view.setNavigationAdapterClickListener(this)
    }

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: MutableList<NavigationItem>): NavigationDataModel {
        if (data.isNotEmpty() && dataManager.showRateTheApp()) {
            tracker.trackUserSawRateTheApp()
            data.add(RateTheApp.POSITION, RateTheApp())
        }
        return NavigationDataModelImpl(data)
    }

    /**
     * {@inheritDoc}
     */
    override fun onClick(navigationItem: NavigationItem) {
        if (navigationItem is BuildType) {
            router.startBuildListActivity(navigationItem.getName(), navigationItem.getId())
        } else {
            router.startNavigationActivity(navigationItem.name, navigationItem.getId())
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onRateCancelButtonClick() {
        view.hideTheRateApp()
        dataManager.saveRateCancelClickedOn()
        tracker.trackUserClickedOnRateCancel()
    }

    /**
     * {@inheritDoc}
     */
    override fun onRateNowButtonClick() {
        view.hideTheRateApp()
        dataManager.saveRateNowClickedOn()
        router.openRateTheApp()
        tracker.trackUserClickedOnRateNow()
    }

    /**
     * {@inheritDoc}
     */
    override fun onViewsDestroyed() {
        super.onViewsDestroyed()
        dataManager.unsubscribe()
    }
}
