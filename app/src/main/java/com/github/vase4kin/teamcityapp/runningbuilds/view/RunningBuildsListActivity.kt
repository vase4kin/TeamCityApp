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

package com.github.vase4kin.teamcityapp.runningbuilds.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.runningbuilds.presenter.RunningBuildsListPresenterImpl
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Activity to manage running builds
 */
class RunningBuildsListActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var drawerPresenter: DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker>
    @Inject
    lateinit var presenter: RunningBuildsListPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build_list)
        drawerPresenter.onCreate()
        presenter.onViewsCreated()
    }

    public override fun onDestroy() {
        super.onDestroy()
        presenter.onViewsDestroyed()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onBackPressed() {
        drawerPresenter.onBackButtonPressed()
    }

    companion object {

        /**
         * Start [RunningBuildsListActivity]
         *
         * @param activity - Activity context
         */
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, RunningBuildsListActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            DrawerActivityStartUtils.startActivity(launchIntent, activity)
        }
    }
}
