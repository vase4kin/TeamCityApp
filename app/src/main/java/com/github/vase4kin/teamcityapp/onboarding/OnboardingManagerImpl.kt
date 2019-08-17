/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.onboarding

import android.content.Context
import android.content.SharedPreferences

import androidx.annotation.VisibleForTesting

/**
 * Impl of [OnboardingManager]
 */
class OnboardingManagerImpl(context: Context) : OnboardingManager {

    private val sharedPreferences: SharedPreferences

    init {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * {@inheritDoc}
     */
    override val isNavigationDrawerPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_NAV_DRAWER, false)

    /**
     * {@inheritDoc}
     */
    override val isRunBuildPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_RUN_BUILD, false)

    /**
     * {@inheritDoc}
     */
    override val isFilterBuildsPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_FILTER_BUILDS, false)

    /**
     * {@inheritDoc}
     */
    override val isStopBuildPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_STOP_BUILDS, false)

    /**
     * {@inheritDoc}
     */
    override val isRestartBuildPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_RESTART_BUILDS, false)

    /**
     * {@inheritDoc}
     */
    override val isRemoveBuildFromQueuePromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_REMOVE_BUILDS_FROM_QUEUE, false)

    /**
     * {@inheritDoc}
     */
    override val isAddFavPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_ADD_FAV, false)

    /**
     * {@inheritDoc}
     */
    override val isTabsFilterPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_TABS_FILTER, false)

    /**
     * {@inheritDoc}
     */
    override val isFavPromptShown: Boolean
        get() = sharedPreferences.getBoolean(KEY_FAV, false)

    /**
     * {@inheritDoc}
     */
    override fun saveNavigationDrawerPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_NAV_DRAWER, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveRunBuildPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_RUN_BUILD, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveFilterBuildsPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_FILTER_BUILDS, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveStopBuildPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_STOP_BUILDS, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveRestartBuildPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_RESTART_BUILDS, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveRemoveBuildFromQueuePromptShown() {
        sharedPreferences.edit().putBoolean(KEY_REMOVE_BUILDS_FROM_QUEUE, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveAddFavPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_ADD_FAV, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveTabsFilterPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_TABS_FILTER, true).apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveFavPromptShown() {
        sharedPreferences.edit().putBoolean(KEY_FAV, true).apply()
    }

    companion object {

        @VisibleForTesting
        const val PREF_NAME = "OnboardingPref"
        private const val KEY_NAV_DRAWER = "NavDrawer"
        private const val KEY_RUN_BUILD = "RunBuild"
        private const val KEY_FILTER_BUILDS = "FilterBuilds"
        private const val KEY_STOP_BUILDS = "StopBuilds"
        private const val KEY_RESTART_BUILDS = "RestartBuilds"
        private const val KEY_REMOVE_BUILDS_FROM_QUEUE = "RemoveBuildsFromQueue"
        private const val KEY_ADD_FAV = "AddToFavorites"
        private const val KEY_FAV = "AddToFavoritesFromBuildType"
        private const val KEY_TABS_FILTER = "TabsFilterBuilds"
    }
}
