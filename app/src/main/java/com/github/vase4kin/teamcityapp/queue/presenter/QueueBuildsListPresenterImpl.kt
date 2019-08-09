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

package com.github.vase4kin.teamcityapp.queue.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

/**
 * Presenter to handle [com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsFragment]
 */
class QueueBuildsListPresenterImpl @Inject constructor(
        view: RunningBuildListView,
        dataManager: RunningBuildsDataManager,
        tracker: BuildListTracker,
        router: BuildListRouter,
        valueExtractor: BaseValueExtractor,
        buildInteractor: BuildInteractor,
        onboardingManager: OnboardingManager,
        private val filterProvider: FilterProvider,
        private val eventBus: EventBus) : BuildListPresenterImpl<RunningBuildListView, RunningBuildsDataManager>(view, dataManager, tracker, valueExtractor, router, buildInteractor, onboardingManager) {

    /**
     * {@inheritDoc}
     */
    override fun loadData(loadingListener: OnLoadingListener<List<BuildDetails>>, update: Boolean) {
        when (filterProvider.queuedBuildsFilter) {
            Filter.QUEUE_FAVORITES -> dataManager.loadFavorites(loadingListener, update)
            Filter.QUEUE_ALL -> dataManager.load(loadingListener, update)
            else -> view.hideRefreshAnimation()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        super.onResume()
        loadData()
        eventBus.register(this)
    }

    /**
     * On pause activity callback
     */
    fun onPause() {
        stopLoadingData()
        eventBus.unregister(this)
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [HomeDataManager.BuildQueueFilterChangedEvent]
     */
    @Subscribe
    fun onEvent(event: HomeDataManager.BuildQueueFilterChangedEvent) = loadData()

    private fun loadData() {
        view.showRefreshAnimation()
        loadData(loadingListener, false)
    }

    private fun stopLoadingData() {
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
