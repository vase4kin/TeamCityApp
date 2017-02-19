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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.build_details.data.OnOverviewRefreshDataEvent;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;

import de.greenrobot.event.EventBus;
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

    private TeamCityService mTeamCityService;
    private EventBus mEventBus;
    private BaseValueExtractor mValueExtractor;
    private Context mContext;
    private OnOverviewEventsListener mListener;

    public OverviewInteractorImpl(TeamCityService teamCityService,
                                  EventBus eventBus,
                                  BaseValueExtractor valueExtractor,
                                  Context context) {
        this.mTeamCityService = teamCityService;
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
    public void load(@NonNull String url, @NonNull final OnLoadingListener<BuildDetails> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = mTeamCityService.build(url)
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
    public void copyTextToClipBoard(String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", textToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mContext, R.string.build_element_copy_text, Toast.LENGTH_SHORT).show();
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
    public BuildDetails getBuildDetails() {
        return mValueExtractor.getBuildDetails();
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link OnOverviewRefreshDataEvent}
     */
    @SuppressWarnings("unused")
    public void onEvent(OnOverviewRefreshDataEvent event) {
        if (mListener == null) return;
        mListener.onDataRefreshEvent();
    }

}
