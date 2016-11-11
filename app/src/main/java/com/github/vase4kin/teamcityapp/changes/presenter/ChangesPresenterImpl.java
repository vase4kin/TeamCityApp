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

package com.github.vase4kin.teamcityapp.changes.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManager;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModelImpl;
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractor;
import com.github.vase4kin.teamcityapp.changes.view.ChangesView;
import com.mugen.MugenCallbacks;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter manages logic of {@link com.github.vase4kin.teamcityapp.changes.view.ChangesFragment}
 */
public class ChangesPresenterImpl extends BaseListPresenterImpl
        <ChangesDataModel, Changes.Change, ChangesView, ChangesDataManager, ChangesValueExtractor> {

    private boolean mIsLoadMoreLoading = false;

    @Inject
    ChangesPresenterImpl(@NonNull ChangesView view,
                         @NonNull ChangesDataManager dataManager,
                         @Nullable ChangesValueExtractor valueExtractor) {
        super(view, dataManager, null, valueExtractor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        mView.setLoadMoreListener(new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                mIsLoadMoreLoading = true;
                mView.addLoadMore();
                mDataManager.loadMore(new OnLoadingListener<List<Changes.Change>>() {
                    @Override
                    public void onSuccess(List<Changes.Change> data) {
                        mView.removeLoadMore();
                        mView.addMoreBuilds(new ChangesDataModelImpl(data));
                        mIsLoadMoreLoading = false;
                    }

                    @Override
                    public void onFail(String errorMessage) {
                        mView.removeLoadMore();
                        mView.showRetryLoadMoreSnackBar();
                        mIsLoadMoreLoading = false;
                    }
                });
            }

            @Override
            public boolean isLoading() {
                return mIsLoadMoreLoading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return !mDataManager.canLoadMore();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<Changes.Change>> loadingListener) {
        mDataManager.loadLimited(mValueExtractor.getUrl(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ChangesDataModel createModel(List<Changes.Change> data) {
        return new ChangesDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        super.onViewsCreated();
        mDataManager.loadTabTitle(mValueExtractor.getUrl(), new OnLoadingListener<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                mDataManager.postChangeTabTitleEvent(data);
            }

            @Override
            public void onFail(String errorMessage) {
            }
        });
    }
}
