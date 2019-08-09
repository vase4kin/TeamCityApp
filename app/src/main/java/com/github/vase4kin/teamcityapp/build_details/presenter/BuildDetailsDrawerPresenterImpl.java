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

package com.github.vase4kin.teamcityapp.build_details.presenter;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

import javax.inject.Inject;

/**
 * Drawer presenter impl special for {@link BuildDetailsActivity}
 */
public class BuildDetailsDrawerPresenterImpl extends DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker> {

    private BaseValueExtractor mValueExtractor;

    @Inject
    BuildDetailsDrawerPresenterImpl(DrawerView mViewModel,
                                    DrawerDataManager dataManager,
                                    BaseValueExtractor valueExtractor,
                                    DrawerRouter router,
                                    DrawerTracker tracker) {
        super(mViewModel, dataManager, router, tracker);
        this.mValueExtractor = valueExtractor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        setBuildTabColor();
        super.onCreate();
    }

    /**
     * Setting default color for drawer
     */
    private void setBuildTabColor() {
        if (!mValueExtractor.isBundleNullOrEmpty()) {
            BuildDetails buildDetails = mValueExtractor.getBuildDetails();
            if (buildDetails.isRunning()) {
                view.setDefaultColors(R.color.running_tool_bar_color);
            } else if (buildDetails.isQueued()) {
                view.setDefaultColors(R.color.queued_tool_bar_color);
            } else if (buildDetails.isSuccess()) {
                view.setDefaultColors(R.color.success_tool_bar_color);
            } else if (buildDetails.isFailed()) {
                view.setDefaultColors(R.color.failed_tool_bar_color);
            } else {
                view.setDefaultColors(R.color.queued_tool_bar_color);
            }
        }
    }
}
