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

/**
 * Listener to handle events
 */
public interface OnBuildDetailsEventsListener {

    /**
     * Fab is visible
     */
    void onShow();

    /**
     * Fab is hidden
     */
    void onHide();

    /**
     * When cancel/remove from queue build action is triggered
     */
    void onCancelBuildActionTriggered();

    /**
     * When share build action is triggered
     */
    void onShareBuildActionTriggered();

    /**
     * When restart build action is triggered
     */
    void onRestartBuildActionTriggered();
}
