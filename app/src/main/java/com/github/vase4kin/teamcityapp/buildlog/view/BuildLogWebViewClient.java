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

package com.github.vase4kin.teamcityapp.buildlog.view;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Build log web client with listener to receive client callbacks
 */
public class BuildLogWebViewClient extends WebViewClient {

    /**
     * Magic awesome js script to hide things user doesn't need to see
     */
    private static final String SCRIPT = "$('topWrapper').style.display='none';\n" +
            "document.getElementsByClassName('tabsTable')[0].style.display='none';\n" +
            "document.getElementById('mainNavigation').style.display='none';\n" +
            "document.getElementsByClassName('footerMainContainer')[0].style.display='none';\n" +
            "document.getElementsByClassName('subTabsRight')[0].style.display='none';\n" +
            "document.body.className = '';";

    @Nullable
    private OnBuildLogViewListener mListener;

    /**
     * {@inheritDoc}
     */
    public void setListener(@Nullable OnBuildLogViewListener mListener) {
        this.mListener = mListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (mListener == null) return;
        mListener.evaluateScript(SCRIPT);

        // TODO: Make another proper solution for wait for js complete
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mListener == null) return;
                mListener.hideProgressWheel();
                mListener.showWebView();
            }
        }, BuildLogTimeouts.TIMEOUT_PAGE_LOADING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (mListener == null) return;
        mListener.showProgressWheel();
        mListener.hideWebView();
        mListener.hideError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (mListener == null) return;
        mListener.hideProgressWheel();
        mListener.hideWebView();
        mListener.showError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        if (mListener == null) return;
        mListener.hideProgressWheel();
        mListener.hideWebView();
        mListener.showError();
    }
}
