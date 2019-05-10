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

package com.github.vase4kin.teamcityapp.agenttabs.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.agenttabs.tracker.AgentTabsViewTracker
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager
import com.github.vase4kin.teamcityapp.base.tabs.presenter.BaseTabsPresenterImpl
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Manages agents activity
 */
class AgentTabsActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var drawerPresenter: DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker>
    @Inject
    internal lateinit var tabsPresenter: BaseTabsPresenterImpl<BaseTabsViewModel, BaseTabsDataManager, AgentTabsViewTracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent_tabs)

        drawerPresenter.onCreate()
        tabsPresenter.onViewsCreated()
    }

    public override fun onResume() {
        super.onResume()
        tabsPresenter.onResume()
    }

    public override fun onPause() {
        super.onPause()
        tabsPresenter.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        tabsPresenter.onViewsDestroyed()
    }

    override fun onBackPressed() {
        drawerPresenter.onBackButtonPressed()
    }

    companion object {

        /**
         * Start [AgentTabsActivity]
         *
         * @param activity - Activity context
         */
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, AgentTabsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            DrawerActivityStartUtils.startActivity(launchIntent, activity)
        }
    }
}
