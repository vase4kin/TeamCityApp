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

package com.github.vase4kin.teamcityapp.base.tabs.presenter;

import androidx.annotation.NonNull;

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

    protected T view;
    protected TR tracker;
    protected DT interactor;

    @Inject
    public BaseTabsPresenterImpl(@NonNull T view,
                                 @NonNull TR tracker,
                                 @NonNull DT interactor) {
        this.view = view;
        this.tracker = tracker;
        this.interactor = interactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        view.initViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsDestroyed() {
        view.unBindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        interactor.registerEventBus();
        interactor.setListener(this);
        tracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        interactor.unregisterEventBus();
        interactor.setListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdateTabTitle(int tabPosition, String newTitle) {
        view.updateTabTitle(tabPosition, newTitle);
    }
}
