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

package com.github.vase4kin.teamcityapp.tests.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent;
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenter;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Impl of {@link TestsDataManager}
 */
public class TestsDataManagerImpl extends BaseListRxDataManagerImpl<TestOccurrences, TestOccurrences.TestOccurrence> implements TestsDataManager {

    private String mLoadMoreUrl;
    private OnLoadingListener<List<TestOccurrences.TestOccurrence>> mLoadingListener;

    private Subscription mLoadTestsDetailsSubscription;
    private Subscription mLoadTestsSubscription;

    private Repository mRepository;
    private EventBus mEventBus;

    public TestsDataManagerImpl(Repository repository, EventBus eventBus) {
        this.mRepository = repository;
        this.mEventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String url,
                     @NonNull OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener,
                     boolean update) {
        mLoadingListener = loadingListener;
        loadTests(url, loadingListener, update);
    }

    /**
     * Load test
     *
     * @param url             - Tests url
     * @param update          - Force cache update
     * @param loadingListener - Listener to receive server callbacks
     */
    private void loadTests(@NonNull String url,
                           @NonNull final OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener,
                           boolean update) {
        if (mLoadTestsSubscription != null) {
            mSubscriptions.remove(mLoadTestsSubscription);
        }
        mLoadTestsSubscription = mRepository.listTestOccurrences(url, update)
                .flatMap(new Func1<TestOccurrences, Observable<TestOccurrences.TestOccurrence>>() {
                    @Override
                    public Observable<TestOccurrences.TestOccurrence> call(TestOccurrences testOccurrences) {
                        mLoadMoreUrl = testOccurrences.getNextHref();
                        return Observable.from(testOccurrences.getObjects());
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<TestOccurrences.TestOccurrence>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(List<TestOccurrences.TestOccurrence> response) {
                        loadingListener.onSuccess(response);
                    }
                });
        mSubscriptions.add(mLoadTestsSubscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFailedTests(@NonNull String url,
                                @NonNull OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener,
                                boolean update) {
        load(url + ",status:FAILURE,count:10", loadingListener, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFailedTests(@NonNull String url) {
        loadFailedTests(url, mLoadingListener, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadIgnoredTests(@NonNull String url) {
        load(url + ",status:UNKNOWN,count:10", mLoadingListener, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadPassedTests(@NonNull String url) {
        load(url + ",status:SUCCESS,count:10", mLoadingListener, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadTestDetails(@NonNull final String url, @NonNull final OnLoadingListener<Integer> loadingListener) {
        if (mLoadTestsDetailsSubscription != null) {
            mSubscriptions.remove(mLoadTestsDetailsSubscription);
        }
        mLoadTestsDetailsSubscription = mRepository.listTestOccurrences(url + ",count:" + Integer.MAX_VALUE + "&fields=count", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TestOccurrences>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onSuccess(0);
                    }

                    @Override
                    public void onNext(TestOccurrences response) {
                        loadingListener.onSuccess(response.getCount());
                    }
                });
        mSubscriptions.add(mLoadTestsDetailsSubscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadMore(@NonNull final OnLoadingListener<List<TestOccurrences.TestOccurrence>> loadingListener) {
        loadTests(mLoadMoreUrl, loadingListener, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canLoadMore() {
        return mLoadMoreUrl != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postChangeTabTitleEvent(Integer size) {
        mEventBus.post(new OnTextTabChangeEvent(size, BuildDetailsPresenter.TESTS_TAB));
    }
}
