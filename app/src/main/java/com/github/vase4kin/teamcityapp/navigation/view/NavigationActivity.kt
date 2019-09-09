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

package com.github.vase4kin.teamcityapp.navigation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.navigation.presenter.NavigationPresenterImpl
import com.github.vase4kin.teamcityapp.utils.initToolbar
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Activity to manage navigation between projects and build types
 */
class NavigationActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var presenter: NavigationPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_list)
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

    companion object {

        fun start(name: String, id: String, activity: Activity) {
            val bundle = Bundle()
            bundle.putString(BundleExtractorValues.NAME, name)
            val intent = Intent(activity, NavigationActivity::class.java)
            bundle.putString(BundleExtractorValues.ID, id)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }
}
