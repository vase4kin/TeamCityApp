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

package com.github.vase4kin.teamcityapp.buildlog.presenter

import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView
import com.github.vase4kin.teamcityapp.buildlog.view.OnBuildLogLoadListener

import javax.inject.Inject

/**
 * Impl of [BuildLogPresenter]
 */
class BuildLogPresenterImpl @Inject constructor(
        private val view: BuildLogView,
        private val buildLogUrlProvider: BuildLogUrlProvider,
        private val interactor: BuildLogInteractor,
        private val router: BuildLogRouter) : BuildLogPresenter, OnBuildLogLoadListener {

    /**
     * {@inheritDoc}
     */
    override fun onCreateViews() {
        view.initViews(this)
        if (interactor.isSslDisabled) {
            view.showSslWarningView()
            return
        }
        if (!interactor.isAuthDialogShown && !interactor.isGuestUser) {
            view.showAuthView()
        } else {
            loadBuildLog()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroyViews() {
        view.unBindViews()
        router.unbindCustomsTabs()
    }

    /**
     * {@inheritDoc}
     */
    override fun loadBuildLog() {
        view.loadBuildLog(buildLogUrlProvider.provideUrl())
    }

    /**
     * {@inheritDoc}
     */
    override fun onAuthButtonClick() {
        view.hideAuthView()
        interactor.setAuthDialogStatus(true)
        loadBuildLog()
    }

    /**
     * {@inheritDoc}
     */
    override fun onOpenBuildLogInBrowser() {
        val url = buildLogUrlProvider.provideUrl()
        router.openUrl(url)
    }
}
