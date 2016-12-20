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

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;

/**
 * Interactor for {@link com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsActivity}
 */
public interface BuildTabsInteractor extends BaseTabsDataManager {

    /**
     * Set listener to handle floating action button visibility states
     *
     * @param listener - Listener to receive callbacks
     */
    void setOnBuildTabsEventsListener(OnBuildTabsEventsListener listener);

    /**
     * Post on artifact tab change event to {@link de.greenrobot.event.EventBus}
     */
    void postOnArtifactTabChangeEvent();

    /**
     * @return {true} if the build is running
     */
    boolean isBuildRunning();

    /**
     * @return {true} if the build was triggered by active user
     */
    boolean isBuildTriggeredByMe();

    /**
     * Cancel/Remove from queue the build
     *
     * @param loadingListener - Listener to receive callbacks on UI
     */
    void cancelBuild(LoadingListenerWithForbiddenSupport<Build> loadingListener);

    /**
     * Unsubscribe Rx subscriptions
     */
    void unsubsribe();
}
