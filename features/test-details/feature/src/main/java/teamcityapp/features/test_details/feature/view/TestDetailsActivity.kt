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

package teamcityapp.features.test_details.feature.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import teamcityapp.features.test_details.feature.R
import teamcityapp.features.test_details.feature.presenter.TestDetailsPresenterImpl
import javax.inject.Inject

const val ARG_TEST_URL = "testUrl"

/**
 * Activity to manage test details
 */
class TestDetailsActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var presenter: TestDetailsPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_test_details)
        presenter.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
    }

    companion object {

        /**
         * Open failed test activity
         *
         * @param url - Url to failed test
         * @param activity - Activity context
         */
        fun openFailedTest(url: String, activity: Activity) {
            val intent = Intent(activity, TestDetailsActivity::class.java)
            intent.putExtra(ARG_TEST_URL, url)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold)
        }
    }
}
