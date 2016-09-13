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

package com.github.vase4kin.teamcityapp.base.list.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Base rx data manager impl
 * <p>
 * {@inheritDoc}
 */
public class BaseListRxDataManagerImpl<T extends Collectible<D>, D> implements BaseListRxDataManager<T, D> {

    protected CompositeSubscription mSubscriptions;

    public BaseListRxDataManagerImpl() {
        mSubscriptions = new CompositeSubscription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull Observable<T> call, @NonNull final OnLoadingListener<List<D>> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<T>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(T response) {
                        loadingListener.onSuccess(response.getObjects());
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
