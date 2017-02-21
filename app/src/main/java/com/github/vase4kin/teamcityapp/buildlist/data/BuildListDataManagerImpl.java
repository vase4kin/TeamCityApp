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
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Impl of {@link BuildListDataManager}
 */
public class BuildListDataManagerImpl extends BaseListRxDataManagerImpl<Builds, Build> implements BuildListDataManager {

    /**
     * Load more url
     */
    private String mLoadMoreUrl;
    /**
     * TeamCity Rest Api instance
     */
    protected TeamCityService mTeamCityService;

    public BuildListDataManagerImpl(TeamCityService teamCityService) {
        this.mTeamCityService = teamCityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String id, @NonNull OnLoadingListener<List<BuildDetails>> loadingListener) {
        loadBuilds(mTeamCityService.listBuilds(id, BuildListFilter.DEFAULT_FILTER_LOCATOR), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String id, BuildListFilter filter, @NonNull OnLoadingListener<List<BuildDetails>> loadingListener) {
        loadBuilds(mTeamCityService.listBuilds(id, filter.toLocator()), loadingListener);
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
    public void loadBuilds(@NonNull Observable<Builds> call, @NonNull final OnLoadingListener<List<BuildDetails>> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = call
                // converting all received builds to observables
                .flatMap(new Func1<Builds, Observable<Build>>() {
                    @Override
                    public Observable<Build> call(Builds builds) {
                        if (builds.getCount() == 0) {
                            return Observable.from(Collections.<Build>emptyList());
                        } else {
                            mLoadMoreUrl = builds.getNextHref();
                            return Observable.from(builds.getObjects());
                        }
                    }
                })
                // returning new updated build observables for each stored build already
                .flatMap(new Func1<Build, Observable<Build>>() {
                    @Override
                    public Observable<Build> call(Build build) {
                        return mTeamCityService.build(build.getHref());
                    }
                })
                // transform all builds to build details
                .flatMap(new Func1<Build, Observable<BuildDetails>>() {
                    @Override
                    public Observable<BuildDetails> call(Build build) {
                        return Observable.<BuildDetails>just(new BuildDetailsImpl(build));
                    }
                })
                // putting them all to the sorted list
                // where queued builds go first
                .toSortedList(new Func2<BuildDetails, BuildDetails, Integer>() {
                    @Override
                    public Integer call(BuildDetails build, BuildDetails build2) {
                        return (build.isQueued() == build2.isQueued())
                                ? 0
                                : (build.isQueued()
                                ? -1
                                : 1);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<BuildDetails>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(List<BuildDetails> builds) {
                        loadingListener.onSuccess(builds);
                    }
                });
        mSubscriptions.add(subscription);
    }

    /**
     * Load build count
     *
     * @param call            - Retrofit call
     * @param loadingListener - Listener to receive server callbacks
     */
    public void loadCount(Observable<Builds> call, final OnLoadingListener<Integer> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Builds>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onSuccess(0);
                    }

                    @Override
                    public void onNext(Builds response) {
                        loadingListener.onSuccess(response.getCount());
                    }
                });
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadMore(@NonNull final OnLoadingListener<List<BuildDetails>> loadingListener) {
        loadBuilds(mTeamCityService.listMoreBuilds(mLoadMoreUrl), loadingListener);
    }
}
