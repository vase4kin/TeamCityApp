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

package com.github.vase4kin.teamcityapp.testdetails.view;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListenerImpl;
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import tr.xip.errorview.ErrorView;

/**
 * Impl of {@link TestDetailsView}
 */
public class TestDetailsViewImpl implements TestDetailsView, OnActionModeListener {

    @BindView(R.id.test_occurrence_details)
    TextView mTestDetailsTextView;
    @BindView(R.id.error_view)
    ErrorView mErrorView;
    @BindView(R.id.progress_wheel)
    MaterialProgressBar mProgressWheel;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.empty)
    TextView mEmpty;

    private Unbinder mUnbinder;

    private Activity mActivity;
    private StatusBarUtils mStatusBarUtils;

    public TestDetailsViewImpl(Activity dialogFragment, StatusBarUtils statusBarUtils) {
        this.mActivity = dialogFragment;
        this.mStatusBarUtils = statusBarUtils;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(ErrorView.RetryListener retryListener) {
        mUnbinder = ButterKnife.bind(this, mActivity);
        mErrorView.setRetryListener(retryListener);
        mErrorView.setImageTint(Color.LTGRAY);
        initToolBar();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mTestDetailsTextView.setCustomSelectionActionModeCallback(new CustomSelectionActionModeCallBackImpl(this));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showProgress() {
        mProgressWheel.setVisibility(View.VISIBLE);
        mTestDetailsTextView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
    }

    /**
     * Init toolbar
     */
    private void initToolBar() {
        mToolbar.setTitle(R.string.test_details_title);
        mToolbar.setNavigationIcon(
                new IconDrawable(mActivity, MaterialIcons.md_close).color(Color.WHITE).actionBarSize());
        mToolbar.setNavigationOnClickListener(new OnToolBarNavigationListenerImpl(new OnToolBarNavigationListener() {
            @Override
            public void onClick() {
                finish();
            }
        }));
        mToolbar.setBackgroundDrawable(new ColorDrawable(mActivity.getResources().getColor(R.color.failed_tool_bar_color)));
        mStatusBarUtils.changeStatusBarColor(mActivity, R.color.failed_tool_bar_color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideProgress() {
        mProgressWheel.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showTestDetails(String testDetails) {
        mTestDetailsTextView.setVisibility(View.VISIBLE);
        mTestDetailsTextView.setText(testDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showEmptyData() {
        mEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRetryView(String errorMessage) {
        mErrorView.setVisibility(View.VISIBLE);
        mErrorView.setSubtitle(errorMessage);
    }

    @Override
    public void unBindViews() {
        mUnbinder.unbind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() {
        mActivity.finish();
        mActivity.overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        mToolbar.setVisibility(View.INVISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        mToolbar.setVisibility(View.VISIBLE);
    }
}
