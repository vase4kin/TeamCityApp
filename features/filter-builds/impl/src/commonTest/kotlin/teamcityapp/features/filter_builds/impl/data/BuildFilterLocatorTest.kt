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

package teamcityapp.features.filter_builds.impl.data

import kotlin.test.*
import teamcityapp.features.filter_builds.api.*

class BuildFilterLocatorTest {
    @Test fun legacyFilterLocatorsPreserveAllStatusBranchAndFlagRules() {
        val expected = listOf("status:SUCCESS", "status:FAILURE", "status:ERROR", "canceled:true", "failedToStart:true", "running:true", "state:queued", "state:any,canceled:any,failedToStart:any")
        BuildStatusFilter.entries.forEachIndexed { index, status ->
            val suffix = if (status in listOf(BuildStatusFilter.Running, BuildStatusFilter.Queued)) "" else ",count:10"
            assertEquals(expected[index] + ",branch:name:release,personal:true,pinned:" + (if (status == BuildStatusFilter.Queued) "any" else "true") + suffix, BuildFilter(status, "release", true, true).toTeamCityLocator())
        }
        assertEquals("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:false,count:10", BuildFilter().toTeamCityLocator())
    }
}
