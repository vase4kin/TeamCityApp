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

package com.github.vase4kin.teamcityapp.navigation.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;

import java.util.List;

/**
 * Impl of {@link NavigationDataManager}
 */
public class NavigationDataManagerImpl extends BaseListRxDataManagerImpl<NavigationNode, NavigationItem> implements NavigationDataManager {

    private static final String PREF_NAME = "rateTheAppPref";
    private static final String KEY_RATED = "rated";

    private final Repository mRepository;
    private final SharedPreferences sharedPreferences;

    public NavigationDataManagerImpl(Repository repository, Context context) {
        this.mRepository = repository;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String id, boolean update, @NonNull OnLoadingListener<List<NavigationItem>> loadingListener) {
        load(mRepository.listBuildTypes(id, update), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean showRateTheApp() {
        return !isRated() && true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRateLaterClickedOn() {
        saveRatedState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRateNowClickedOn() {
        saveRatedState();
    }

    private boolean isRated() {
        return sharedPreferences.getBoolean(KEY_RATED, false);
    }

    private void saveRatedState() {
        sharedPreferences.edit().putBoolean(KEY_RATED, true).apply();
    }
}
