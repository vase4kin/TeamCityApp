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

package com.github.vase4kin.teamcityapp.home.presenter

import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.app_navigation.ARG_SELECTED_TAB
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker
import com.github.vase4kin.teamcityapp.home.view.HomeView
import teamcityapp.libraries.onboarding.OnboardingManager
import javax.inject.Inject

/**
 * Impl of [HomePresenter]
 */
class HomePresenterImpl @Inject constructor(
    private val view: HomeView,
    private val dataManager: HomeDataManager,
    private val tracker: HomeTracker,
    private val interactor: BuildLogInteractor,
    private val onboardingManager: OnboardingManager,
    private val bottomNavigationView: BottomNavigationView,
    private val filterProvider: FilterProvider
) : HomePresenter, BottomNavigationView.ViewListener, HomeView.ViewListener,
    HomeDataManager.Listener {

    private var baseUrl: String? = null

    /**
     * {@inheritDoc}
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        start(savedInstanceState)
        view.initViews(this)
        // Set title for Projects tab if nothing is saved
        if (savedInstanceState == null) {
            bottomNavigationView.setTitle(R.string.projects_drawer_item)
        } else {
            val selectedTab = savedInstanceState.getInt(ARG_SELECTED_TAB)
            AppNavigationItem.values().getOrNull(selectedTab)?.let {
                bottomNavigationView.setTitle(it.title)
                onTabSelected(it.ordinal)
            }
        }
        // Load notifications
        loadNotificationsCount()
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        dataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        // update on every return
        if (!dataManager.isModelEmpty) {
            loadNotificationsCount()
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
     * Open root projects if active user is available
     */
    fun start(savedInstanceState: Bundle?) {
        baseUrl = dataManager.activeUser.teamcityUrl
        bottomNavigationView.initViews(this, savedInstanceState)
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
        onTabSelected(position)
        loadNotificationsCount()
        view.dimissSnackbar()
    }

    private fun onTabSelected(position: Int) {
        when (position) {
            AppNavigationItem.FAVORITES.ordinal -> {
                showFavoritesPrompt()
                bottomNavigationView.showFavoritesFab()
            }
            AppNavigationItem.RUNNING_BUILDS.ordinal -> {
                showRunningBuildsFilterPrompt()
                bottomNavigationView.showFilterFab()
            }
            AppNavigationItem.BUILD_QUEUE.ordinal -> {
                showBuildQueueFilterPrompt()
                bottomNavigationView.showFilterFab()
            }
            AppNavigationItem.AGENTS.ordinal -> {
                showAgentsFilterPrompt()
                bottomNavigationView.showFilterFab()
            }
            else -> bottomNavigationView.hideFab()
        }
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
     * Show agents filter prompt
     */
    private fun showAgentsFilterPrompt() {
        if (!onboardingManager.isAgentsFilterPromptShown) {
            view.showAgentsFilterPrompt { onboardingManager.saveAgentsFilterPromptShown() }
        }
    }

    /**
     * Show builds queue filter prompt
     */
    private fun showBuildQueueFilterPrompt() {
        if (!onboardingManager.isBuildsQueueFilterPromptShown) {
            view.showBuildsQueueFilterPrompt { onboardingManager.saveBuildsQueueFilterPromptShown() }
        }
    }

    /**
     * Show running builds filter prompt
     */
    private fun showRunningBuildsFilterPrompt() {
        if (!onboardingManager.isRunningBuildsFilterPromptShown) {
            view.showRunningBuildsFilterPrompt(object : OnboardingManager.OnPromptShownListener {
                override fun onPromptShown() {
                    onboardingManager.saveRunningBuildsFilterPromptShown()
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
                tracker.trackUserClicksOnAgentsFilterFab()
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    private fun loadNotificationsCount() {
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
            view.showFilterAppliedSnackBar()
        }
        if (filter.isQueued) {
            loadQueueBuildsCount()
            dataManager.postBuildQueueFilterChangedEvent()
            view.showFilterAppliedSnackBar()
        }
        if (filter.isAgents) {
            loadAgentsCount()
            dataManager.postAgentsFilterChangedEvent()
            view.showAgentsFilterAppliedSnackBar()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onDrawerClick() {
        view.showDrawer()
    }
}
