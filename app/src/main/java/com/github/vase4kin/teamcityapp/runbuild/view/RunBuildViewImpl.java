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

package com.github.vase4kin.teamcityapp.runbuild.view;

import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListenerImpl;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Impl of {@link RunBuildView}
 */
public class RunBuildViewImpl implements RunBuildView {

    @BindView(R.id.fab_queue_build)
    FloatingActionButton mQueueBuildFab;
    @BindView(R.id.container)
    View mContainer;

    private MaterialDialog mProgressDialog;

    private RunBuildActivity mActivity;
    private Unbinder mUnbinder;

    public RunBuildViewImpl(RunBuildActivity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(final ViewListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);

        Toolbar mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(mToolbar);

        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_run_build);
        }

        // For ui testing purpose
        mToolbar.setNavigationContentDescription(R.string.navigate_up);
        mToolbar.setNavigationIcon(new IconDrawable(mActivity, MaterialIcons.md_close).color(Color.WHITE).actionBarSize());
        mToolbar.setNavigationOnClickListener(new OnToolBarNavigationListenerImpl(listener));

        mQueueBuildFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBuildQueue();
            }
        });

        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .content(R.string.text_queueing_build)
                .progress(true, 0)
                .autoDismiss(false)
                .build();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showQueuingBuildProgress() {
        mProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideQueuingBuildProgress() {
        mProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showForbiddenErrorSnackbar() {
        Snackbar snackBar = Snackbar.make(
                mContainer,
                R.string.error_forbidden_error,
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showErrorSnackbar() {
        Snackbar snackBar = Snackbar.make(
                mContainer,
                R.string.error_base_error,
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
        mUnbinder.unbind();
    }

}
