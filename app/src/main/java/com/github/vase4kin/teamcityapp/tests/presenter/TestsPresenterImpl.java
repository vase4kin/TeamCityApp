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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModelImpl;
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor;
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter;
import com.github.vase4kin.teamcityapp.tests.view.OnTestsPresenterListener;
import com.github.vase4kin.teamcityapp.tests.view.TestsView;
import com.mugen.MugenCallbacks;

import java.util.Collections;
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
        ViewTracker,
        TestsValueExtractor> implements TestsPresenter, OnTestsPresenterListener {

    private TestsRouter mRouter;
    @VisibleForTesting
    boolean mIsLoadMoreLoading = false;

    @Inject
    TestsPresenterImpl(@NonNull TestsView view,
                       @NonNull TestsDataManager dataManager,
                       @NonNull ViewTracker tracker,
                       @NonNull TestsValueExtractor valueExtractor,
                       TestsRouter router) {
        super(view, dataManager, tracker, valueExtractor);
        this.mRouter = router;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener, boolean update) {
        dataManager.loadFailedTests(valueExtractor.getUrl(), loadingListener, update);
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
        view.setListener(this);
        view.setOnLoadMoreListener(new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                mIsLoadMoreLoading = true;
                view.addLoadMore();
                dataManager.loadMore(new OnLoadingListener<List<TestOccurrences.TestOccurrence>>() {
                    @Override
                    public void onSuccess(List<TestOccurrences.TestOccurrence> data) {
                        view.removeLoadMore();
                        view.addMoreBuilds(new TestsDataModelImpl(data));
                        mIsLoadMoreLoading = false;
                    }

                    @Override
                    public void onFail(String errorMessage) {
                        view.removeLoadMore();
                        view.showRetryLoadMoreSnackBar();
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
                return !dataManager.canLoadMore();
            }
        });
        view.replaceSkeletonViewContent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsCreated() {
        super.onViewsCreated();
        dataManager.loadTestDetails(valueExtractor.getUrl(), new OnLoadingListener<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                dataManager.postChangeTabTitleEvent(data);
            }

            @Override
            public void onFail(String errorMessage) {
            }
        });
        view.invalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        view.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        view.onPrepareOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        view.showRefreshAnimation();
        view.hideErrorView();
        view.hideEmpty();
        view.showData(new TestsDataModelImpl(Collections.<TestOccurrences.TestOccurrence>emptyList()));
        return view.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFailedTests() {
        dataManager.loadFailedTests(valueExtractor.getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSuccessTests() {
        dataManager.loadPassedTests(valueExtractor.getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadIgnoredTests() {
        dataManager.loadIgnoredTests(valueExtractor.getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFailedTestClick(String url) {
        mRouter.openFailedTest(url);
    }
}
