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

package com.github.vase4kin.teamcityapp.drawer.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerTimeOut;

/**
 * Helper for start activities from the drawer
 */
public class DrawerActivityStartUtils {

    /**
     * Start activity from drawer
     *
     * @param launchIntent - Intent to start
     * @param activity - Activity context
     */
    public static void startActivity(Intent launchIntent, final Activity activity) {
        if (launchIntent != null) {
            final Intent finalLaunchIntent = launchIntent;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.startActivity(finalLaunchIntent);
                    activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                }
            }, DrawerTimeOut.DELAY_ON_CLOSE);
        }
    }
}
