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

package com.github.vase4kin.teamcityapp.onboarding;

/**
 * Onboarding manager
 */
public interface OnboardingManager {

    /**
     * @return {true} if navigation drawer onboarding prompt is shwon
     */
    boolean isNavigationDrawerPromptShown();

    /**
     * Save that navigation drawer prompt is shown
     */
    void saveNavigationDrawerPromptShown();

    /**
     * @return {true} if run build onboarding prompt is shwon
     */
    boolean isRunBuildPromptShown();

    /**
     * Save that run build prompt is shown
     */
    void saveRunBuildPromptShown();

    /**
     * @return {true} if filter builds onboarding prompt is shwon
     */
    boolean isFilterBuildsPromptShown();

    /**
     * Save that filter builds prompt is shown
     */
    void saveFilterBuildsPromptShown();

    /**
     * @return {true} if stop build onboarding prompt is shwon
     */
    boolean isStopBuildPromptShown();

    /**
     * Save that stop build prompt is shown
     */
    void saveStopBuildPromptShown();

    /**
     * @return {true} if restart build onboarding prompt is shwon
     */
    boolean isRestartBuildPromptShown();

    /**
     * Save that restart build prompt is shown
     */
    void saveRestartBuildPromptShown();

    /**
     * @return {true} if remove build from queue onboarding prompt is shwon
     */
    boolean isRemoveBuildFromQueuePromptShown();

    /**
     * Save that remove build from queue prompt is shown
     */
    void saveRemoveBuildFromQueuePromptShown();

    /**
     * @return {true} if add favorites onboarding prompt is shown
     */
    boolean isAddFavPromptShown();

    /**
     * Save that add favorites prompt is shown
     */
    void saveAddFavPromptShown();

    interface OnPromptShownListener {
        /**
         * when prompt is shown
         */
        void onPromptShown();
    }
}
