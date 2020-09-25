/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.about

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import teamcityapp.libraries.utils.initToolbar

/**
 * About activity
 */
class AboutActivity :
    DaggerAppCompatActivity(),
    AboutActivityLoadingListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initToolbar()

        // Commit fragment to container
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.about_library_container, AboutFragment())
            .commit()
    }

    override fun showLoader() {
        findViewById<View>(R.id.progress_wheel).visibility = View.VISIBLE
    }

    override fun hideLoader() {
        findViewById<View>(R.id.progress_wheel).visibility = View.GONE
    }

    companion object {

        /**
         * Start About activity with Flag [Intent.FLAG_ACTIVITY_SINGLE_TOP]
         */
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, AboutActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(launchIntent)
        }
    }
}

interface AboutActivityLoadingListener {
    fun showLoader()
    fun hideLoader()
}
