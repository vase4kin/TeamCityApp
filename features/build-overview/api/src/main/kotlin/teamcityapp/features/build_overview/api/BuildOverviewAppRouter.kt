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

package teamcityapp.features.build_overview.api

import android.app.Activity
import teamcityapp.libraries.builds.BuildLaunchData

/** UI-scoped app adapter keeps the native BuildDetails host and its confirmation flows. */
interface BuildOverviewAppRouter {
    /** Publish latest data before dispatch: host actions must not use the incoming snapshot. */
    fun buildLoaded(activity: Activity, build: BuildLaunchData)
    fun dispatch(activity: Activity, action: BuildOverviewAction, build: BuildLaunchData, branch: String? = null)

    /** Existing shared onboarding preferences/prompt integration stays behind this host adapter. */
    fun resumed(activity: Activity, build: BuildLaunchData)
    fun sheetVisibility(activity: Activity, visible: Boolean)
    fun dispose(activity: Activity)
}
