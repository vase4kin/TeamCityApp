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

package com.github.vase4kin.teamcityapp.navigation

/** Installed Intent compatibility belongs to the app, not feature destination APIs. */
internal object LegacyProjectEntry {
    const val PROJECT_ID = "id"
    const val PROJECT_NAME = "name"
    const val ROOT_PROJECT_ID = "_Root"
    const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity"
}
internal object LegacyBuildHistoryEntry {
    const val EXTRA_ID = "id"
    const val EXTRA_NAME = "name"
    const val EXTRA_LOCATOR = "buildHistoryLocator"
    const val EXTRA_LEGACY_FILTER = "filter"
    const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity"
}
internal object LegacyArtifactsEntry {
    const val BUILD = "build"
    const val URL = "url"
    const val NAME = "name"
    const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.artifact.view.ArtifactListActivity"
}
