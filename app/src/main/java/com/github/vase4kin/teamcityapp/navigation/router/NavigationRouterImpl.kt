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

package com.github.vase4kin.teamcityapp.navigation.router

import android.app.Activity

import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity

/**
 * Impl of [NavigationRouter]
 */
class NavigationRouterImpl(private val activity: Activity) : NavigationRouter {

    /**
     * {@inheritDoc}
     */
    override fun startBuildListActivity(name: String, id: String) {
        BuildListActivity.start(name, id, null, activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun startNavigationActivity(name: String, id: String) {
        NavigationActivity.start(name, id, activity)
    }

    override fun openRateTheApp() {
        ConvenienceBuilder.createRateOnClickAction(activity).onClick()
    }
}
