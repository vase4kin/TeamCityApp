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

package com.github.vase4kin.teamcityapp.buildlog.view

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.github.vase4kin.teamcityapp.buildlog.viewmodel.BuildLogViewModel

/**
 * Magic awesome js script to hide things user doesn't need to see
 */
private const val SCRIPT = "$('topWrapper').style.display='none';\n" +
    "document.getElementsByClassName('tabsTable')[0].style.display='none';\n" +
    "document.getElementById('mainNavigation').style.display='none';\n" +
    "document.getElementsByClassName('footerMainContainer')[0].style.display='none';\n" +
    "document.getElementsByClassName('subTabsRight')[0].style.display='none';\n" +
    "document.body.className = '';"

/**
 * Build log web client with listener to receive client callbacks
 */
class BuildLogWebViewClient(
    private val viewModel: BuildLogViewModel,
    private val evaluateJs: (script: String) -> Unit
) : WebViewClient(), LifecycleObserver {

    private val handler = Handler(Looper.getMainLooper())

    private val runnable: Runnable = Runnable {
        viewModel.progressVisibility.set(View.GONE)
        viewModel.webViewVisibility.set(View.VISIBLE)
    }

    /**
     * {@inheritDoc}
     */
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return false
    }

    /**
     * {@inheritDoc}
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        evaluateJs(SCRIPT)

        // TODO: Make another proper solution for wait for js complete
        handler.postDelayed({
            viewModel.progressVisibility.set(View.GONE)
            viewModel.webViewVisibility.set(View.VISIBLE)
        }, BuildLogTimeouts.TIMEOUT_PAGE_LOADING)
    }

    /**
     * {@inheritDoc}
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        viewModel.progressVisibility.set(View.VISIBLE)
        viewModel.webViewVisibility.set(View.GONE)
        viewModel.errorVisibility.set(View.GONE)
    }

    /**
     * {@inheritDoc}
     */
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        viewModel.progressVisibility.set(View.GONE)
        viewModel.webViewVisibility.set(View.GONE)
        viewModel.errorVisibility.set(View.VISIBLE)
    }

    /**
     * {@inheritDoc}
     */
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        viewModel.progressVisibility.set(View.GONE)
        viewModel.webViewVisibility.set(View.GONE)
        viewModel.errorVisibility.set(View.VISIBLE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun removeCallbacks() {
        handler.removeCallbacks(runnable)
    }
}
