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

package com.github.vase4kin.teamcityapp.buildlog.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

/**
 * Impl of {@link BuildLogInteractor}
 */
public class BuildLogInteractorImpl implements BuildLogInteractor {

    private final static String PREF_NAME = "BuildLogPrefs";
    private final static String KEY = "BuildLogDialogShown";
    private SharedPreferences mSharedPreferences;
    private UserAccount mUserAccount;

    public BuildLogInteractorImpl(Context context, UserAccount userAccount) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mUserAccount = userAccount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGuestUser() {
        return mUserAccount.isGuestUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthDialogShown() {
        return mSharedPreferences.getBoolean(KEY, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthDialogStatus(boolean isShown) {
        mSharedPreferences.edit().putBoolean(KEY, isShown).apply();
    }
}
