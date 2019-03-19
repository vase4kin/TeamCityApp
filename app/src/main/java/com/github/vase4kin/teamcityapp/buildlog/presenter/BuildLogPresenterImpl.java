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

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor;
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter;
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView;
import com.github.vase4kin.teamcityapp.buildlog.view.OnBuildLogLoadListener;

import javax.inject.Inject;

/**
 * Impl of {@link BuildLogPresenter}
 */
public class BuildLogPresenterImpl implements BuildLogPresenter, OnBuildLogLoadListener {

    private final BuildLogView view;
    private final BuildLogUrlProvider buildLogUrlProvider;
    private final BuildLogInteractor interactor;
    private final BuildLogRouter router;

    @Inject
    BuildLogPresenterImpl(@NonNull BuildLogView view,
                          BuildLogUrlProvider buildLogUrlProvider,
                          BuildLogInteractor interactor,
                          BuildLogRouter router) {
        this.view = view;
        this.buildLogUrlProvider = buildLogUrlProvider;
        this.interactor = interactor;
        this.router = router;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateViews() {
        view.initViews(this);
        if (interactor.isSslDisabled()) {
            view.showSslWarningView();
            return;
        }
        if (!interactor.isAuthDialogShown() && !interactor.isGuestUser()) {
            view.showAuthView();
        } else {
            loadBuildLog();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyViews() {
        view.unBindViews();
        router.unbindCustomsTabs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBuildLog() {
        view.loadBuildLog(buildLogUrlProvider.provideUrl());
    }

    @Override
    public void onAuthButtonClick() {
        view.hideAuthView();
        interactor.setAuthDialogStatus(true);
        loadBuildLog();
    }

    @Override
    public void onOpenBuildLogInBrowser() {
        String url = buildLogUrlProvider.provideUrl();
        router.openUrl(url);
    }
}
