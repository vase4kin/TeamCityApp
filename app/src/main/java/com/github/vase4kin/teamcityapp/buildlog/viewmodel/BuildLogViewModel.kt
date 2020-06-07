/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.buildlog.viewmodel

import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider
import tr.xip.errorview.ErrorView

class BuildLogViewModel(
    private val buildLogUrlProvider: BuildLogUrlProvider,
    private val interactor: BuildLogInteractor,
    private val router: BuildLogRouter,
    private val loadUrl: (url: String) -> Unit,
    private val initWebView: () -> Unit
) : LifecycleObserver, ErrorView.RetryListener {

    val webViewVisibility = ObservableInt(View.GONE)
    val progressVisibility = ObservableInt(View.GONE)
    val errorVisibility = ObservableInt(View.GONE)
    val sslWarningVisibility = ObservableInt(View.GONE)
    val authViewVisibility = ObservableInt(View.GONE)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        if (interactor.isSslDisabled) {
            sslWarningVisibility.set(View.VISIBLE)
            return
        }
        initWebView()
        if (!interactor.isAuthDialogShown && !interactor.isGuestUser) {
            authViewVisibility.set(View.VISIBLE)
        } else {
            loadBuildLog()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        router.unbindCustomsTabs()
    }

    override fun onRetry() {
        loadBuildLog()
    }

    private fun loadBuildLog() {
        loadUrl(buildLogUrlProvider.provideUrl())
    }

    fun onAuthButtonClick() {
        authViewVisibility.set(View.GONE)
        interactor.setAuthDialogStatus(true)
        loadBuildLog()
    }

    fun onOpenBuildLogInBrowser() {
        val url = buildLogUrlProvider.provideUrl()
        router.openUrl(url)
    }
}
