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

package com.github.vase4kin.teamcityapp.buildlog.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor;
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView;
import com.github.vase4kin.teamcityapp.buildlog.view.OnBuildLogLoadListener;

import javax.inject.Inject;

/**
 * Impl of {@link BuildLogPresenter}
 */
public class BuildLogPresenterImpl implements BuildLogPresenter, OnBuildLogLoadListener {

    private BuildLogView mView;
    private BuildLogUrlProvider mBuildLogUrlProvider;
    private BuildLogInteractor mInteractor;

    @Inject
    BuildLogPresenterImpl(@NonNull BuildLogView view,
                          BuildLogUrlProvider buildLogUrlProvider,
                          BuildLogInteractor interactor) {
        this.mView = view;
        this.mBuildLogUrlProvider = buildLogUrlProvider;
        this.mInteractor = interactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateViews() {
        mView.initViews(this);
        if (!mInteractor.isAuthDialogShown() && !mInteractor.isGuestUser()) {
            mView.showAuthView();
        } else {
            loadBuildLog();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyViews() {
        mView.unBindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBuildLog() {
        mView.loadBuildLog(mBuildLogUrlProvider.provideUrl());
    }

    @Override
    public void onAuthButtonClick() {
        mView.hideAuthView();
        mInteractor.setAuthDialogStatus(true);
        loadBuildLog();
    }
}
