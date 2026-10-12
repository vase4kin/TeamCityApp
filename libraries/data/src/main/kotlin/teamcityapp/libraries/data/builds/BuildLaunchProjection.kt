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

package teamcityapp.libraries.data.builds

/**
 * Fields consumed by BuildLaunchMapper, requested on every row in the collection.
 * Parameters and restart use the incoming property values, so they must be included.
 * Other sections need links/counts, not their potentially large item collections.
 */
const val BUILD_LAUNCH_FIELDS = "count,build(" +
    "id,href,webUrl,number,state,status,statusText,branchName,buildTypeId," +
    "buildType(id,name,projectId,projectName,href)," +
    "queuedDate,startDate,finishDate,waitReason,startEstimate," +
    "triggered(type,date,details,user(username,name,id,href),buildType(id,name,projectId,projectName,href))," +
    "agent(id,name,href),canceledInfo(timestamp,user(username,name,id,href))," +
    "changes(href,count,nextHref),artifacts(href)," +
    "testOccurrences(href,passed,failed,ignored,count,nextHref)," +
    "properties(id,href,property(name,value,own,id,href))," +
    "snapshot-dependencies(href,count,nextHref),personal,pinned,cleanSources,queueAtTop)"
