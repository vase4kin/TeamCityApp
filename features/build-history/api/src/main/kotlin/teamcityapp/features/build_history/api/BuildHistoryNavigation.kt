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

package teamcityapp.features.build_history.api

import android.app.Activity
import java.io.Serializable
import teamcityapp.libraries.builds.BuildLaunchData

interface BuildHistoryNavigation {
    fun open(activity: Activity, configurationId: String, configurationName: String, locator: String? = null)
    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_NAME = "name"
        const val EXTRA_LEGACY_FILTER = "filter"
        const val EXTRA_LOCATOR = "buildHistoryLocator"
        const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity"
    }
}

/** The app reads its legacy Serializable BuildListFilter; features retain only its locator. */
interface BuildHistoryFilterAdapter {
    fun locator(filter: Serializable): String
}

/** Stateless bridge to legacy BuildDetails and Home; the feature owns its current Activity. */
interface BuildHistoryAppRouter {
    fun openBuild(activity: Activity, build: BuildLaunchData, configurationName: String?)
    fun openFavorites(activity: Activity)
}
