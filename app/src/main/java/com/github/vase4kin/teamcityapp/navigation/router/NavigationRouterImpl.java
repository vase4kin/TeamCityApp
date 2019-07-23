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

package com.github.vase4kin.teamcityapp.navigation.router;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity;
import com.github.vase4kin.teamcityapp.home.view.HomeActivity;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity;

/**
 * Impl of {@link NavigationRouter}
 */
public class NavigationRouterImpl implements NavigationRouter {

    private Activity mActivity;

    public NavigationRouterImpl(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startBuildListActivity(@NonNull String name, @NonNull String id) {
        BuildListActivity.Companion.start(name, id, null, mActivity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startNavigationActivity(@NonNull String name, @NonNull String id) {
        NavigationActivity.Companion.start(name, id, mActivity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startProjectActivity() {
        HomeActivity.Companion.startWhenNavigateToRootFromDrawer(mActivity);
    }

    @Override
    public void openRateTheApp() {
        ConvenienceBuilder.createRateOnClickAction(mActivity).onClick();
    }
}
