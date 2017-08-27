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

package com.github.vase4kin.teamcityapp.base.list.view;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tr.xip.errorview.ErrorView;

/**
 * Base list view impl
 */
public abstract class BaseListViewImpl<T extends BaseDataModel, RA extends RecyclerView.Adapter> implements BaseListView<T> {

    @BindView(R.id.my_recycler_view)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.error_view)
    ErrorView mErrorView;
    @BindView(android.R.id.empty)
    TextView mEmpty;
    @BindView(R.id.progress_wheel)
    ProgressWheel mProgressWheel;

    private Unbinder mUnbinder;

    @StringRes
    private int mEmptyMessage;

    protected View mView;
    protected Activity mActivity;
    protected RA mAdapter;

    public BaseListViewImpl(View view,
                            Activity activity,
                            @StringRes int emptyMessage,
                            RA adapter) {
        this.mEmptyMessage = emptyMessage;
        this.mView = view;
        this.mActivity = activity;
        this.mAdapter = adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull ViewListener listener) {
        mUnbinder = ButterKnife.bind(this, mView);
        // <!----Setting id for testing purpose----->!
        mRecyclerView.setId(recyclerViewId());
        // <!--------------------------------------->!
        mErrorView.setImageTint(Color.LTGRAY);
        mErrorView.setRetryListener(listener);
        mEmpty.setText(mEmptyMessage);
        mSwipeRefreshLayout.setOnRefreshListener(listener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
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
    public boolean isProgressWheelShown() {
        return mProgressWheel.getVisibility() == View.VISIBLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableSwipeToRefresh() {
        mSwipeRefreshLayout.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableSwipeToRefresh() {
        mSwipeRefreshLayout.setEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRefreshAnimation() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showRefreshAnimation() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showErrorView() {
        mErrorView.setVisibility(View.VISIBLE);
        mErrorView.setSubtitle(R.string.error_view_error_text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideErrorView() {
        mErrorView.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showEmpty() {
        mEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideEmpty() {
        mEmpty.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
        mUnbinder.unbind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableRecyclerView() {
        mRecyclerView.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableRecyclerView() {
        mRecyclerView.setEnabled(false);
    }

    /**
     * Provide recycler view id for each view impl to easy determine them by Espresso
     */
    protected abstract int recyclerViewId();
}
