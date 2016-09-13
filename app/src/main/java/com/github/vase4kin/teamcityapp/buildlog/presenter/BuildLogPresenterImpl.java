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

import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogViewModel;
import com.github.vase4kin.teamcityapp.buildlog.view.OnBuildLogLoadListener;

import javax.inject.Inject;

/**
 * Impl of {@link BuildLogPresenter}
 */
public class BuildLogPresenterImpl implements BuildLogPresenter, OnBuildLogLoadListener {

    private BuildLogViewModel mViewModel;
    private BuildLogUrlProvider mBuildLogUrlProvider;

    @Inject
    BuildLogPresenterImpl(@NonNull BuildLogViewModel mViewModel,
                          BuildLogUrlProvider buildLogUrlProvider) {
        this.mViewModel = mViewModel;
        this.mBuildLogUrlProvider = buildLogUrlProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateViews() {
        mViewModel.initViews(this);
        loadBuildLog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyViews() {
        mViewModel.unBindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBuildLog() {
        mViewModel.loadBuildLog(mBuildLogUrlProvider.provideUrl());
    }
}
