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

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment;

/**
 * Data manager for {@link OverviewFragment}
 */
public interface OverViewInteractor extends BaseListRxDataManager<Build, BuildElement> {

    /**
     * Set listener to handle receiving events
     *
     * @param listener - Listener
     */
    void setListener(OnOverviewEventsListener listener);

    /**
     * Load build details
     *
     * @param url             - Build ROOT_PROJECTS_URL
     * @param loadingListener - Listener to receive server callbacks
     */
    void load(@NonNull String url, @NonNull OnLoadingListener<BuildDetails> loadingListener);

    /**
     * Post {@link StopBuildEvent}
     */
    void postStopBuildEvent();

    /**
     * Post {@link ShareBuildEvent}
     */
    void postShareBuildInfoEvent();

    /**
     * Post {@link RestartBuildEvent}
     */
    void postRestartBuildEvent();

    /**
     * Subscribe to event bus events
     */
    void subscribeToEventBusEvents();

    /**
     * Unsubscribe to event bus events
     */
    void unsubsribeFromEventBusEvents();

    /**
     * Copy text to the clipboard
     *
     * @param textToCopy - text to copy
     */
    void copyTextToClipBoard(String textToCopy);

    /**
     * Post {@link FloatButtonChangeVisibilityEvent} GONE event
     */
    void postFABGoneEvent();

    /**
     * Post {@link FloatButtonChangeVisibilityEvent} VISIBLE event
     */
    void postFABVisibleEvent();

    /**
     * Post {@link StartBuildsListActivityEvent}
     */
    void postStartBuildListActivityEvent();

    /**
     * @return {@link BuildDetails} passed through intent
     */
    BuildDetails getBuildDetails();

    /**
     * Event listener
     */
    interface OnOverviewEventsListener {
        /**
         * On post data refresh event
         */
        void onDataRefreshEvent();
    }

}
