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

package com.github.vase4kin.teamcityapp.base.list.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.api.Jsonable;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import java.util.Collections;
import java.util.List;

import tr.xip.errorview.ErrorView;

/**
 * Base impl of {@link BaseListPresenter}
 */
public abstract class BaseListPresenterImpl<
        T extends BaseDataModel,
        S extends Jsonable,
        VM extends BaseListView,
        DM extends BaseListRxDataManager,
        BE extends BaseValueExtractor> implements BaseListPresenter {

    @NonNull
    protected VM mView;
    @NonNull
    protected DM mDataManager;
    @Nullable
    protected ViewTracker mTracker;
    @Nullable
    protected BE mValueExtractor;

    /**
     * On loading listener
     */
    private OnLoadingListener<List<S>> loadingListener = new OnLoadingListener<List<S>>() {
        @Override
        public void onSuccess(List<S> data) {
            onSuccessCallBack(data);
        }

        @Override
        public void onFail(String errorMessage) {
            onFailCallBack(errorMessage);
        }
    };

    public BaseListPresenterImpl(
            @NonNull VM view,
            @NonNull DM dataManager,
            @Nullable ViewTracker tracker,
            @Nullable BE valueExtractor) {
        this.mView = view;
        this.mDataManager = dataManager;
        this.mTracker = tracker;
        this.mValueExtractor = valueExtractor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        initViews();
        mView.showProgressWheel();
        mView.disableSwipeToRefresh();
        loadData(loadingListener);
    }

    /**
     * Use this one to load data with DataManager
     *
     * @param loadingListener - Receive server callbacks
     */
    protected abstract void loadData(@NonNull OnLoadingListener<List<S>> loadingListener);

    /**
     * Init views, register listeners
     */
    protected void initViews() {
        ErrorView.RetryListener retryListener = new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                mView.showProgressWheel();
                mView.hideErrorView();
                mView.hideEmpty();
                loadData(loadingListener);
            }
        };
        SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mView.hideErrorView();
                mView.hideEmpty();
                loadData(loadingListener);
            }
        };
        mView.initViews(retryListener, refreshListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsDestroyed() {
        mView.unbindViews();
        mDataManager.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        if (mTracker != null) {
            mTracker.trackView();
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected void onSuccessCallBack(List<S> data) {
        mView.hideErrorView();
        if (data.isEmpty()) {
            mView.showEmpty();

        } else {
            mView.hideEmpty();
        }
        mView.showData(createModel(data));

        onCompleteLoading();
    }

    /**
     * Create data model for list
     *
     * @return Data model
     */
    protected abstract T createModel(List<S> data);

    protected void onFailCallBack(String errorMessage) {
        mView.showData(createModel(Collections.<S>emptyList()));
        mView.hideEmpty();
        mView.showErrorView(errorMessage);

        onCompleteLoading();
    }

    /**
     * Base views interaction on server request completion
     */
    private void onCompleteLoading() {
        if (mView.isProgressWheelShown()) {
            mView.hideProgressWheel();
        }

        mView.enableSwipeToRefresh();

        mView.hideRefreshAnimation();
    }
}
