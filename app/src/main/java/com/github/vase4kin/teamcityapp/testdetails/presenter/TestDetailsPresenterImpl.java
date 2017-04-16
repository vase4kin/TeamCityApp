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

package com.github.vase4kin.teamcityapp.testdetails.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManager;
import com.github.vase4kin.teamcityapp.testdetails.extractor.TestDetailsValueExtractor;
import com.github.vase4kin.teamcityapp.testdetails.tracker.TestDetailsTracker;
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsView;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import javax.inject.Inject;

import tr.xip.errorview.ErrorView;

/**
 * Impl of {@link com.github.vase4kin.teamcityapp.tests.presenter.TestsPresenter}
 */
public class TestDetailsPresenterImpl implements TestDetailsPresenter, ErrorView.RetryListener {

    private TestDetailsView mView;
    private TestDetailsDataManager mDataManager;
    private TestDetailsTracker mTracker;
    private TestDetailsValueExtractor mValueExtractor;

    @Inject
    TestDetailsPresenterImpl(@NonNull TestDetailsView view,
                             @NonNull TestDetailsDataManager mDataManager,
                             @NonNull TestDetailsTracker tracker,
                             @NonNull TestDetailsValueExtractor valueExtractor) {
        this.mView = view;
        this.mDataManager = mDataManager;
        this.mTracker = tracker;
        this.mValueExtractor = valueExtractor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        mView.initViews(this);
        mView.showProgress();
        loadData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        mView.unBindViews();
        mDataManager.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        mView.finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        mTracker.trackView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRetry() {
        loadData();
    }

    /**
     * Load test details data
     */
    private void loadData() {
        mDataManager.loadData(new OnLoadingListener<TestOccurrences.TestOccurrence>() {
            @Override
            public void onSuccess(TestOccurrences.TestOccurrence data) {
                mView.hideProgress();
                String testDetails = data.getDetails();
                if (TextUtils.isEmpty(testDetails)) {
                    mView.showEmptyData();
                } else {
                    mView.showTestDetails(testDetails);
                }
            }

            @Override
            public void onFail(String errorMessage) {
                mView.hideProgress();
                mView.showRetryView(errorMessage);
            }
        }, mValueExtractor.getTestUrl());
    }
}
