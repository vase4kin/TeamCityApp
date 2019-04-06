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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;

/**
 * Impl of {@link OnboardingManager}
 */
public class OnboardingManagerImpl implements OnboardingManager {

    @VisibleForTesting
    public final static String PREF_NAME = "OnboardingPref";
    private final static String KEY_NAV_DRAWER = "NavDrawer";
    private final static String KEY_RUN_BUILD = "RunBuild";
    private final static String KEY_FILTER_BUILDS = "FilterBuilds";
    private final static String KEY_STOP_BUILDS = "StopBuilds";
    private final static String KEY_RESTART_BUILDS = "RestartBuilds";
    private final static String KEY_REMOVE_BUILDS_FROM_QUEUE = "RemoveBuildsFromQueue";
    private final static String KEY_ADD_FAV = "AddToFavorites";
    private final static String KEY_FAV = "AddToFavoritesFromBuildType";

    private final SharedPreferences mSharedPreferences;

    public OnboardingManagerImpl(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNavigationDrawerPromptShown() {
        return mSharedPreferences.getBoolean(KEY_NAV_DRAWER, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveNavigationDrawerPromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_NAV_DRAWER, true).apply();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunBuildPromptShown() {
        return mSharedPreferences.getBoolean(KEY_RUN_BUILD, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRunBuildPromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_RUN_BUILD, true).apply();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFilterBuildsPromptShown() {
        return mSharedPreferences.getBoolean(KEY_FILTER_BUILDS, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveFilterBuildsPromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_FILTER_BUILDS, true).apply();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStopBuildPromptShown() {
        return mSharedPreferences.getBoolean(KEY_STOP_BUILDS, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveStopBuildPromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_STOP_BUILDS, true).apply();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRestartBuildPromptShown() {
        return mSharedPreferences.getBoolean(KEY_RESTART_BUILDS, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRestartBuildPromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_RESTART_BUILDS, true).apply();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRemoveBuildFromQueuePromptShown() {
        return mSharedPreferences.getBoolean(KEY_REMOVE_BUILDS_FROM_QUEUE, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRemoveBuildFromQueuePromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_REMOVE_BUILDS_FROM_QUEUE, true).apply();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAddFavPromptShown() {
        return mSharedPreferences.getBoolean(KEY_ADD_FAV, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAddFavPromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_ADD_FAV, true).apply();
    }

    @Override
    public boolean isFavPromptShown() {
        return mSharedPreferences.getBoolean(KEY_FAV, false);
    }

    @Override
    public void saveFavPromptShown() {
        mSharedPreferences.edit().putBoolean(KEY_FAV, true).apply();
    }
}
