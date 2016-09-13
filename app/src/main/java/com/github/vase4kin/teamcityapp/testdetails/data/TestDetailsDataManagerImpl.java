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

package com.github.vase4kin.teamcityapp.testdetails.data;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Impl of {@link TestDetailsDataManager}
 */
public class TestDetailsDataManagerImpl implements TestDetailsDataManager {

    private TeamCityService mTeamCityService;

    private CompositeSubscription mSubscriptions;

    public TestDetailsDataManagerImpl(TeamCityService teamCityService) {
        this.mTeamCityService = teamCityService;
        mSubscriptions = new CompositeSubscription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData(final OnLoadingListener<TestOccurrences.TestOccurrence> loadingListener, final String url) {
        mSubscriptions.clear();
        Subscription subscription = mTeamCityService.testOccurrence(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TestOccurrences.TestOccurrence>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(TestOccurrences.TestOccurrence response) {
                        loadingListener.onSuccess(response);
                    }
                });
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe() {
        mSubscriptions.unsubscribe();
    }
}
