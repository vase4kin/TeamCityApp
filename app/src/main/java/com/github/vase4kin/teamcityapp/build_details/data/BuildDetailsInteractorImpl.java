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

package com.github.vase4kin.teamcityapp.build_details.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactErrorDownloadingEvent;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManagerImpl;
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.FloatButtonChangeVisibilityEvent;
import com.github.vase4kin.teamcityapp.overview.data.RestartBuildEvent;
import com.github.vase4kin.teamcityapp.overview.data.ShareBuildEvent;
import com.github.vase4kin.teamcityapp.overview.data.StartBuildsListActivityEvent;
import com.github.vase4kin.teamcityapp.overview.data.StartBuildsListActivityFilteredByBranchEvent;
import com.github.vase4kin.teamcityapp.overview.data.StartProjectActivityEvent;
import com.github.vase4kin.teamcityapp.overview.data.StopBuildEvent;
import com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor.CODE_FORBIDDEN;

/**
 * Impl of {@link BuildDetailsInteractor}
 */
public class BuildDetailsInteractorImpl extends BaseTabsDataManagerImpl implements BuildDetailsInteractor {

    @Nullable
    private OnBuildDetailsEventsListener mListener;
    private BaseValueExtractor mValueExtractor;
    private SharedUserStorage mSharedUserStorage;
    private Repository mRepository;

    private CompositeSubscription mSubscription = new CompositeSubscription();

    public BuildDetailsInteractorImpl(@NonNull EventBus mEventBus,
                                      BaseValueExtractor valueExtractor,
                                      SharedUserStorage sharedUserStorage,
                                      Repository repository) {
        super(mEventBus);
        this.mValueExtractor = valueExtractor;
        this.mSharedUserStorage = sharedUserStorage;
        this.mRepository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnBuildTabsEventsListener(OnBuildDetailsEventsListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postRefreshOverViewDataEvent() {
        mEventBus.post(new OnOverviewRefreshDataEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuildTriggeredByMe() {
        return mValueExtractor.getBuildDetails().isTriggeredByUser(mSharedUserStorage.getActiveUser().getUserName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildDetails getBuildDetails() {
        return mValueExtractor.getBuildDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeName() {
        BuildDetails buildDetails = mValueExtractor.getBuildDetails();
        if (buildDetails.hasBuildTypeInfo() && buildDetails.getBuildTypeName() != null) {
            return buildDetails.getBuildTypeName();
        } else {
            return mValueExtractor.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectId() {
        return mValueExtractor.getBuildDetails().getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectName() {
        return mValueExtractor.getBuildDetails().getProjectName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelBuild(final LoadingListenerWithForbiddenSupport<Build> loadingListener, boolean isReAddToTheQueue) {
        mSubscription.clear();
        Subscription queueBuildSubscription = mRepository.cancelBuild(mValueExtractor.getBuildDetails().getHref(), new BuildCancelRequest(isReAddToTheQueue))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Build>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == CODE_FORBIDDEN) {
                                loadingListener.onForbiddenError();
                            } else {
                                loadingListener.onFail(e.getMessage());
                            }
                        } else {
                            loadingListener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Build build) {
                        loadingListener.onSuccess(build);
                    }
                });
        mSubscription.add(queueBuildSubscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebUrl() {
        return mValueExtractor.getBuildDetails().getWebUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubsribe() {
        mSubscription.unsubscribe();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event FloatButtonChangeVisibilityEvent
     */
    @Subscribe
    public void onEvent(FloatButtonChangeVisibilityEvent event) {
        if (mListener == null) return;
        switch (event.getVisibility()) {
            case View.VISIBLE:
                mListener.onShow();
                break;
            case View.GONE:
                mListener.onHide();
                break;
            default:
                break;
        }
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link StopBuildEvent}
     */
    @Subscribe
    public void onEvent(StopBuildEvent event) {
        if (mListener == null) return;
        mListener.onCancelBuildActionTriggered();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link ShareBuildEvent}
     */
    @Subscribe
    public void onEvent(ShareBuildEvent event) {
        if (mListener == null) return;
        mListener.onShareBuildActionTriggered();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link com.github.vase4kin.teamcityapp.overview.data.RestartBuildEvent}
     */
    @Subscribe
    public void onEvent(RestartBuildEvent event) {
        if (mListener == null) return;
        mListener.onRestartBuildActionTriggered();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent}
     */
    @Subscribe
    public void onEvent(TextCopiedEvent event) {
        if (mListener == null) return;
        mListener.onTextCopiedActionTriggered();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent}
     */
    @Subscribe
    public void onEvent(ArtifactErrorDownloadingEvent event) {
        if (mListener == null) return;
        mListener.onErrorDownloadingArtifactActionTriggered();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link StartBuildsListActivityFilteredByBranchEvent}
     */
    @Subscribe
    public void onEvent(StartBuildsListActivityFilteredByBranchEvent event) {
        if (mListener == null) return;
        mListener.onStartBuildListActivityFilteredByBranchEventTriggered(event.getBranchName());
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link StartBuildsListActivityEvent}
     */
    @Subscribe
    public void onEvent(StartBuildsListActivityEvent event) {
        if (mListener == null) return;
        mListener.onStartBuildListActivityEventTriggered();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link StartProjectActivityEvent}
     */
    @Subscribe
    public void onEvent(StartProjectActivityEvent event) {
        if (mListener == null) return;
        mListener.onStartProjectActivityEventTriggered();
    }
}
