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

package com.github.vase4kin.teamcityapp.about

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.utils.initToolbar
import com.mikepenz.aboutlibraries.LibsBuilder

/**
 * About libraries screen activity
 */
class AboutLibrariesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initToolbar()

        // About library fragment
        val aboutLibrary = LibsBuilder()
            .supportFragment()

        // Commit fragment to container
        supportFragmentManager
            .beginTransaction()
            .add(R.id.about_library_container, aboutLibrary)
            .commit()
    }

    companion object {

        /**
         * Start About activity with Flag [Intent.FLAG_ACTIVITY_SINGLE_TOP]
         */
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, AboutLibrariesActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(launchIntent)
        }
    }
}
