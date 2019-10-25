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

package com.github.vase4kin.teamcityapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import javax.inject.Inject

/**
 * Status bar utils class
 */
class StatusBarUtils @Inject constructor() {

    /**
     * Changing the status bar color
     *
     * @param activity - Activity
     * @param color - Color to set
     */
    @SuppressLint("InlinedApi")
    fun changeStatusBarColor(activity: Activity, @ColorRes color: Int) {
        val window = activity.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        // finally change the color
        window.statusBarColor = ContextCompat.getColor(activity, color)
    }
}
