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

package com.github.vase4kin.teamcityapp.buildlist.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModelImpl;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import java.util.List;

import javax.inject.Inject;

public class BuildListPresenterImpl<V extends BuildListView, DM extends BuildListDataManager> extends BaseListPresenterImpl<
        BuildListDataModel, Build, V, DM, BaseValueExtractor> {

    private BuildListRouter mRouter;
    private boolean mIsLoadMoreLoading = false;

    @Inject
    public BuildListPresenterImpl(@NonNull V view,
                                  @NonNull DM dataManager,
                                  @Nullable ViewTracker tracker,
                                  @NonNull BuildListRouter router,
                                  @Nullable BaseValueExtractor valueExtractor) {
        super(view, dataManager, tracker, valueExtractor);
        this.mRouter = router;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<Build>> loadingListener) {
        mDataManager.load(mValueExtractor.getId(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        if (!mValueExtractor.isBundleNull()) {
            mView.setTitle(mValueExtractor.getName());
        }
        mView.setOnBuildListPresenterListener(new OnBuildListPresenterListener() {
            @Override
            public void onClick(Build build) {
                mRouter.openBuildPage(build);
            }

            @Override
            public void onLoadMore() {
                mIsLoadMoreLoading = true;
                mView.addLoadMore();
                mDataManager.loadMore(new OnLoadingListener<List<Build>>() {
                    @Override
                    public void onSuccess(List<Build> data) {
                        mView.removeLoadMore();
                        mView.addMoreBuilds(new BuildListDataModelImpl(data));
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
    protected BuildListDataModel createModel(List<Build> data) {
        return new BuildListDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSuccessCallBack(List<Build> data) {
        super.onSuccessCallBack(data);
        mView.showRunBuildFloatActionButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFailCallBack(String errorMessage) {
        super.onFailCallBack(errorMessage);
        mView.hideRunBuildFloatActionButton();
    }
}
