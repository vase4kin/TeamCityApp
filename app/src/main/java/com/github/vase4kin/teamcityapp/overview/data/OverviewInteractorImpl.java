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

package com.github.vase4kin.teamcityapp.overview.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.build_details.data.OnOverviewRefreshDataEvent;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Impl of {@link OverViewInteractor}
 */
public class OverviewInteractorImpl extends BaseListRxDataManagerImpl<Build, BuildElement> implements OverViewInteractor {

    /**
     * Delay of posting event
     */
    private static final int DELAY = 500;

    private Repository mRepository;
    private EventBus mEventBus;
    private BaseValueExtractor mValueExtractor;
    private Context mContext;
    private OnOverviewEventsListener mListener;

    public OverviewInteractorImpl(Repository teamCityService,
                                  EventBus eventBus,
                                  BaseValueExtractor valueExtractor,
                                  Context context) {
        this.mRepository = teamCityService;
        this.mEventBus = eventBus;
        this.mValueExtractor = valueExtractor;
        this.mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListener(OnOverviewEventsListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String url,
                     @NonNull final OnLoadingListener<BuildDetails> loadingListener,
                     boolean update) {
        mSubscriptions.clear();
        Subscription subscription = mRepository.build(url, update)
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
                    public void onNext(Build response) {
                        loadingListener.onSuccess(new BuildDetailsImpl(response));
                    }
                });
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStopBuildEvent() {
        mEventBus.post(new StopBuildEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postShareBuildInfoEvent() {
        mEventBus.post(new ShareBuildEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postRestartBuildEvent() {
        mEventBus.post(new RestartBuildEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeToEventBusEvents() {
        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubsribeFromEventBusEvents() {
        mEventBus.unregister(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postFABGoneEvent() {
        mEventBus.post(new FloatButtonChangeVisibilityEvent(View.GONE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postFABVisibleEvent() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mEventBus.post(new FloatButtonChangeVisibilityEvent(View.VISIBLE));
            }
        }, DELAY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStartBuildListActivityFilteredByBranchEvent(String branchName) {
        mEventBus.post(new StartBuildsListActivityFilteredByBranchEvent(branchName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStartBuildListActivityEvent() {
        mEventBus.post(new StartBuildsListActivityEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStartProjectActivityEvent() {
        mEventBus.post(new StartProjectActivityEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildDetails getBuildDetails() {
        return mValueExtractor.getBuildDetails();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link OnOverviewRefreshDataEvent}
     */
    @Subscribe
    public void onEvent(OnOverviewRefreshDataEvent event) {
        if (mListener == null) return;
        mListener.onDataRefreshEvent();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link NavigateToBuildListFilteredByBranchEvent}
     */
    @Subscribe
    public void onEvent(NavigateToBuildListFilteredByBranchEvent event) {
        if (mListener == null) return;
        mListener.onNavigateToBuildListEvent(event.getBranchName());
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link NavigateToBuildListEvent}
     */
    @Subscribe
    public void onEvent(NavigateToBuildListEvent ignored) {
        if (mListener == null) return;
        mListener.onNavigateToBuildListEvent();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link NavigateToProjectEvent}
     */
    @Subscribe
    public void onEvent(NavigateToProjectEvent ignored) {
        if (mListener == null) return;
        mListener.onNavigateToProjectEvent();
    }

}
