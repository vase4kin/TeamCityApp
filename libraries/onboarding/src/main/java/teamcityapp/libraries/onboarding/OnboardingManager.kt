/*
 * Copyright 2020 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.libraries.onboarding

/**
 * Onboarding manager
 */
interface OnboardingManager {

    /**
     * @return {true} if navigation drawer onboarding prompt is shwon
     */
    val isNavigationDrawerPromptShown: Boolean

    /**
     * @return {true} if run build onboarding prompt is shwon
     */
    val isRunBuildPromptShown: Boolean

    /**
     * @return {true} if filter builds onboarding prompt is shwon
     */
    val isFilterBuildsPromptShown: Boolean

    /**
     * @return {true} if stop build onboarding prompt is shwon
     */
    val isStopBuildPromptShown: Boolean

    /**
     * @return {true} if restart build onboarding prompt is shwon
     */
    val isRestartBuildPromptShown: Boolean

    /**
     * @return {true} if remove build from queue onboarding prompt is shwon
     */
    val isRemoveBuildFromQueuePromptShown: Boolean

    /**
     * @return {true} if add favorites onboarding prompt is shown
     */
    val isAddFavPromptShown: Boolean

    /**
     * @return {true} if tab filter builds onboarding prompt is shown
     */
    val isRunningBuildsFilterPromptShown: Boolean

    /**
     * @return {true} if filter builds queue onboarding prompt is shown
     */
    val isBuildsQueueFilterPromptShown: Boolean

    /**
     * @return {true} if filter agents onboarding prompt is shown
     */
    val isAgentsFilterPromptShown: Boolean

    /**
     * @return {true} if add favorites from buildtype onboarding prompt is shown
     */
    val isFavPromptShown: Boolean

    /**
     * Save that navigation drawer prompt is shown
     */
    fun saveNavigationDrawerPromptShown()

    /**
     * Save that run build prompt is shown
     */
    fun saveRunBuildPromptShown()

    /**
     * Save that filter builds prompt is shown
     */
    fun saveFilterBuildsPromptShown()

    /**
     * Save that stop build prompt is shown
     */
    fun saveStopBuildPromptShown()

    /**
     * Save that restart build prompt is shown
     */
    fun saveRestartBuildPromptShown()

    /**
     * Save that remove build from queue prompt is shown
     */
    fun saveRemoveBuildFromQueuePromptShown()

    /**
     * Save that add favorites prompt is shown
     */
    fun saveAddFavPromptShown()

    /**
     * Save that tab filter running builds prompt is shown
     */
    fun saveRunningBuildsFilterPromptShown()

    /**
     * Save that filter running builds prompt is shown
     */
    fun saveBuildsQueueFilterPromptShown()

    /**
     * Save that agents filter prompt is shown
     */
    fun saveAgentsFilterPromptShown()

    /**
     * Save that add favorites from buildtype prompt is shown
     */
    fun saveFavPromptShown()

    interface OnPromptShownListener {
        /**
         * when prompt is shown
         */
        fun onPromptShown()
    }
}
