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

package com.github.vase4kin.teamcityapp.home.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.home.extractor.HomeBundleValueManager
import com.github.vase4kin.teamcityapp.home.router.HomeRouter
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker
import com.github.vase4kin.teamcityapp.home.view.HomeView
import com.github.vase4kin.teamcityapp.home.view.OnAccountSwitchListener
import com.github.vase4kin.teamcityapp.home.view.OnDrawerUpdateListener
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import javax.inject.Inject

/**
 * Impl of [HomePresenter]
 */
class HomePresenterImpl @Inject constructor(
        view: HomeView,
        dataManager: HomeDataManager,
        tracker: HomeTracker,
        private var valueExtractor: HomeBundleValueManager,
        private val listener: OnAccountSwitchListener,
        private val router: HomeRouter,
        private val interactor: BuildLogInteractor,
        private val onboardingManager: OnboardingManager,
        private val bottomNavigationView: BottomNavigationView,
        private val filterProvider: FilterProvider
) : DrawerPresenterImpl<HomeView, HomeDataManager, DrawerRouter, HomeTracker>(view, dataManager, router, tracker), HomePresenter, OnDrawerUpdateListener, BottomNavigationView.ViewListener, HomeView.ViewListener, HomeDataManager.Listener {

    private var baseUrl: String? = null

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        start()
        super.onCreate()
        view.setListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        view.setDrawerSelection(DrawerView.HOME)

        val isRequiredToReload = valueExtractor.isRequiredToReload
        val isNewAccountCreated = valueExtractor.isNewAccountCreated

        // if new user was created
        if (isRequiredToReload) {
            dataManager.evictAllCache()
            start()
            update()
        }

        // update on every return
        if (!view.isModelEmpty && !isRequiredToReload) {
            update()
        }

        // If active user was deleted
        if (dataManager.activeUser.teamcityUrl != baseUrl) {
            dataManager.evictAllCache()
            start()
            update()
        }

        if (view.isModelEmpty) {
            router.openAccountsList()
        }

        if (isNewAccountCreated) {
            dataManager.evictAllCache()
            dataManager.clearAllWebViewCookies()
            interactor.setAuthDialogStatus(false)
        }

        // track view
        tracker.trackView()

        // Show navigation drawer prompt
        if (!onboardingManager.isNavigationDrawerPromptShown) {
            view.showNavigationDrawerPrompt(OnboardingManager.OnPromptShownListener { onboardingManager.saveNavigationDrawerPromptShown() })
        }

        // switch tab
        if (valueExtractor.isTabSelected) {
            val selectedTab = valueExtractor.selectedTab
            bottomNavigationView.selectTab(selectedTab.ordinal)
        }

        // Remove all data from bundle
        if (!valueExtractor.isNullOrEmpty) {
            valueExtractor.clear()
        }

        dataManager.subscribeToEventBusEvents()
        dataManager.setListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onPause() {
        dataManager.unsubscribeOfEventBusEvents()
        dataManager.setListener(null)
    }

    /**
     * {@inheritDoc}
     */
    override fun onNewIntent() {
        if (valueExtractor.isRequiredToReload) {
            super.onCreate()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onAccountSwitch() {
        if (valueExtractor.isRequiredToReload) {
            dataManager.initTeamCityService()
            start()
            listener.onAccountSwitch()
            dataManager.clearAllWebViewCookies()
            interactor.setAuthDialogStatus(false)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun update() {
        loadData()
        loadNotificationsCount()
    }

    /**
     * {@inheritDoc}
     */
    override fun updateRootBundleValueManager(homeBundleValueManager: HomeBundleValueManager) {
        this.valueExtractor = homeBundleValueManager
    }

    /**
     * Open root projects if active user is available
     */
    fun start() {
        baseUrl = dataManager.activeUser.teamcityUrl
        bottomNavigationView.initViews(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onTabSelected(position: Int, wasSelected: Boolean) {
        bottomNavigationView.expandToolBar()
        trackTabSelection(position)
        if (wasSelected) {
            return
        }
        val titleRes = AppNavigationItem.values()[position].title
        bottomNavigationView.setTitle(titleRes)
        if (position == AppNavigationItem.FAVORITES.ordinal) {
            showFavoritesPrompt()
            bottomNavigationView.showFavoritesFab()
        } else if (position == AppNavigationItem.RUNNING_BUILDS.ordinal || position == AppNavigationItem.BUILD_QUEUE.ordinal) {
            showFilterPrompt()
            bottomNavigationView.showFilterFab()
        } else {
            bottomNavigationView.hideFab()
        }
        loadNotificationsCount()
        view.dimissSnackbar()
    }

    private fun trackTabSelection(position: Int) {
        when (position) {
            AppNavigationItem.PROJECTS.ordinal -> tracker.trackUserSelectsProjectsTab()
            AppNavigationItem.FAVORITES.ordinal -> tracker.trackUserSelectsFavoritesTab()
            AppNavigationItem.RUNNING_BUILDS.ordinal -> tracker.trackUserSelectsRunningBuildsTab()
            AppNavigationItem.BUILD_QUEUE.ordinal -> tracker.trackUserSelectsBuildQueueTab()
        }
    }

    /**
     * Show favorites prompt
     */
    private fun showFavoritesPrompt() {
        if (!onboardingManager.isAddFavPromptShown) {
            view.showAddFavPrompt(OnboardingManager.OnPromptShownListener { onboardingManager.saveAddFavPromptShown() })
        }
    }

    /**
     * Show tab filter prompt
     */
    private fun showFilterPrompt() {
        if (!onboardingManager.isTabsFilterPromptShown) {
            view.showTabsFilterPrompt(OnboardingManager.OnPromptShownListener { onboardingManager.saveTabsFilterPromptShown() })
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onFavoritesFabClicked() {
        tracker.trackUserClickOnFavFab()
        view.showFavoritesInfoSnackbar()
    }

    /**
     * {@inheritDoc}
     */
    override fun onFilterTabsClicked(position: Int) {
        if (position == AppNavigationItem.RUNNING_BUILDS.ordinal) {
            val filter = filterProvider.runningBuildsFilter
            view.showFilterBottomSheet(filter)
            tracker.trackUserClicksOnRunningBuildsFilterFab()
        } else if (position == AppNavigationItem.BUILD_QUEUE.ordinal) {
            val filter = filterProvider.queuedBuildsFilter
            view.showFilterBottomSheet(filter)
            tracker.trackUserClicksOnBuildsQueueFilterFab()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun loadNotificationsCount() {
        super.loadNotificationsCount()
        loadRunningBuildsCount()
        loadQueueBuildsCount()
        loadFavoritesCount()
    }

    /**
     * Load running builds count
     */
    private fun loadRunningBuildsCount() {
        val currentFilter = filterProvider.runningBuildsFilter
        if (currentFilter === Filter.RUNNING_FAVORITES) {
            dataManager.loadFavoriteRunningBuildsCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int?) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.RUNNING_BUILDS.ordinal, data!!)
                }

                override fun onFail(errorMessage: String) {}
            })
        } else if (currentFilter === Filter.RUNNING_ALL) {
            dataManager.loadRunningBuildsCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int?) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.RUNNING_BUILDS.ordinal, data!!)
                }

                override fun onFail(errorMessage: String) {}
            })
        }
    }

    /**
     * Load queued builds count
     */
    private fun loadQueueBuildsCount() {
        val currentFilter = filterProvider.queuedBuildsFilter
        if (currentFilter === Filter.QUEUE_FAVORITES) {
            dataManager.loadFavoriteBuildQueueCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int?) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.BUILD_QUEUE.ordinal, data!!)
                }

                override fun onFail(errorMessage: String) {

                }
            })
        } else if (currentFilter === Filter.QUEUE_ALL) {
            dataManager.loadBuildQueueCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int?) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.BUILD_QUEUE.ordinal, data!!)
                }

                override fun onFail(errorMessage: String) {

                }
            })
        }
    }

    /**
     * Load favorites count
     */
    private fun loadFavoritesCount() {
        val favoritesCount = dataManager.favoritesCount
        bottomNavigationView.updateNotifications(AppNavigationItem.FAVORITES.ordinal, favoritesCount)
    }

    /**
     * {@inheritDoc}
     */
    override fun onFavoritesSnackBarActionClicked() {
        tracker.trackUserClicksOnFavSnackBarAction()
        bottomNavigationView.selectTab(AppNavigationItem.PROJECTS.ordinal)
    }

    /**
     * {@inheritDoc}
     *
     *
     * Pass filter here to understand what tab to update
     */
    override fun onFilterApplied(filter: Filter) {
        if (filter.isRunning) {
            loadRunningBuildsCount()
            dataManager.postRunningBuildsFilterChangedEvent()
        }
        if (filter.isQueued) {
            loadQueueBuildsCount()
            dataManager.postBuildQueueFilterChangedEvent()
        }
        view.showFilterAppliedSnackBar()
    }
}
