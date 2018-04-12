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
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

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
     * repository Api instance
     */
    protected final Repository mRepository;
    private final SharedUserStorage sharedUserStorage;

    public BuildListDataManagerImpl(Repository repository, SharedUserStorage sharedUserStorage) {
        this.mRepository = repository;
        this.sharedUserStorage = sharedUserStorage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String id,
                     @NonNull OnLoadingListener<List<BuildDetails>> loadingListener,
                     boolean update) {
        loadBuilds(mRepository.listBuilds(id, BuildListFilter.DEFAULT_FILTER_LOCATOR, update), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String id,
                     BuildListFilter filter,
                     @NonNull OnLoadingListener<List<BuildDetails>> loadingListener,
                     boolean update) {
        loadBuilds(mRepository.listBuilds(id, filter.toLocator(), update), loadingListener);
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
    public void addToFavorites(String buildTypeId) {
        sharedUserStorage.addBuildTypeToFavorites(buildTypeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromFavorites(String buildTypeId) {
        sharedUserStorage.removeBuildTypeFromFavorites(buildTypeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBuildTypeAsFavorite(String buildTypeId) {
        for (String favBuildTypeId : sharedUserStorage.getActiveUser().getBuildTypeIds()) {
            if (favBuildTypeId.equals(buildTypeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected void loadBuilds(@NonNull Observable<Builds> call,
                              @NonNull final OnLoadingListener<List<BuildDetails>> loadingListener) {
        Observable<List<BuildDetails>> buildDetailsList = getBuildDetailsObservable(call)
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
                });
        loadBuildDetailsList(buildDetailsList, loadingListener);
    }

    protected void loadBuildDetailsList(@NonNull Observable<List<BuildDetails>> call,
                                        @NonNull final OnLoadingListener<List<BuildDetails>> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = call
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
     * {@inheritDoc}
     */
    protected void loadNotSortedBuilds(@NonNull Observable<Builds> call,
                                       @NonNull final OnLoadingListener<List<BuildDetails>> loadingListener) {
        Observable<List<BuildDetails>> buildDetailsList = getBuildDetailsObservable(call)
                .toList();
        loadBuildDetailsList(buildDetailsList, loadingListener);
    }

    protected Observable<BuildDetails> getBuildDetailsObservable(@NonNull Observable<Builds> call) {
        return call
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
                        // Make sure cache is updated
                        final BuildDetails serverBuildDetails = new BuildDetailsImpl(build);
                        // If server build's running update cache immediately
                        if (serverBuildDetails.isRunning()) {
                            return mRepository.build(build.getHref(), true);
                        } else {
                            // Call cache
                            return mRepository.build(build.getHref(), false)
                                    .flatMap(new Func1<Build, Observable<Build>>() {
                                        @Override
                                        public Observable<Build> call(Build build) {
                                            BuildDetails cacheBuildDetails = new BuildDetailsImpl(build);
                                            // Compare if server side and cache are updated
                                            // If cache's not updated -> update it
                                            return mRepository.build(
                                                    build.getHref(),
                                                    // Don't update cache if server and cache builds are finished
                                                    !(serverBuildDetails.isFinished() == cacheBuildDetails.isFinished()));
                                        }
                                    });
                        }
                    }
                })
                // transform all builds to build details
                .flatMap(new Func1<Build, Observable<BuildDetails>>() {
                    @Override
                    public Observable<BuildDetails> call(Build build) {
                        return Observable.<BuildDetails>just(new BuildDetailsImpl(build));
                    }
                });
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
        loadBuilds(mRepository.listMoreBuilds(mLoadMoreUrl), loadingListener);
    }
}
