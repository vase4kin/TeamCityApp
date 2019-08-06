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

package com.github.vase4kin.teamcityapp.home.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils
import com.github.vase4kin.teamcityapp.home.presenter.HomePresenterImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class HomeActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var presenter: HomePresenterImpl

    @Inject
    lateinit var sharedUserStorage: SharedUserStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        presenter.onCreate()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val isRequiredToReload = intent.extras?.isRequiredToReload() == true
        if (isRequiredToReload) {
            reinitDeps()
        }
        presenter.onNewIntent(isRequiredToReload)
    }

    override fun onBackPressed() {
        presenter.onBackButtonPressed()
    }

    private fun reinitDeps() {
        (this.applicationContext as TeamCityApplication).buildRestApiInjectorWithBaseUrl(sharedUserStorage.activeUser.teamcityUrl)
        AndroidInjection.inject(this)
    }

    companion object {

        const val ARG_TAB = "arg_tab"

        fun startForTheFirstStart(activity: Activity) {
            val intent = Intent(activity, HomeActivity::class.java)
            intent.putExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, true)
            activity.startActivity(intent)
        }

        fun startWhenNewAccountIsCreated(activity: Activity) {
            startWhenSwitchingAccountsFromDrawer(activity)
        }

        fun startWhenNavigateToRootFromDrawer(activity: Activity) {
            val launchIntent = Intent(activity, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            DrawerActivityStartUtils.startActivity(launchIntent, activity)
        }

        fun startWithTabSelected(activity: Activity, navigationItem: AppNavigationItem) {
            val launchIntent = Intent(activity, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP).apply {
                        putExtra(ARG_TAB, navigationItem.ordinal)
                    }
            activity.startActivity(launchIntent)
        }

        fun startWhenSwitchingAccountsFromDrawer(activity: Activity) {
            val launchIntent = Intent(activity, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            launchIntent.putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
            DrawerActivityStartUtils.startActivity(launchIntent, activity)
        }

        fun start(activity: Activity) {
            val intent = Intent(activity, HomeActivity::class.java)
            activity.startActivity(intent)
        }
    }
}

private fun Bundle?.isRequiredToReload(): Boolean? {
    return this?.getBoolean(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, false)
}
