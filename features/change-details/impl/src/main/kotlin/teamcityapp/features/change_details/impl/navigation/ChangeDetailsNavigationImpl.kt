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

package teamcityapp.features.change_details.impl.navigation

import android.app.Activity
import android.content.Intent
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangeDetailsNavigation
import teamcityapp.features.change_details.impl.ChangeDetailsActivity
import teamcityapp.features.change_details.impl.ChangeDetailsArguments
import javax.inject.Inject

class ChangeDetailsNavigationImpl @Inject constructor() : ChangeDetailsNavigation {
    override fun open(activity: Activity, details: ChangeDetails) {
        activity.startActivity(Intent(activity, ChangeDetailsActivity::class.java).putExtras(ChangeDetailsArguments.bundle(details)))
        activity.overridePendingTransition(teamcityapp.libraries.utils.R.anim.slide_in_bottom, teamcityapp.libraries.utils.R.anim.hold)
    }
}
