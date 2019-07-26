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

package com.github.vase4kin.teamcityapp.drawer.presenter;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModelImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.drawer.view.OnDrawerPresenterListener;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.List;

import javax.inject.Inject;

/**
 * Impl of {@link DrawerPresenter}
 */
public class DrawerPresenterImpl<
        V extends DrawerView,
        DM extends DrawerDataManager,
        DR extends DrawerRouter,
        DT extends DrawerTracker> implements DrawerPresenter, OnDrawerPresenterListener {

    protected V mView;
    protected DM mDataManager;
    protected DT mTracker;
    private DR mRouter;

    @Inject
    public DrawerPresenterImpl(@NonNull V mView,
                               @NonNull DM dataManager,
                               @NonNull DR router,
                               @NonNull DT tracker) {
        this.mView = mView;
        this.mDataManager = dataManager;
        this.mTracker = tracker;
        this.mRouter = router;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        mView.initViews(this);
        loadData();
        if (!mView.isModelEmpty()) {
            loadNotificationsCount();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        mDataManager.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackButtonPressed() {
        mView.backButtonPressed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveUser(String url, String userName) {
        mDataManager.setActiveUser(url, userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActiveProfile(String url, String userName) {
        return mDataManager.isActiveUser(url, userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerSlide() {
        loadNotificationsCount();
    }

    /**
     * Load all required counts
     */
    protected void loadNotificationsCount() {
        loadAgentsCount();
    }

    /**
     * Load drawer data
     */
    protected void loadData() {
        mDataManager.load(new OnLoadingListener<List<UserAccount>>() {
            @Override
            public void onSuccess(List<UserAccount> data) {
                mView.showData(new DrawerDataModelImpl(data));
            }

            @Override
            public void onFail(String errorMessage) {
            }
        });
    }

    /**
     * Load agents count
     */
    private void loadAgentsCount() {
        mDataManager.loadConnectedAgentsCount(new OnLoadingListener<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                mView.updateAgentsBadge(data);
            }

            @Override
            public void onFail(String errorMessage) {

            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUserChange() {
        mTracker.trackChangeAccount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startHomeActivity() {
        mRouter.startHomeActivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startRootProjectsActivityWhenSwitchingAccounts() {
        mRouter.startRootProjectsActivityWhenSwitchingAccounts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAccountListActivity() {
        mRouter.startAccountListActivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAgentActivity() {
        mRouter.startAgentActivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAboutActivity() {
        mRouter.startAboutActivity();
    }
}
