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

package com.github.vase4kin.teamcityapp.navigation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.navigation.presenter.NavigationPresenterImpl
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Activity to manage navigation between projects and build types
 */
class NavigationActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var drawerPresenter: DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker>
    @Inject
    lateinit var presenter: NavigationPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_list)
        drawerPresenter.onCreate()
        presenter.onViewsCreated()
    }

    override fun onDestroy() {
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

        fun start(name: String, id: String, activity: Activity) {
            val bundle = Bundle()
            bundle.putString(BundleExtractorValues.NAME, name)
            val intent = Intent(activity, NavigationActivity::class.java)
            bundle.putString(BundleExtractorValues.ID, id)
            intent.putExtras(bundle)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
        }
    }
}
