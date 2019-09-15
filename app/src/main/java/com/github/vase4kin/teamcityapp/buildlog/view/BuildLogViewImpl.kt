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

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.github.vase4kin.teamcityapp.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import tr.xip.errorview.ErrorView

/**
 * Impl of [BuildLogView]
 */
class BuildLogViewImpl(private val view: View, private val client: BuildLogWebViewClient) :
    BuildLogView, OnBuildLogViewListener {

    @BindView(R.id.progress_wheel)
    lateinit var progressWheel: MaterialProgressBar
    @BindView(R.id.error_view)
    lateinit var errorView: ErrorView
    @BindView(R.id.web_view)
    lateinit var webView: WebView
    @BindView(R.id.auth_view)
    lateinit var authView: View
    @BindView(R.id.warning_build_log_view)
    lateinit var warningView: View
    @BindView(R.id.auth_button)
    lateinit var authButton: Button
    @BindView(R.id.view_build_log_button)
    lateinit var openBuildLogInBrowserButton: Button

    private lateinit var unbinder: Unbinder

    /**
     * {@inheritDoc}
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun initViews(listener: OnBuildLogLoadListener) {
        unbinder = ButterKnife.bind(this, view)

        errorView.setImageTint(Color.LTGRAY)
        errorView.setRetryListener { listener.loadBuildLog() }
        authButton.setOnClickListener { listener.onAuthButtonClick() }
        openBuildLogInBrowserButton.setOnClickListener { listener.onOpenBuildLogInBrowser() }

        webView.settings.javaScriptEnabled = true
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        client.setListener(this)

        webView.webViewClient = client
    }

    /**
     * {@inheritDoc}
     */
    override fun loadBuildLog(buildLogUrl: String) {
        showProgressWheel()
        webView.loadUrl(buildLogUrl)
    }

    /**
     * {@inheritDoc}
     */
    override fun showAuthView() {
        authView.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun hideAuthView() {
        authView.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun showSslWarningView() {
        warningView.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun unBindViews() {
        unbinder.unbind()
        client.setListener(null)
    }

    /**
     * {@inheritDoc}
     */
    override fun showWebView() {
        if (::webView.isInitialized) {
            webView.visibility = View.VISIBLE
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun hideWebView() {
        if (::webView.isInitialized) {
            webView.visibility = View.GONE
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showProgressWheel() {
        if (::progressWheel.isInitialized) {
            progressWheel.visibility = View.VISIBLE
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun hideProgressWheel() {
        if (::progressWheel.isInitialized) {
            progressWheel.visibility = View.GONE
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showError() {
        if (::errorView.isInitialized) {
            errorView.visibility = View.VISIBLE
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun hideError() {
        if (::errorView.isInitialized) {
            errorView.visibility = View.GONE
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun evaluateScript(script: String) {
        if (::webView.isInitialized) {
            webView.evaluateJavascript(script, null)
        }
    }
}
