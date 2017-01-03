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

package com.github.vase4kin.teamcityapp.buildtabs.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManagerImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered;
import com.github.vase4kin.teamcityapp.buildtabs.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.overview.data.FloatButtonChangeVisibilityEvent;
import com.github.vase4kin.teamcityapp.overview.data.ShareBuildEvent;
import com.github.vase4kin.teamcityapp.overview.data.StopBuildEvent;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import de.greenrobot.event.EventBus;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor.CODE_FORBIDDEN;

/**
 * Impl of {@link BuildTabsInteractor}
 */
public class BuildTabsInteractorImpl extends BaseTabsDataManagerImpl implements BuildTabsInteractor {

    @Nullable
    private OnBuildTabsEventsListener mListener;
    private BaseValueExtractor mValueExtractor;
    private SharedUserStorage mSharedUserStorage;
    private TeamCityService mTeamCityService;

    private CompositeSubscription mSubscription = new CompositeSubscription();

    public BuildTabsInteractorImpl(@NonNull EventBus mEventBus,
                                   BaseValueExtractor valueExtractor,
                                   SharedUserStorage sharedUserStorage,
                                   TeamCityService teamCityService) {
        super(mEventBus);
        this.mValueExtractor = valueExtractor;
        this.mSharedUserStorage = sharedUserStorage;
        this.mTeamCityService = teamCityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnBuildTabsEventsListener(OnBuildTabsEventsListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postOnArtifactTabChangeEvent() {
        mEventBus.post(new OnArtifactTabChangeEvent());
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
    public boolean isBuildRunning() {
        return mValueExtractor.getBuild().isRunning();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuildTriggeredByMe() {
        Triggered triggered = mValueExtractor.getBuild().getTriggered();
        return triggered != null
                && triggered.isUser()
                && triggered.getUser() != null
                && mSharedUserStorage.getActiveUser().getUserName().equals(mValueExtractor.getBuild().getTriggered().getUser().getUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelBuild(final LoadingListenerWithForbiddenSupport<Build> loadingListener, boolean isReAddToTheQueue) {
        mSubscription.clear();
        Subscription queueBuildSubscription = mTeamCityService.cancelBuild(mValueExtractor.getBuild().getHref(), new BuildCancelRequest(isReAddToTheQueue))
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
        return mValueExtractor.getBuild().getWebUrl();
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public void onEvent(StopBuildEvent event) {
        if (mListener == null) return;
        mListener.onCancelBuildActionTriggered();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link ShareBuildEvent}
     */
    @SuppressWarnings("unused")
    public void onEvent(ShareBuildEvent event) {
        if (mListener == null) return;
        mListener.onShareBuildActionTriggered();
    }
}
