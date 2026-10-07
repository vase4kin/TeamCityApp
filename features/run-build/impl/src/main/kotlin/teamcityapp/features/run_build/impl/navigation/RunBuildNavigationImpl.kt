/*
 * Copyright 2016 Andrey Tolpeev
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

package teamcityapp.features.run_build.impl.navigation
import android.app.Activity
import android.content.Intent
import teamcityapp.features.run_build.api.BUILD_TYPE_ID
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation
import teamcityapp.features.run_build.impl.RunBuildActivity
import teamcityapp.libraries.resources.R as SharedR
internal class RunBuildNavigationImpl : RunBuildNavigation {
    override fun openForResult(activity: Activity, buildTypeId: String) {
        activity.startActivityForResult(Intent(activity, RunBuildActivity::class.java).putExtra(BUILD_TYPE_ID, buildTypeId), RunBuildNavigation.REQUEST_CODE)
        activity.overridePendingTransition(SharedR.anim.slide_in_bottom, SharedR.anim.hold)
    }
}
