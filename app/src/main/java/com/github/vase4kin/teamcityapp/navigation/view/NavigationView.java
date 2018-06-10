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

package com.github.vase4kin.teamcityapp.navigation.view;

import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;

/**
 * View to manager Navigation screen
 */
public interface NavigationView extends BaseListView<NavigationDataModel> {

    /**
     * Rate the app view holder type
     */
    int TYPE_RATE_THE_APP = 2;

    /**
     * Set adapter click listener
     *
     * @param clickListener - Listener to receive click callbacks
     */
    void setNavigationAdapterClickListener(OnNavigationItemClickListener clickListener);

    /**
     * Set toolbar title
     *
     * @param title - Title to set
     */
    void setTitle(String title);

    /**
     * Hide rate the app item
     */
    void hideTheRateApp();
}
