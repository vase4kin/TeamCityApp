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

package com.github.vase4kin.teamcityapp.root.view;

import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;

/**
 * Root drawer view interactions
 */
public interface RootDrawerView extends DrawerView {

    /**
     * Select drawer selection
     *
     * @param selection - Drawer selection
     */
    void setDrawerSelection(int selection);

    /**
     * Show app rate dialog
     *
     * @param listener - Listener to receive dialog button callbacks
     */
    void showAppRateDialog(OnAppRateListener listener);

    /**
     * Show navigation drawer onboarding prompt
     *
     * @param listener - Listener to know when prompt is shown
     */
    void showNavigationDrawerPrompt(OnboardingManager.OnPromptShownListener listener);

    /**
     * On app rate dialog listener
     */
    interface OnAppRateListener {
        /**
         * On remind later button click
         */
        void onNeutralButtonClick();

        /**
         * On negative button click
         */
        void onNegativeButtonClick();

        /**
         * On rate app button click();
         */
        void onPositiveButtonClick();
    }
}
