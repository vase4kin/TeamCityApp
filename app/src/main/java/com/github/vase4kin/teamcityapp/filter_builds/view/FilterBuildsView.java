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
package com.github.vase4kin.teamcityapp.filter_builds.view;


import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener;

/**
 * {@link FilterBuildsActivity} view interactions
 */
public interface FilterBuildsView {

    /**
     * Init views
     *
     * @param listener - to handle view interactions
     */
    void initViews(ViewListener listener);

    /**
     * Unbind views
     */
    void unbindViews();

    /**
     * Listener to receive callbacks to presenter
     */
    interface ViewListener extends OnToolBarNavigationListener {
    }
}
