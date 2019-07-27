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

package com.github.vase4kin.teamcityapp.home.presenter;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem;
import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView;
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.Filter;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.FilterProvider;
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager;
import com.github.vase4kin.teamcityapp.home.extractor.HomeBundleValueManager;
import com.github.vase4kin.teamcityapp.home.router.HomeRouter;
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker;
import com.github.vase4kin.teamcityapp.home.view.HomeView;
import com.github.vase4kin.teamcityapp.home.view.OnAccountSwitchListener;
import com.github.vase4kin.teamcityapp.home.view.OnDrawerUpdateListener;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

/**
 * Impl of {@link HomePresenter}
 */
public class HomePresenterImpl extends DrawerPresenterImpl<HomeView, HomeDataManager, DrawerRouter, HomeTracker> implements HomePresenter, OnDrawerUpdateListener, BottomNavigationView.ViewListener, HomeView.ViewListener, HomeDataManager.Listener {

    private final OnAccountSwitchListener listener;
    private HomeBundleValueManager valueExtractor;
    private final HomeRouter router;
    private final BuildLogInteractor interactor;
    private final OnboardingManager onboardingManager;
    private final BottomNavigationView bottomNavigationView;
    private final FilterProvider filterProvider;
    private String baseUrl;

    @Inject
    HomePresenterImpl(HomeView view,
                      HomeDataManager dataManager,
                      OnAccountSwitchListener listener,
                      HomeBundleValueManager valueExtractor,
                      HomeRouter router,
                      BuildLogInteractor interactor,
                      HomeTracker tracker,
                      OnboardingManager onboardingManager,
                      BottomNavigationView bottomNavigationView,
                      FilterProvider filterProvider) {
        super(view, dataManager, router, tracker);
        this.listener = listener;
        this.valueExtractor = valueExtractor;
        this.router = router;
        this.interactor = interactor;
        this.onboardingManager = onboardingManager;
        this.bottomNavigationView = bottomNavigationView;
        this.filterProvider = filterProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        start();
        super.onCreate();
        mView.setListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        mView.setDrawerSelection(DrawerView.HOME);

        final boolean isRequiredToReload = valueExtractor.isRequiredToReload();
        final boolean isNewAccountCreated = valueExtractor.isNewAccountCreated();
        if (!valueExtractor.isBundleNullOrEmpty()) {
            valueExtractor.removeIsNewAccountCreated();
            valueExtractor.removeIsRequiredToReload();
        }

        // TODO: Simplify logic and cover it by unit tests

        // if new user was created
        if (isRequiredToReload) {
            mDataManager.evictAllCache();
            start();
            update();
        }

        // update on every return
        if (!mView.isModelEmpty() && !isRequiredToReload) {
            update();
        }

        // If active user was deleted
        if (!mDataManager.getActiveUser().getTeamcityUrl().equals(baseUrl)) {
            mDataManager.evictAllCache();
            start();
            update();
        }

        if (mView.isModelEmpty()) {
            router.openAccountsList();
        }

        if (isNewAccountCreated) {
            mDataManager.evictAllCache();
            mDataManager.clearAllWebViewCookies();
            interactor.setAuthDialogStatus(false);
        }

        // track view
        mTracker.trackView();

        // Show navigation drawer prompt
        if (!onboardingManager.isNavigationDrawerPromptShown()) {
            mView.showNavigationDrawerPrompt(onboardingManager::saveNavigationDrawerPromptShown);
        }

        mDataManager.subscribeToEventBusEvents();
        mDataManager.setListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        mDataManager.unsubscribeOfEventBusEvents();
        mDataManager.setListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewIntent() {
        if (valueExtractor.isRequiredToReload()) {
            super.onCreate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccountSwitch() {
        if (valueExtractor.isRequiredToReload()) {
            mDataManager.initTeamCityService();
            start();
            listener.onAccountSwitch();
            mDataManager.clearAllWebViewCookies();
            interactor.setAuthDialogStatus(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        loadData();
        loadNotificationsCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRootBundleValueManager(@NonNull HomeBundleValueManager homeBundleValueManager) {
        this.valueExtractor = homeBundleValueManager;
    }

    /**
     * Open root projects if active user is available
     */
    public void start() {
        baseUrl = mDataManager.getActiveUser().getTeamcityUrl();
        bottomNavigationView.initViews(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTabSelected(int position, boolean wasSelected) {
        bottomNavigationView.expandToolBar();
        if (wasSelected) {
            return;
        }
        int titleRes = new ArrayList<>(Arrays.asList(AppNavigationItem.values())).get(position).getTitle();
        bottomNavigationView.setTitle(titleRes);
        if (position == AppNavigationItem.FAVORITES.ordinal()) {
            showFavoritesPrompt();
            bottomNavigationView.showFavoritesFab();
        } else if (position == AppNavigationItem.RUNNING_BUILDS.ordinal()) {
            // Show running builds filter
            bottomNavigationView.showFilterFab();
        } else if (position == AppNavigationItem.BUILD_QUEUE.ordinal()) {
            // Show build queue filter
            bottomNavigationView.showFilterFab();
        } else {
            bottomNavigationView.hideFab();
        }
        loadNotificationsCount();
        mView.dimissSnackbar();
    }

    private void showFavoritesPrompt() {
        if (!onboardingManager.isAddFavPromptShown()) {
            mView.showAddFavPrompt(onboardingManager::saveAddFavPromptShown);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFavoritesFabClicked() {
        mView.showFavoritesInfoSnackbar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFilterTabsClicked(int position) {
        if (position == AppNavigationItem.RUNNING_BUILDS.ordinal()) {
            Filter filter = filterProvider.getRunningBuildsFilter();
            mView.showFilterBottomSheet(filter);
        } else if (position == AppNavigationItem.BUILD_QUEUE.ordinal()) {
            Filter filter = filterProvider.getQueuedBuildsFilter();
            mView.showFilterBottomSheet(filter);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadNotificationsCount() {
        super.loadNotificationsCount();
        loadRunningBuildsCount();
        loadQueueBuildsCount();
        loadFavoritesCount();
    }

    /**
     * Load running builds count
     */
    private void loadRunningBuildsCount() {
        Filter currentFilter = filterProvider.getRunningBuildsFilter();
        if (currentFilter == Filter.RUNNING_FAVORITES) {
            mDataManager.loadFavoriteRunningBuildsCount(new OnLoadingListener<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.RUNNING_BUILDS.ordinal(), data);
                }

                @Override
                public void onFail(String errorMessage) {
                }
            });
        } else if (currentFilter == Filter.RUNNING_ALL) {
            mDataManager.loadRunningBuildsCount(new OnLoadingListener<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.RUNNING_BUILDS.ordinal(), data);
                }

                @Override
                public void onFail(String errorMessage) {
                }
            });
        }
    }

    /**
     * Load queued builds count
     */
    private void loadQueueBuildsCount() {
        Filter currentFilter = filterProvider.getQueuedBuildsFilter();
        if (currentFilter == Filter.QUEUE_FAVORITES) {
            mDataManager.loadFavoriteBuildQueueCount(new OnLoadingListener<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.BUILD_QUEUE.ordinal(), data);
                }

                @Override
                public void onFail(String errorMessage) {

                }
            });
        } else if (currentFilter == Filter.QUEUE_ALL) {
            mDataManager.loadBuildQueueCount(new OnLoadingListener<Integer>() {
                @Override
                public void onSuccess(Integer data) {
                    bottomNavigationView.updateNotifications(AppNavigationItem.BUILD_QUEUE.ordinal(), data);
                }

                @Override
                public void onFail(String errorMessage) {

                }
            });
        }
    }

    /**
     * Load favorites count
     */
    private void loadFavoritesCount() {
        int favoritesCount = mDataManager.getFavoritesCount();
        bottomNavigationView.updateNotifications(AppNavigationItem.FAVORITES.ordinal(), favoritesCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFavoritesSnackBarActionClicked() {
        bottomNavigationView.selectProjectsTab();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Pass filter here to understand what tab to update
     */
    @Override
    public void onFilterApplied(Filter filter) {
        if (filter.isRunning()) {
            loadRunningBuildsCount();
        }
        if (filter.isQueued()) {
            loadQueueBuildsCount();
        }
        mView.showFilterAppliedSnackBar();
    }
}
