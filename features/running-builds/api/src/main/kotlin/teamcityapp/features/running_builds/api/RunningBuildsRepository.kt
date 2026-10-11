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

package teamcityapp.features.running_builds.api

import kotlinx.coroutines.flow.Flow
import teamcityapp.libraries.builds.BuildLaunchData

interface RunningBuildsRepository {
    /** Emits the current account and Home All/Favorites selection, including favorite changes. */
    val query: Flow<RunningBuildsQuery>

    /**
     * Hydrates complete launch snapshots for the query's account/session. Reject stale account
     * snapshots before requesting. Running summaries force fresh details; other cached details
     * refresh when the summary/cached finished flag differs. forceRefresh evicts the list cache.
     * Uses the running builds locator on app/rest/builds.
     * Home continues to own the live count flow independently of this list.
     */
    suspend fun builds(query: RunningBuildsQuery, forceRefresh: Boolean): List<BuildLaunchData>
}
