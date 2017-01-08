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

package com.github.vase4kin.teamcityapp.buildlist.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Impl of {@link BuildInteractor}
 */
public class BuildInteractorImpl implements BuildInteractor {

    /**
     * TeamCity Rest Api instance
     */
    protected TeamCityService mTeamCityService;
    /**
     * Load build subscription to manage load builds rx operations
     */
    private CompositeSubscription mLoadBuildSubscription = new CompositeSubscription();

    public BuildInteractorImpl(TeamCityService teamCityService) {
        this.mTeamCityService = teamCityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBuild(@NonNull String href, @NonNull final OnLoadingListener<Build> loadingListener) {
        mLoadBuildSubscription.clear();
        Subscription loadBuildSubscriptions = mTeamCityService.build(href)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Build>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(Build build) {
                        loadingListener.onSuccess(build);
                    }
                });
        mLoadBuildSubscription.add(loadBuildSubscriptions);
    }

    @Override
    public void unsubscribe() {
        mLoadBuildSubscription.unsubscribe();
    }
}
