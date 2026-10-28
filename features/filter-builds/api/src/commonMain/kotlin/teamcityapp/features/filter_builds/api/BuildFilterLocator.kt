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

package teamcityapp.features.filter_builds.api

/** Byte-compatible with Android's installed BuildListFilterImpl, including queued pinned:any. */
fun BuildFilter.toTeamCityLocator(): String = buildString {
    append(
        when (status) {
            BuildStatusFilter.Success -> "status:SUCCESS"
            BuildStatusFilter.Failed -> "status:FAILURE"
            BuildStatusFilter.Error -> "status:ERROR"
            BuildStatusFilter.Cancelled -> "canceled:true"
            BuildStatusFilter.FailedToStart -> "failedToStart:true"
            BuildStatusFilter.Running -> "running:true"
            BuildStatusFilter.Queued -> "state:queued"
            BuildStatusFilter.None -> "state:any,canceled:any,failedToStart:any"
        }
    )
    append(if (branch.isEmpty()) ",branch:default:any" else ",branch:name:$branch")
    append(",personal:$personal,pinned:")
    append(if (status == BuildStatusFilter.Queued) "any" else pinned.toString())
    if (status != BuildStatusFilter.Queued && status != BuildStatusFilter.Running) append(",count:10")
}
