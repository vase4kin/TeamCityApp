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

package teamcityapp.features.drawer.utils

import android.app.Activity
import android.content.Intent
import android.os.Handler
import teamcityapp.features.drawer.R

/**
 * Helper for start activities from the drawer
 */
object DrawerActivityStartUtils {

    /**
     * Start activity from drawer
     *
     * @param launchIntent - Intent to start
     * @param activity - Activity context
     */
    fun startActivity(launchIntent: Intent?, activity: Activity, drawerTimeOut: Int) {
        if (launchIntent != null) {
            Handler().postDelayed({
                activity.startActivity(launchIntent)
                activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
            }, drawerTimeOut.toLong())
        }
    }
}
