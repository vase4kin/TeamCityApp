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

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;

/**
 * Interactor for {@link BuildDetailsActivity}
 */
public interface BuildDetailsInteractor extends BaseTabsDataManager {

    /**
     * Set listener to handle floating action button visibility states
     *
     * @param listener - Listener to receive callbacks
     */
    void setOnBuildTabsEventsListener(OnBuildDetailsEventsListener listener);

    /**
     * Post {@link OnOverviewRefreshDataEvent}
     */
    void postRefreshOverViewDataEvent();

    /**
     * @return {true} if the build was triggered by active user
     */
    boolean isBuildTriggeredByMe();

    /**
     * @return build details
     */
    BuildDetails getBuildDetails();

    /**
     * @return build type name
     */
    String getBuildTypeName();

    /**
     * Cancel/Remove from queue the build
     *
     * @param loadingListener   - Listener to receive callbacks on UI
     * @param isReAddToTheQueue - Re-add build to the queue flag
     */
    void cancelBuild(LoadingListenerWithForbiddenSupport<Build> loadingListener, boolean isReAddToTheQueue);

    /**
     * return {String} web url to share build
     */
    String getWebUrl();

    /**
     * Unsubscribe Rx subscriptions
     */
    void unsubsribe();
}
