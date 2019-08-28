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

package com.github.vase4kin.teamcityapp.buildlog.view

import android.graphics.Bitmap
import android.os.Handler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

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
class BuildLogWebViewClient : WebViewClient() {

    private var listener: OnBuildLogViewListener? = null

    /**
     * {@inheritDoc}
     */
    fun setListener(mListener: OnBuildLogViewListener?) {
        this.listener = mListener
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
    override fun onPageFinished(view: WebView, url: String) {
        val listener = this.listener ?: return
        listener.evaluateScript(SCRIPT)

        // TODO: Make another proper solution for wait for js complete
        Handler().postDelayed({
            listener.hideProgressWheel()
            listener.showWebView()
        }, BuildLogTimeouts.TIMEOUT_PAGE_LOADING)
    }

    /**
     * {@inheritDoc}
     */
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        val listener = this.listener ?: return
        listener.showProgressWheel()
        listener.hideWebView()
        listener.hideError()
    }

    /**
     * {@inheritDoc}
     */
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        val listener = this.listener ?: return
        listener.hideProgressWheel()
        listener.hideWebView()
        listener.showError()
    }

    /**
     * {@inheritDoc}
     */
    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        val listener = this.listener ?: return
        listener.hideProgressWheel()
        listener.hideWebView()
        listener.showError()
    }
}
