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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.github.vase4kin.teamcityapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import tr.xip.errorview.ErrorView;

/**
 * Impl of {@link BuildLogView}
 */
public class BuildLogViewImpl implements BuildLogView, OnBuildLogViewListener {

    @BindView(R.id.progress_wheel)
    MaterialProgressBar mProgressWheel;
    @BindView(R.id.error_view)
    ErrorView mErrorView;
    @BindView(R.id.web_view)
    WebView mWebView;
    @BindView(R.id.auth_view)
    View mAuthView;
    @BindView(R.id.warning_build_log_view)
    View mWarningView;
    @BindView(R.id.auth_button)
    Button mAuthButton;
    @BindView(R.id.view_build_log_button)
    Button mOpenBuildLogInBrowserButton;

    private Unbinder mUnbinder;

    private final View mView;
    private final BuildLogWebViewClient client;

    public BuildLogViewImpl(View mView, BuildLogWebViewClient client) {
        this.mView = mView;
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initViews(final OnBuildLogLoadListener listener) {
        mUnbinder = ButterKnife.bind(this, mView);

        mErrorView.setImageTint(Color.LTGRAY);
        mErrorView.setRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                listener.loadBuildLog();
            }
        });
        mAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAuthButtonClick();
            }
        });
        mOpenBuildLogInBrowserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOpenBuildLogInBrowser();
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        client.setListener(this);

        mWebView.setWebViewClient(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBuildLog(String buildLogUrl) {
        showProgressWheel();
        mWebView.loadUrl(buildLogUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAuthView() {
        mAuthView.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideAuthView() {
        mAuthView.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showSslWarningView() {
        mWarningView.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unBindViews() {
        mUnbinder.unbind();
        client.setListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showWebView() {
        mWebView.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideWebView() {
        mWebView.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showProgressWheel() {
        mProgressWheel.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideProgressWheel() {
        mProgressWheel.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showError() {
        mErrorView.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideError() {
        mErrorView.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluateScript(String script) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(script, null);
        } else {
            mWebView.loadUrl("javascript:" + script + "void(0);");
        }
    }

}
