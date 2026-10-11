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

package teamcityapp.features.build_history.impl.navigation

import android.app.Activity
import android.content.Intent
import javax.inject.Inject
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_history.impl.R

class BuildHistoryNavigationImpl @Inject constructor() : BuildHistoryNavigation {
    override fun open(activity: Activity, configurationId: String, configurationName: String, locator: String?) {
        activity.startActivity(
            Intent().setClassName(activity, BuildHistoryNavigation.LEGACY_ACTIVITY).apply {
                putExtra(BuildHistoryNavigation.EXTRA_ID, configurationId)
                putExtra(BuildHistoryNavigation.EXTRA_NAME, configurationName)
                locator?.let { putExtra(BuildHistoryNavigation.EXTRA_LOCATOR, it) }
            }
        )
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
    }
}
