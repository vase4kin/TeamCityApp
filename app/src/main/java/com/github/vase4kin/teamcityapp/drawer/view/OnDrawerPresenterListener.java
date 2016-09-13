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

/**
 * Listener to handle drawer interactions and receive callbacks from them on {@link com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl}
 */
public interface OnDrawerPresenterListener {

    /**
     * On opening drawer
     */
    void onDrawerSlide();

    /**
     * On user account change
     */
    void onUserChange();

    /**
     * Set active user with url
     *
     * @param url - TC url
     */
    void setActiveUser(String url);

    /**
     * Is account active
     *
     * @param url - TC url
     */
    boolean isActiveProfile(String url);

    /**
     * {@inheritDoc}
     */
    void startRootProjectsActivity();

    /**
     * {@inheritDoc}
     */
    void startRootProjectsActivityWhenSwitchingAccounts();

    /**
     * {@inheritDoc}
     */
    void startAccountListActivity();

    /**
     * {@inheritDoc}
     */
    void startAgentActivity();

    /**
     * {@inheritDoc}
     */
    void startBuildRunningActivity();

    /**
     * {@inheritDoc}
     */
    void startQueuedBuildsActivity();

    /**
     * {@inheritDoc}
     */
    void startAboutActivity();
}
