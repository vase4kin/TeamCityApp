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

package com.github.vase4kin.teamcityapp.drawer.view;

import android.support.annotation.ColorRes;

import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModel;

/**
 * View interactions for drawer
 */
public interface DrawerView {

    /**
     * Drawer item ids
     */
    int PROJECTS = 0;
    int RUNNING_BUILDS = 1;
    int AGENTS = 2;
    int BUILD_QUEUE = 3;
    int NO_SELECTION = -1;
    int PROFILES_MANAGING = 4;
    int ABOUT = 5;
    int FAVORITES = 6;

    /**
     * Init views
     *
     * @param listener - Listener to receive view callbacks
     */
    void initViews(OnDrawerPresenterListener listener);

    /**
     * Show drawer data
     *
     * @param dataModel - Drawer data
     */
    void showData(DrawerDataModel dataModel);

    /**
     * On back button pressed
     */
    void backButtonPressed();

    /**
     * Update running builds badge
     *
     * @param count - Number to update
     */
    void updateRunningBuildsBadge(int count);

    /**
     * Update agents badge
     *
     * @param count - Number to update
     */
    void updateAgentsBadge(int count);

    /**
     * Update queued builds badge
     *
     * @param count - Number to update
     */
    void updateBuildQueueBadge(int count);

    /**
     * Update favorites badge
     *
     * @param count - Number to update
     */
    void updateFavoritesBadge(int count);

    /**
     * Set default color for drawer
     * @param color - Color to make default
     */
    void setDefaultColors(@ColorRes int color);

    /**
     * Is model empty
     */
    boolean isModelEmpty();
}
