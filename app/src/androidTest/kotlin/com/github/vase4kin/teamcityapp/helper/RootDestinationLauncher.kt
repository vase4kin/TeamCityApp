/*
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

package com.github.vase4kin.teamcityapp.helper

import android.app.Activity
import android.content.Intent
import androidx.navigation3.runtime.NavKey
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import javax.inject.Inject
import teamcityapp.libraries.navigation.DestinationRegistry

/** Integration fixtures exercise real incoming Intents without shipping activity-entry wrappers. */
class RootDestinationLauncher @Inject constructor(private val registry: DestinationRegistry) {
    fun open(activity: Activity, destination: NavKey, flags: Int = 0) {
        activity.startActivity(
            Intent(activity, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(destination))
                .addFlags(flags or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }
}
