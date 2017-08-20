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

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.api.Files;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;

import java.util.List;

/**
 * Data manager for {@link ArtifactDataManager}
 */
public interface ArtifactDataManager extends BaseListRxDataManager<Files, File> {

    /**
     * {@inheritDoc}
     */
    void load(@NonNull String url,
              @NonNull OnLoadingListener<List<File>> loadingListener,
              boolean update);

    /**
     * Download artifact
     *
     * @param url             - Artifact url
     * @param name            - Artifact name
     * @param loadingListener - Listener to receive server callbacks
     */
    void downloadArtifact(@NonNull String url, @NonNull String name, @NonNull OnLoadingListener<java.io.File> loadingListener);

    /**
     * Register event bus
     */
    void registerEventBus();

    /**
     * Unregister event bus
     */
    void unregisterEventBus();

    /**
     * @param listener - Listener
     */
    void setListener(OnArtifactEventListener listener);

    /**
     * Post {@link ArtifactErrorDownloadingEvent}
     */
    void postArtifactErrorDownloadingEvent();
}
