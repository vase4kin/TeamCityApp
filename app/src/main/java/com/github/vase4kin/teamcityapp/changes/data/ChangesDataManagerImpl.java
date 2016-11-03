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

package com.github.vase4kin.teamcityapp.changes.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent;
import com.github.vase4kin.teamcityapp.buildtabs.presenter.BuildTabsPresenter;
import com.github.vase4kin.teamcityapp.changes.api.Changes;

import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Impl of {@link ChangesDataManager}
 */
public class ChangesDataManagerImpl extends BaseListRxDataManagerImpl<Changes, Changes.Change> implements ChangesDataManager {

    private TeamCityService mTeamCityService;
    private EventBus mEventBus;

    private String mLoadMoreUrl;

    private Subscription mLoadTabSubscription;
    private Subscription mLoadSubscription;

    public ChangesDataManagerImpl(TeamCityService teamCityService, EventBus eventBus) {
        this.mTeamCityService = teamCityService;
        this.mEventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadTabTitle(@NonNull final String url, @NonNull final OnLoadingListener<Integer> loadingListener) {
        if (mLoadTabSubscription != null) {
            mSubscriptions.remove(mLoadTabSubscription);
        }
        mLoadTabSubscription = mTeamCityService.listChanges(url + ",count:" + Integer.MAX_VALUE + "&fields=count")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Changes>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onSuccess(0);
                    }

                    @Override
                    public void onNext(Changes response) {
                        loadingListener.onSuccess(response.getCount());
                    }
                });
        mSubscriptions.add(mLoadTabSubscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadLimited(@NonNull String url, @NonNull OnLoadingListener<List<Changes.Change>> loadingListener) {
        load(url + ",count:10", loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String url, @NonNull final OnLoadingListener<List<Changes.Change>> loadingListener) {
        if (mLoadSubscription != null) {
            mSubscriptions.remove(mLoadSubscription);
        }
        mLoadSubscription = mTeamCityService.listChanges(url)
                .flatMap(new Func1<Changes, Observable<Changes.Change>>() {
                    @Override
                    public Observable<Changes.Change> call(Changes changes) {
                        if (changes.getCount() == 0) {
                            return Observable.from(Collections.<Changes.Change>emptyList());
                        } else {
                            mLoadMoreUrl = changes.getNextHref();
                            return Observable.from(changes.getObjects());
                        }
                    }
                })
                .flatMap(new Func1<Changes.Change, Observable<Changes.Change>>() {
                    @Override
                    public Observable<Changes.Change> call(Changes.Change change) {
                        return mTeamCityService.change(change.getHref());
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Changes.Change>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(List<Changes.Change> changes) {
                        loadingListener.onSuccess(changes);
                    }
                });
        mSubscriptions.add(mLoadSubscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadMore(@NonNull final OnLoadingListener<List<Changes.Change>> loadingListener) {
        load(mLoadMoreUrl, loadingListener);
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
        mEventBus.post(new OnTextTabChangeEvent(size, BuildTabsPresenter.CHANGES_TAB));
    }
}
