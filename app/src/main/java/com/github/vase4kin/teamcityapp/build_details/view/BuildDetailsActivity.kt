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

package com.github.vase4kin.teamcityapp.build_details.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenterImpl
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import teamcityapp.libraries.utils.initToolbar
import javax.inject.Inject

/**
 * Activity to manage build details info
 */
class BuildDetailsActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var presenter: BuildDetailsPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build)
        initToolbar()
        presenter.onViewsCreated()
    }

    override fun onDestroy() {
        presenter.onViewsDestroyed()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        presenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        presenter.onRestoreInstanceState(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        AndroidInjection.inject(this)
        presenter.onViewsCreated()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right)
    }

    companion object {

        /**
         * Open [this] activity
         *
         * @param activity - Activity
         * @param build - Build to be passed
         * @param buildTypeName - Build type name
         */
        fun start(activity: Activity, build: Build, buildTypeName: String?) {
            val intent = Intent(activity, BuildDetailsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val b = Bundle()
            b.putSerializable(BundleExtractorValues.BUILD, build)
            b.putString(BundleExtractorValues.NAME, buildTypeName)
            intent.putExtras(b)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
        }

        fun startNotAsNewTask(activity: Activity, build: Build, buildTypeName: String?) {
            val intent = Intent(activity, BuildDetailsActivity::class.java)
            val b = Bundle()
            b.putSerializable(BundleExtractorValues.BUILD, build)
            b.putString(BundleExtractorValues.NAME, buildTypeName)
            intent.putExtras(b)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
        }
    }
}
