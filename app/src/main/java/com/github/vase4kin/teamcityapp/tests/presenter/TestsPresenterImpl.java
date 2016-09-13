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

package com.github.vase4kin.teamcityapp.tests.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.view.OnLoadMoreListener;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModelImpl;
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor;
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter;
import com.github.vase4kin.teamcityapp.tests.view.OnTestsPresenterListener;
import com.github.vase4kin.teamcityapp.tests.view.TestsView;

import java.util.List;

import javax.inject.Inject;

/**
 * Impl of {@link TestsPresenter}
 */
public class TestsPresenterImpl extends BaseListPresenterImpl<
        TestsDataModel,
        TestOccurrences.TestOccurrence,
        TestsView,
        TestsDataManager,
        TestsValueExtractor> implements TestsPresenter, OnTestsPresenterListener {

    private TestsRouter mRouter;

    @Inject
    TestsPresenterImpl(@NonNull TestsView view,
                       @NonNull TestsDataManager dataManager,
                       @Nullable ViewTracker tracker,
                       @Nullable TestsValueExtractor valueExtractor,
                       @NonNull TestsRouter router) {
        super(view, dataManager, tracker, valueExtractor);
        this.mRouter = router;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener) {
        mDataManager.loadFailedTests(mValueExtractor.getUrl(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TestsDataModel createModel(List<TestOccurrences.TestOccurrence> data) {
        return new TestsDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        mView.setListener(this);
        mView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void loadMore() {
                mView.addLoadMoreItem();
                mDataManager.loadMore(new OnLoadingListener<List<TestOccurrences.TestOccurrence>>() {
                    @Override
                    public void onSuccess(List<TestOccurrences.TestOccurrence> data) {
                        mView.removeLoadMoreItem();
                        mView.addMoreBuilds(new TestsDataModelImpl(data));
                    }

                    @Override
                    public void onFail(String errorMessage) {
                        mView.removeLoadMoreItem();
                        mView.showRetryLoadMoreSnackBar();
                    }
                });
            }

            @Override
            public boolean isLoadedAllItems() {
                return !mDataManager.canLoadMore();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        super.onViewsCreated();
        mDataManager.loadTestDetails(mValueExtractor.getUrl(), new OnLoadingListener<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                mDataManager.postChangeTabTitleEvent(data);
            }

            @Override
            public void onFail(String errorMessage) {
            }
        });
        mView.invalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mView.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mView.onPrepareOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mView.showProgressWheel();
        mView.hideErrorView();
        mView.hideEmpty();
        mView.emptyRecyclerView();
        return mView.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFailedTests() {
        mDataManager.loadFailedTests(mValueExtractor.getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSuccessTests() {
        mDataManager.loadPassedTests(mValueExtractor.getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadIgnoredTests() {
        mDataManager.loadIgnoredTests(mValueExtractor.getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFailedTestClick(String url) {
        mRouter.openFailedTest(url);
    }
}
