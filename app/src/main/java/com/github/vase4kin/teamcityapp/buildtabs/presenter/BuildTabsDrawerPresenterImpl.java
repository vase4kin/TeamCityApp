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

package com.github.vase4kin.teamcityapp.buildtabs.presenter;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;

import javax.inject.Inject;

/**
 * Drawer presenter impl special for {@link com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsActivity}
 */
public class BuildTabsDrawerPresenterImpl extends DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter> {

    private BaseValueExtractor mValueExtractor;

    @Inject
    BuildTabsDrawerPresenterImpl(DrawerView mViewModel,
                                 DrawerDataManager dataManager,
                                 BaseValueExtractor valueExtractor,
                                 DrawerRouter router) {
        super(mViewModel, dataManager, router);
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
        if (!mValueExtractor.isBundleNull()) {
            Build build = mValueExtractor.getBuild();
            if (build == null) return;
            if (build.isRunning()) {
                mView.setDefaultColors(R.color.running_tool_bar_color);
            } else if (build.isQueued()) {
                mView.setDefaultColors(R.color.queued_tool_bar_color);
            } else if (build.isSuccess()) {
                mView.setDefaultColors(R.color.success_tool_bar_color);
            } else if (build.isFailed()) {
                mView.setDefaultColors(R.color.failed_tool_bar_color);
            } else {
                mView.setDefaultColors(R.color.queued_tool_bar_color);
            }
        }
    }
}
