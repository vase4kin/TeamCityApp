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

package com.github.vase4kin.teamcityapp.home.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.home.router.HomeRouter
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker
import com.github.vase4kin.teamcityapp.home.view.HomeView
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
    router: HomeRouter,
    private val interactor: BuildLogInteractor,
    private val onboardingManager: OnboardingManager,
    private val bottomNavigationView: BottomNavigationView,
    private val filterProvider: FilterProvider
) : DrawerPresenterImpl<HomeView, HomeDataManager, DrawerRouter, HomeTracker>(
    view,
    dataManager,
    router,
    tracker
),
    HomePresenter, OnDrawerUpdateListener, BottomNavigationView.ViewListener, HomeView.ViewListener,
    HomeDataManager.Listener {

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
        // update on every return
        if (!view.isModelEmpty) {
            update()
        }

        // track view
        tracker.trackView()

        // Show navigation drawer prompt
        if (!onboardingManager.isNavigationDrawerPromptShown) {
            view.showNavigationDrawerPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveNavigationDrawerPromptShown()
                }
            })
        }

        dataManager.subscribeToEventBusEvents()
        dataManager.setListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun restartMatrix() {
        dataManager.evictAllCache()
        dataManager.clearAllWebViewCookies()
        interactor.setAuthDialogStatus(false)
        onCreate()
        bottomNavigationView.selectTab(AppNavigationItem.PROJECTS.ordinal)
    }

    /**
     * {@inheritDoc}
     */
    override fun selectTab(navigationItem: AppNavigationItem) {
        bottomNavigationView.selectTab(navigationItem.ordinal)
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
    override fun update() {
        loadData()
        loadNotificationsCount()
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
        } else if (position == AppNavigationItem.AGENTS.ordinal) {
            // TODO: show agents filter prompt
            bottomNavigationView.showFilterFab()
        } else {
            bottomNavigationView.hideFab()
        }
        loadNotificationsCount()
        view.dimissSnackbar()
    }

    private fun trackTabSelection(position: Int) {
        val navItem = AppNavigationItem.values()[position]
        tracker.trackTabSelected(navItem)
    }

    /**
     * Show favorites prompt
     */
    private fun showFavoritesPrompt() {
        if (!onboardingManager.isAddFavPromptShown) {
            view.showAddFavPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveAddFavPromptShown()
                }
            })
        }
    }

    /**
     * Show tab filter prompt
     */
    private fun showFilterPrompt() {
        if (!onboardingManager.isTabsFilterPromptShown) {
            view.showTabsFilterPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveTabsFilterPromptShown()
                }
            })
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
        when (position) {
            AppNavigationItem.RUNNING_BUILDS.ordinal -> {
                val filter = filterProvider.runningBuildsFilter
                view.showFilterBottomSheet(filter)
                tracker.trackUserClicksOnRunningBuildsFilterFab()
            }
            AppNavigationItem.BUILD_QUEUE.ordinal -> {
                val filter = filterProvider.queuedBuildsFilter
                view.showFilterBottomSheet(filter)
                tracker.trackUserClicksOnBuildsQueueFilterFab()
            }
            AppNavigationItem.AGENTS.ordinal -> {
                val filter = filterProvider.agentsFilter
                view.showFilterBottomSheet(filter)
                // TODO: track
            }
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
        loadAgentsCount()
    }

    /**
     * Load running builds count
     */
    private fun loadRunningBuildsCount() {
        val currentFilter = filterProvider.runningBuildsFilter
        if (currentFilter === Filter.RUNNING_FAVORITES) {
            dataManager.loadFavoriteRunningBuildsCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int) {
                    bottomNavigationView.updateNotifications(
                        AppNavigationItem.RUNNING_BUILDS.ordinal,
                        data
                    )
                }

                override fun onFail(errorMessage: String) {}
            })
        } else if (currentFilter === Filter.RUNNING_ALL) {
            dataManager.loadRunningBuildsCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int) {
                    bottomNavigationView.updateNotifications(
                        AppNavigationItem.RUNNING_BUILDS.ordinal,
                        data
                    )
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
                override fun onSuccess(data: Int) {
                    bottomNavigationView.updateNotifications(
                        AppNavigationItem.BUILD_QUEUE.ordinal,
                        data
                    )
                }

                override fun onFail(errorMessage: String) {
                }
            })
        } else if (currentFilter === Filter.QUEUE_ALL) {
            dataManager.loadBuildQueueCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int) {
                    bottomNavigationView.updateNotifications(
                        AppNavigationItem.BUILD_QUEUE.ordinal,
                        data
                    )
                }

                override fun onFail(errorMessage: String) {
                }
            })
        }
    }

    /**
     * Load running builds count
     */
    private fun loadAgentsCount() {
        val currentFilter = filterProvider.agentsFilter
        if (currentFilter === Filter.AGENTS_CONNECTED) {
            dataManager.loadAgentsCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int) {
                    bottomNavigationView.updateNotifications(
                        AppNavigationItem.AGENTS.ordinal,
                        data
                    )
                }

                override fun onFail(errorMessage: String) {}
            }, false)
        } else if (currentFilter === Filter.AGENTS_DISCONNECTED) {
            dataManager.loadAgentsCount(object : OnLoadingListener<Int> {
                override fun onSuccess(data: Int) {
                    bottomNavigationView.updateNotifications(
                        AppNavigationItem.AGENTS.ordinal,
                        data
                    )
                }

                override fun onFail(errorMessage: String) {}
            }, true)
        }
    }

    /**
     * Load favorites count
     */
    private fun loadFavoritesCount() {
        val favoritesCount = dataManager.favoritesCount
        bottomNavigationView.updateNotifications(
            AppNavigationItem.FAVORITES.ordinal,
            favoritesCount
        )
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
        if (filter.isAgents) {
            loadAgentsCount()
            // TODO: post filters have been changed
        }
        view.showFilterAppliedSnackBar()
    }
}
