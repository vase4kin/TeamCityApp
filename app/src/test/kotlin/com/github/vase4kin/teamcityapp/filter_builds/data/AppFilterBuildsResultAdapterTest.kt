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

package com.github.vase4kin.teamcityapp.filter_builds.data
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.filter_builds.api.*
class AppFilterBuildsResultAdapterTest {
    @Test fun everyStatusRetainsTheLegacyFilterPayload() {
        BuildStatusFilter.entries.forEach { status ->
            val selection = BuildFilter(status, "release", personal = true, pinned = true)
            val legacy = AppFilterBuildsResultAdapter().serialize(selection) as BuildListFilter
            val statusLocators = listOf("status:SUCCESS", "status:FAILURE", "status:ERROR", "canceled:true", "failedToStart:true", "running:true", "state:queued", "state:any,canceled:any,failedToStart:any")
            val pinned = if (status == BuildStatusFilter.Queued) "any" else "true"
            val count = if (status == BuildStatusFilter.Running || status == BuildStatusFilter.Queued) "" else ",count:10"
            val expected = "${statusLocators[status.ordinal]},branch:name:release,personal:true,pinned:$pinned$count"
            assertEquals(expected, legacy.toLocator())
            val bytes = java.io.ByteArrayOutputStream()
            java.io.ObjectOutputStream(bytes).use { it.writeObject(legacy) }
            val restored = java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() } as BuildListFilter
            assertEquals("com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl", restored.javaClass.name)
            assertEquals(expected, restored.toLocator())
        }
    }
}
