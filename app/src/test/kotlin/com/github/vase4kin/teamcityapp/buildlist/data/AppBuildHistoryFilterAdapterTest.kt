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

package com.github.vase4kin.teamcityapp.buildlist.data

import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.build_history.api.BuildHistoryQuery
import teamcityapp.features.filter_builds.api.BuildStatusFilter

class AppBuildHistoryFilterAdapterTest {
    private val adapter = AppBuildHistoryFilterAdapter()

    @Test fun emptySelectionKeepsPersonalAndPinnedFalseUnlikeDefaultHistoryLocator() {
        val actual = adapter.locator(BuildListFilterImpl())
        assertEquals("state:any,canceled:any,failedToStart:any,branch:default:any,personal:false,pinned:false,count:10", actual)
        assertNotEquals(BuildHistoryQuery.DEFAULT_LOCATOR, actual)
    }

    @Test fun queuedSelectionSurvivesLegacySerializationAndKeepsNoCountAndPinnedAny() {
        val filter = BuildListFilterImpl().apply {
            setFilter(BuildStatusFilter.Queued.ordinal)
            setBranch("feature/raw")
            setPersonal(true)
            setPinned(true)
        }
        val bytes = ByteArrayOutputStream().also { ObjectOutputStream(it).use { stream -> stream.writeObject(filter) } }.toByteArray()
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Serializable }
        assertEquals("state:queued,branch:name:feature/raw,personal:true,pinned:any", adapter.locator(restored))
    }

    @Test fun runningSelectionKeepsItsLegacyUnpagedLocator() {
        val filter = BuildListFilterImpl().apply {
            setFilter(BuildStatusFilter.Running.ordinal)
            setPinned(true)
        }
        assertEquals("running:true,branch:default:any,personal:false,pinned:true", adapter.locator(filter))
    }
}
