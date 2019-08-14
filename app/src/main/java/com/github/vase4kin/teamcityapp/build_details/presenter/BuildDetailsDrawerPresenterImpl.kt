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

package com.github.vase4kin.teamcityapp.build_details.presenter

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import javax.inject.Inject

/**
 * Drawer presenter impl special for [BuildDetailsActivity]
 */
class BuildDetailsDrawerPresenterImpl @Inject constructor(mViewModel: DrawerView,
                                                          dataManager: DrawerDataManager,
                                                          private val valueExtractor: BaseValueExtractor,
                                                          router: DrawerRouter,
                                                          tracker: DrawerTracker) : DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker>(mViewModel, dataManager, router, tracker) {

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        setBuildTabColor()
        super.onCreate()
    }

    /**
     * Setting default color for drawer
     */
    private fun setBuildTabColor() {
        if (!valueExtractor.isBundleNullOrEmpty) {
            val buildDetails = valueExtractor.buildDetails
            when {
                buildDetails.isRunning -> view.setDefaultColors(R.color.running_tool_bar_color)
                buildDetails.isQueued -> view.setDefaultColors(R.color.queued_tool_bar_color)
                buildDetails.isSuccess -> view.setDefaultColors(R.color.success_tool_bar_color)
                buildDetails.isFailed -> view.setDefaultColors(R.color.failed_tool_bar_color)
                else -> view.setDefaultColors(R.color.queued_tool_bar_color)
            }
        }
    }
}
