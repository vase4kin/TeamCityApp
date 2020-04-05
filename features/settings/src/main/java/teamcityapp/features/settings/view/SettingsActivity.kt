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

package teamcityapp.features.settings.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import teamcityapp.features.settings.R
import teamcityapp.features.settings.tracker.SettingsTracker
import teamcityapp.libraries.utils.initToolbar
import javax.inject.Inject

class SettingsActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var tracker: SettingsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        initToolbar()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
    }

    override fun onResume() {
        super.onResume()
        tracker.trackView()
    }

    companion object {
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, SettingsActivity::class.java)
            activity.startActivity(launchIntent)
        }
    }
}
