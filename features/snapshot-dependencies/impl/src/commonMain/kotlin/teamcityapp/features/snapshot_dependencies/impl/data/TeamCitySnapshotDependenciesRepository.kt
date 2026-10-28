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

package teamcityapp.features.snapshot_dependencies.impl.data

import teamcityapp.features.snapshot_dependencies.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.network.*

class TeamCitySnapshotDependenciesRepository(private val account: AccountScope, private val api: TeamCityApi) : SnapshotDependenciesRepository {
    override suspend fun dependencies(buildId: String, forceRefresh: Boolean): List<BuildLaunchData> = account.run {
        val locator = "snapshotDependency:(to:(id:${buildId.entityId()}),includeInitial:true),defaultFilter:false"
        api.fullBuilds(api.apiPath("builds?locator=${locator.encoded()}&fields=${(BUILD_FIELDS + ",nextHref").encoded()}"), true)
    }
}
