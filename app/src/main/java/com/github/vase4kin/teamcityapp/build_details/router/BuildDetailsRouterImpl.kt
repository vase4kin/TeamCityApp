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

package com.github.vase4kin.teamcityapp.build_details.router

import android.app.Activity
import android.content.Intent

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity

/**
 * Impl of [BuildDetailsRouter]
 */
class BuildDetailsRouterImpl(private val activity: Activity) : BuildDetailsRouter {

    /**
     * {@inheritDoc}
     */
    override fun reopenBuildTabsActivity(build: Build, buildTypeName: String) {
        BuildDetailsActivity.start(activity, build, buildTypeName)
    }

    /**
     * {@inheritDoc}
     */
    override fun startShareBuildWebUrlActivity(webUrl: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, webUrl)
        sendIntent.type = "text/plain"
        activity.startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.text_share_build)))
    }

    /**
     * {@inheritDoc}
     */
    override fun startBuildListActivity(name: String, id: String, filter: BuildListFilter?) {
        BuildListActivity.start(name, id, filter, activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun startProjectActivity(name: String, id: String) {
        NavigationActivity.start(name, id, activity)
    }
}
