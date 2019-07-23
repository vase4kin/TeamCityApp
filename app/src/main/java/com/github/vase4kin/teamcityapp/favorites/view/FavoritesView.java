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

package com.github.vase4kin.teamcityapp.favorites.view;

import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;

/**
 * View to manager Navigation screen
 */
public interface FavoritesView extends BaseListView<NavigationDataModel> {

    /**
     * Set adapter click listener
     *
     * @param listener - Listener to receive click callbacks
     */
    void setViewListener(ViewListener listener);

    /**
     * Show add favorites info snack bar
     */
    void showInfoSnackbar();

    /**
     * Show add fav onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    void showAddFavPrompt(OnboardingManager.OnPromptShownListener listener);

    interface ViewListener {

        /**
         * On favorite item click event
         *
         * @param navigationItem - Navigation being clicked
         */
        void onClick(NavigationItem navigationItem);

        /**
         * On FAB click
         */
        void onFabClick();

        /**
         * On snack bar action click
         */
        void onSnackBarAction();
    }
}
