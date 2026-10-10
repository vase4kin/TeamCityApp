/*
 * Copyright 2020 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
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
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsArguments
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenterImpl
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider
import teamcityapp.libraries.utils.initToolbar

/**
 * Activity to manage build details info
 */
@AndroidEntryPoint
class BuildDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var presenterProvider: Provider<BuildDetailsPresenterImpl>

    @Inject
    lateinit var arguments: BuildDetailsArguments

    private var presenterResumed = false

    lateinit var presenter: BuildDetailsPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.restoreCurrent(savedInstanceState)
        presenter = presenterProvider.get()
        setContentView(R.layout.activity_build)
        initToolbar()
        presenter.onViewsCreated()
    }

    override fun onDestroy() {
        disposePresenter()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
        presenterResumed = true
    }

    override fun onPause() {
        super.onPause()
        if (presenterResumed) {
            presenter.onPause()
            presenterResumed = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        arguments.saveCurrent(outState)
        presenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        presenter.onRestoreInstanceState(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val resumePresenter = presenterResumed
        disposePresenter()
        // A new build needs fresh tab arguments; recreation keeps the restored tabs instead.
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach { remove(it) }
        }.commitNow()
        setIntent(intent)
        arguments.resetIncoming(intent.extras)
        presenter = presenterProvider.get()
        presenter.onViewsCreated()
        if (resumePresenter) {
            presenter.onResume()
            presenterResumed = true
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right)
    }

    /**
     * Workaround appcompat-1.1.0 bug https://issuetracker.google.com/issues/141132133
     * TODO: Remove when bug is fixed
     */
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (android.os.Build.VERSION.SDK_INT in android.os.Build.VERSION_CODES.LOLLIPOP..android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            return
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    private fun disposePresenter() {
        if (presenterResumed) {
            presenter.onPause()
            presenterResumed = false
        }
        presenter.onViewsDestroyed()
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
