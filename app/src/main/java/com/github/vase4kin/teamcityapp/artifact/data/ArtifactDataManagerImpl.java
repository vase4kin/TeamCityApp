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

package com.github.vase4kin.teamcityapp.artifact.data;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.build_details.data.OnArtifactTabChangeEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Impl of {@link ArtifactDataManager}
 */
public class ArtifactDataManagerImpl extends BaseListRxDataManagerImpl<Files, File> implements ArtifactDataManager {

    private TeamCityService mTeamCityService;
    private EventBus mEventBus;
    @Nullable
    private OnArtifactTabChangeEventListener mListener;

    public ArtifactDataManagerImpl(TeamCityService teamCityService, EventBus eventBus) {
        this.mTeamCityService = teamCityService;
        this.mEventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String url, @NonNull OnLoadingListener<List<File>> loadingListener) {
        load(mTeamCityService.listArtifacts(url, "browseArchives:true"), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downloadArtifact(@NonNull final String url, @NonNull final String name, @NonNull final OnLoadingListener<java.io.File> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = mTeamCityService.downloadFile(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        java.io.File downloadedFile = new java.io.File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
                        try {
                            BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                            sink.writeAll(response.source());
                            sink.close();
                            loadingListener.onSuccess(downloadedFile);
                        } catch (Exception e) {
                            loadingListener.onFail(e.getMessage());
                        }
                    }
                });
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerEventBus() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterEventBus() {
        mEventBus.unregister(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListener(OnArtifactTabChangeEventListener listener) {
        this.mListener = listener;
    }

    /**
     * On artifact tab change bus event
     */
    @SuppressWarnings("unused")
    public void onEvent(OnArtifactTabChangeEvent onArtifactTabChangeEvent) {
        if (mListener != null) {
            mListener.onEventHappen();
        }
    }
}
