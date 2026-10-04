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

package teamcityapp.libraries.app_rating

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri

import javax.inject.Inject

/** Opens the app's store listing, using the browser when no market app is installed. */
class AppRatingImpl @Inject constructor() : AppRating {
    override fun open(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")))
        }
    }
}
