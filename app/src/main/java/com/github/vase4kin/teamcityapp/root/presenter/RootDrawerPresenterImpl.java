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

package com.github.vase4kin.teamcityapp.root.presenter;

import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.root.data.RootDataManager;
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManager;
import com.github.vase4kin.teamcityapp.root.router.RootRouter;
import com.github.vase4kin.teamcityapp.root.tracker.RootTracker;
import com.github.vase4kin.teamcityapp.root.view.OnAccountSwitchListener;
import com.github.vase4kin.teamcityapp.root.view.OnDrawerUpdateListener;
import com.github.vase4kin.teamcityapp.root.view.RootDrawerView;

import javax.inject.Inject;

/**
 * Impl of {@link RootDrawerPresenter}
 */
public class RootDrawerPresenterImpl extends DrawerPresenterImpl<RootDrawerView, RootDataManager, DrawerRouter, RootTracker> implements RootDrawerPresenter, OnDrawerUpdateListener, RootDrawerView.OnAppRateListener {

    private OnAccountSwitchListener mListener;
    private RootBundleValueManager mValueExtractor;
    private RootRouter mRouter;
    private BuildLogInteractor mInteractor;
    private OnboardingManager mOnboardingManager;
    private String mBaseUrl;

    @Inject
    RootDrawerPresenterImpl(RootDrawerView view,
                            RootDataManager dataManager,
                            OnAccountSwitchListener listener,
                            RootBundleValueManager valueExtractor,
                            RootRouter router,
                            BuildLogInteractor interactor,
                            RootTracker tracker,
                            OnboardingManager onboardingManager) {
        super(view, dataManager, router, tracker);
        this.mListener = listener;
        this.mValueExtractor = valueExtractor;
        this.mRouter = router;
        this.mInteractor = interactor;
        this.mOnboardingManager = onboardingManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        start();
        super.onCreate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        mView.setDrawerSelection(DrawerView.PROJECTS);

        boolean isRequiredToReload = mValueExtractor.isRequiredToReload();
        boolean isNewAccountCreated = mValueExtractor.isNewAccountCreated();
        if (!mValueExtractor.isBundleNull()) {
            mValueExtractor.removeIsNewAccountCreated();
            mValueExtractor.removeIsRequiredToReload();
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
        if (!mDataManager.getActiveUser().getTeamcityUrl().equals(mBaseUrl)) {
            mDataManager.evictAllCache();
            start();
            update();
        }

        if (mView.isModelEmpty()) {
            mRouter.openAccountsList();
        }

        if (isNewAccountCreated) {
            mDataManager.evictAllCache();
            mView.openDrawer();
            mDataManager.clearAllWebViewCookies();
            mInteractor.setAuthDialogStatus(false);
        }

        // track view
        mTracker.trackView();

        // Show rate app dialog
        mView.showAppRateDialog(this);

        // Show navigation drawer prompt
        if (!mOnboardingManager.isNavigationDrawerPromptShown()) {
            mView.showNavigationDrawerPrompt(new OnboardingManager.OnPromptShownListener() {
                @Override
                public void onPromptShown() {
                    mOnboardingManager.saveNavigationDrawerPromptShown();
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewIntent() {
        if (mValueExtractor.isRequiredToReload()) {
            super.onCreate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccountSwitch() {
        if (mValueExtractor.isRequiredToReload()) {
            mDataManager.initTeamCityService();
            start();
            mListener.onAccountSwitch();
            mDataManager.clearAllWebViewCookies();
            mInteractor.setAuthDialogStatus(false);
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
    public void updateRootBundleValueManager(RootBundleValueManager rootBundleValueManager) {
        this.mValueExtractor = rootBundleValueManager;
    }

    /**
     * Open root projects if active user is available
     */
    public void start() {
        mBaseUrl = mDataManager.getActiveUser().getTeamcityUrl();
        if (!TextUtils.isEmpty(mBaseUrl)) {
            mRouter.openRootProjects(mBaseUrl);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNeutralButtonClick() {
        mTracker.trackUserDecidedToRateTheAppLater();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNegativeButtonClick() {
        mTracker.trackUserDidNotRateTheApp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPositiveButtonClick() {
        mTracker.trackUserRatedTheApp();
    }
}
