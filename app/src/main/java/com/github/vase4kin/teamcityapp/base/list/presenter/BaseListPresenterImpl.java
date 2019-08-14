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

package com.github.vase4kin.teamcityapp.base.list.presenter;

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.api.Jsonable;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

import java.util.Collections;
import java.util.List;

/**
 * Base impl of {@link BaseListPresenter}
 */
public abstract class BaseListPresenterImpl<
        T extends BaseDataModel,
        S extends Jsonable,
        VM extends BaseListView,
        DM extends BaseListRxDataManager,
        VT extends ViewTracker,
        BE extends BaseValueExtractor> implements BaseListPresenter {

    @NonNull
    protected VM view;
    @NonNull
    protected DM dataManager;
    @NonNull
    protected VT tracker;
    @NonNull
    protected BE valueExtractor;

    /**
     * On loading listener
     */
    protected OnLoadingListener<List<S>> loadingListener = new OnLoadingListener<List<S>>() {
        @Override
        public void onSuccess(List<S> data) {
            onSuccessCallBack(data);
        }

        @Override
        public void onFail(@NonNull String errorMessage) {
            onFailCallBack(errorMessage);
        }
    };

    public BaseListPresenterImpl(
            @NonNull VM view,
            @NonNull DM dataManager,
            @NonNull VT tracker,
            @NonNull BE valueExtractor) {
        this.view = view;
        this.dataManager = dataManager;
        this.tracker = tracker;
        this.valueExtractor = valueExtractor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        initViews();
        view.showSkeletonView();
        view.disableSwipeToRefresh();
        loadDataOnViewsCreated();
    }

    protected void loadDataOnViewsCreated() {
        loadData(loadingListener, false);
    }

    /**
     * Use this one to load data with DataManager
     *
     * @param loadingListener - Receive server callbacks
     * @param update          - Force cache update (if it's supported)
     */
    protected abstract void loadData(@NonNull OnLoadingListener<List<S>> loadingListener, boolean update);

    /**
     * Init views, register listeners
     */
    protected void initViews() {
        BaseListView.ViewListener listener = new BaseListView.ViewListener() {
            @Override
            public void onRefresh() {
                onSwipeToRefresh();
            }

            @Override
            public void onRetry() {
                view.showRefreshAnimation();
                onSwipeToRefresh();
            }
        };
        view.initViews(listener);
    }

    /**
     * On swipe to refresh
     */
    protected void onSwipeToRefresh() {
        view.hideErrorView();
        view.hideEmpty();
        loadData(loadingListener, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsDestroyed() {
        view.unbindViews();
        dataManager.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        tracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected void onSuccessCallBack(List<S> data) {
        view.hideErrorView();
        if (data.isEmpty()) {
            view.showEmpty();

        } else {
            view.hideEmpty();
        }
        view.showData(createModel(data));

        onCompleteLoading();
    }

    /**
     * Create data model for list
     *
     * @return Data model
     */
    protected abstract T createModel(List<S> data);

    protected void onFailCallBack(String errorMessage) {
        view.showData(createModel(Collections.emptyList()));
        view.hideEmpty();
        view.showErrorView();

        onCompleteLoading();
    }

    /**
     * Base views interaction on server request completion
     */
    private void onCompleteLoading() {
        if (view.isSkeletonViewVisible()) {
            view.hideSkeletonView();
        }

        view.enableSwipeToRefresh();

        view.hideRefreshAnimation();
    }
}
