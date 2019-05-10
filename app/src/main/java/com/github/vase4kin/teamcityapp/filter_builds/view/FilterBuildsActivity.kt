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

package com.github.vase4kin.teamcityapp.filter_builds.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.filter_builds.presenter.FilterBuildsPresenterImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Activity to manage builds filtering logic
 */
class FilterBuildsActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var presenter: FilterBuildsPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_builds)
        presenter.onCreate()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom)
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
        super.onBackPressed()
    }

    companion object {

        /**
         * Activity request code
         */
        const val REQUEST_CODE = 12921

        /**
         * Start filter builds activity
         *
         * @param activity - Activity instance
         */
        fun startForResult(activity: Activity, buildTypeId: String) {
            val intent = Intent(activity, FilterBuildsActivity::class.java)
            intent.putExtra(EXTRA_BUILD_TYPE_ID, buildTypeId)
            activity.startActivityForResult(intent, REQUEST_CODE)
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold)
        }
    }
}
