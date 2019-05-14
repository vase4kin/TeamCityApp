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

package com.github.vase4kin.teamcityapp.root.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManagerImpl
import com.github.vase4kin.teamcityapp.root.presenter.RootDrawerPresenterImpl
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class RootProjectsActivity : DaggerAppCompatActivity(), OnAccountSwitchListener {

    @Inject
    lateinit var presenter: RootDrawerPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root_projects_list)
        presenter.onCreate()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        presenter.updateRootBundleValueManager(RootBundleValueManagerImpl(intent.extras
                ?: Bundle.EMPTY))
        presenter.onAccountSwitch()
        presenter.onNewIntent()
    }

    override fun onBackPressed() {
        presenter.onBackButtonPressed()
    }

    override fun onAccountSwitch() {
        AndroidInjection.inject(this)
    }

    companion object {

        fun startForTheFirstStart(activity: Activity) {
            val intent = Intent(activity, RootProjectsActivity::class.java)
            intent.putExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, true)
            activity.startActivity(intent)
        }

        fun startWhenNewAccountIsCreated(activity: Activity) {
            val launchIntent = Intent(activity, RootProjectsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            launchIntent.putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
            launchIntent.putExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, true)
            activity.startActivity(launchIntent)
        }

        fun startWhenNavigateToRootFromDrawer(activity: Activity) {
            val launchIntent = Intent(activity, RootProjectsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            DrawerActivityStartUtils.startActivity(launchIntent, activity)
        }

        fun startWhenSwitchingAccountsFromDrawer(activity: Activity) {
            val launchIntent = Intent(activity, RootProjectsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            launchIntent.putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
            DrawerActivityStartUtils.startActivity(launchIntent, activity)
        }

        fun start(activity: Activity) {
            val intent = Intent(activity, RootProjectsActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
