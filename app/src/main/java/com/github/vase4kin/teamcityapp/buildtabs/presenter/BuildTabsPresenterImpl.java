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

package com.github.vase4kin.teamcityapp.buildtabs.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.base.tabs.presenter.BaseTabsPresenterImpl;
import com.github.vase4kin.teamcityapp.buildtabs.data.BuildTabsDataManager;
import com.github.vase4kin.teamcityapp.buildtabs.data.OnFloatButtonChangeVisibilityEventListener;
import com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsView;
import com.github.vase4kin.teamcityapp.buildtabs.view.OnTabUnSelectListener;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import javax.inject.Inject;

/**
 * Impl of {@link BuildTabsPresenter}
 */
public class BuildTabsPresenterImpl extends BaseTabsPresenterImpl<BuildTabsView, BuildTabsDataManager>
        implements BuildTabsPresenter, OnFloatButtonChangeVisibilityEventListener, OnTabUnSelectListener {

    @Inject
    BuildTabsPresenterImpl(@NonNull BuildTabsView view,
                           @NonNull ViewTracker tracker,
                           @NonNull BuildTabsDataManager dataManager) {
        super(view, tracker, dataManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        super.onViewsCreated();
        mView.setOnTabUnSelectListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mView.onSave(outState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mView.onRestore(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        mDataManager.setOnFloatButtonChangeVisibilityEventListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        mDataManager.setOnFloatButtonChangeVisibilityEventListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShow() {
        mView.showRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHide() {
        mView.hideRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onArtifactTabUnSelect() {
        mDataManager.postOnArtifactTabChangeEvent();
    }
}
