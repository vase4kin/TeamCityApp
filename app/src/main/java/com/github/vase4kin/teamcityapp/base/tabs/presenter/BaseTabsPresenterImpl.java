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

package com.github.vase4kin.teamcityapp.base.tabs.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager;
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEventListener;
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

import javax.inject.Inject;

/**
 * Base impl for {@link BaseTabsPresenter}
 */
public class BaseTabsPresenterImpl<
        T extends BaseTabsViewModel,
        DT extends BaseTabsDataManager,
        TR extends ViewTracker> implements BaseTabsPresenter, OnTextTabChangeEventListener {

    protected T mView;
    protected TR mTracker;
    protected DT mInteractor;

    @Inject
    public BaseTabsPresenterImpl(@NonNull T mView,
                                 @NonNull TR tracker,
                                 @NonNull DT interactor) {
        this.mView = mView;
        this.mTracker = tracker;
        this.mInteractor = interactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        mView.initViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsDestroyed() {
        mView.unBindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        mInteractor.registerEventBus();
        mInteractor.setListener(this);
        mTracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        mInteractor.unregisterEventBus();
        mInteractor.setListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdateTabTitle(int tabPosition, String newTitle) {
        mView.updateTabTitle(tabPosition, newTitle);
    }
}
