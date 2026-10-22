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

package teamcityapp.libraries.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlin.test.*
import kotlinx.serialization.Serializable

@Serializable
internal data object Root : NavKey

@Serializable
internal data class Detail(val id: String) : NavKey

@Serializable
internal data class Selected(val value: String) : NavigationResult

@Serializable
internal data class Queued(val href: String) : NavigationResult

class NavigatorTest {
    private var sequence = 0
    private fun navigator(): Navigator = Navigator(mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("root", Root))))) { "visit-${++sequence}" }

    @Test fun restorableArgumentUpdateRetainsTheLiveOwnerAndPendingResults() {
        val nav = navigator()
        val origin = nav.push(Detail("history"))
        nav.push(Detail("filter"))
        nav.finish(Selected("branch"), origin)
        assertTrue(nav.updateDestination(origin, Detail("filtered history")))
        assertEquals(origin, nav.current.id)
        assertEquals(Detail("filtered history"), nav.current.destination)
        assertEquals(Selected("branch"), nav.consumeResult(origin, Selected::class))
        assertNull(nav.consumeResult(origin, Selected::class))
        assertFalse(nav.updateDestination("removed", Detail("obsolete")))
        assertFailsWith<IllegalArgumentException> { nav.updateDestination(origin, Root) }
    }

    @Test fun rootBackDelegatesToHostWithoutDestroyingRoot() {
        val nav = navigator()
        assertFalse(nav.pop())
        assertEquals("root", nav.current.id)
    }

    @Test fun repeatedDestinationsHaveIndependentOwnersAndBackRestoresPreviousVisit() {
        val nav = navigator()
        val first = nav.push(Detail("build"))
        val second = nav.push(Detail("build"))
        assertNotEquals(first, second)
        assertEquals(3, nav.snapshot.entries.size)
        assertTrue(nav.pop())
        assertEquals(first, nav.current.id)
        assertEquals(Detail("build"), nav.current.destination)
    }

    @Test fun resultsGoToOriginatingVisitAndAreConsumedOnce() {
        val nav = navigator()
        val history = nav.push(Detail("history"))
        nav.push(Detail("filter"))
        assertTrue(nav.finish(Selected("branch")))
        assertEquals(history, nav.current.id)
        assertNull(nav.consumeResult("root", Selected::class))
        assertEquals(Selected("branch"), nav.consumeResult(history, Selected::class))
        assertNull(nav.consumeResult(history, Selected::class))
    }

    @Test fun resultConsumptionPreservesOtherTypesAndDeliveryOrder() {
        val nav = navigator()
        nav.push(Detail("filter"))
        nav.finish(Selected("first"))
        nav.push(Detail("queue"))
        nav.finish(Queued("build/123"))
        nav.push(Detail("filter"))
        nav.finish(Selected("second"))
        assertEquals(Queued("build/123"), nav.consumeResult("root", Queued::class))
        assertEquals(Selected("first"), nav.consumeResult("root", Selected::class))
        assertEquals(Selected("second"), nav.consumeResult("root", Selected::class))
    }

    @Test fun finishingAtRootOrForRemovedRecipientDoesNotChangeNavigation() {
        val nav = navigator()
        assertFalse(nav.finish(Selected("unused")))
        nav.push(Detail("child"))
        val before = nav.snapshot
        assertFalse(nav.finish(Selected("unused"), "missing"))
        assertEquals(before, nav.snapshot)
    }

    @Test fun cancellingChildReturnsWithoutResult() {
        val nav = navigator()
        nav.push(Detail("filter"))
        nav.pop()
        assertTrue(nav.snapshot.results.isEmpty())
    }

    @Test fun removingRecipientDiscardsItsUnconsumedResults() {
        val nav = navigator()
        val recipient = nav.push(Detail("history"))
        nav.push(Detail("filter"))
        nav.finish(Selected("branch"))
        nav.pop()
        assertNull(nav.consumeResult(recipient, Selected::class))
    }

    @Test fun replacingDestinationChangesOwnerAndDiscardsItsResults() {
        val nav = navigator()
        val original = nav.push(Detail("old-build"))
        nav.push(Detail("filter"))
        nav.finish(Selected("old"))
        nav.replace(Detail("new-build"))
        assertNotEquals(original, nav.current.id)
        assertEquals(Detail("new-build"), nav.current.destination)
        assertEquals(2, nav.snapshot.entries.size)
        assertTrue(nav.snapshot.results.isEmpty())
    }

    @Test fun accountResetReplacesEveryOwnerEvenWithTheSameDestination() {
        val nav = navigator()
        nav.push(Detail("filter"))
        nav.finish(Selected("previous-account"))
        nav.reset(Root, sessionKey = "next-account")
        assertNotEquals("root", nav.current.id)
        assertEquals("next-account", nav.snapshot.sessionKey)
        assertEquals(listOf(Root), nav.snapshot.entries.map { it.destination })
        assertTrue(nav.snapshot.results.isEmpty())
    }

    @Test
    fun emptyStackIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            NavigationSnapshot(emptyList())
        }
    }

    @Test
    fun duplicateOwnersAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            NavigationSnapshot(listOf(NavigationEntry("same", Root), NavigationEntry("same", Detail("build"))))
        }
    }

    @Test
    fun orphanResultIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            NavigationSnapshot(listOf(NavigationEntry("root", Root)), listOf(PendingNavigationResult("missing", Selected("branch"))))
        }
    }

    @Test fun returningToALiveVisitPreservesItsIdentityAndDropsRemovedOwnersResults() {
        val nav = navigator()
        val history = nav.push(Detail("history"))
        nav.push(Detail("filter"))
        nav.finish(Selected("old-history-result"), history)
        nav.push(Detail("nested"))
        assertTrue(nav.popTo("root", Selected("favorites-tab")))
        assertEquals("root", nav.current.id)
        assertEquals(1, nav.snapshot.entries.size)
        assertNull(nav.consumeResult(history, Selected::class))
        assertEquals(Selected("favorites-tab"), nav.consumeResult("root", Selected::class))
        assertNull(nav.consumeResult("root", Selected::class))
    }

    @Test fun returningToAnExpiredVisitDoesNotModifyTheStackOrPendingResults() {
        val nav = navigator()
        nav.push(Detail("history"))
        val original = nav.snapshot
        assertFalse(nav.popTo("removed", Selected("favorites")))
        assertEquals(original, nav.snapshot)
    }

    @Test fun scopedConsumersLeaveOtherChildResultsInPlaceAndPreserveMatchingOrder() {
        val nav = navigator()
        nav.push(Detail("first-sheet"))
        nav.finish(Selected("first-child"))
        nav.push(Detail("second-sheet"))
        nav.finish(Selected("second-child"))
        assertNull(nav.consumeResult("root", Selected::class) { it.value == "missing-child" })
        assertEquals(2, nav.snapshot.results.size)
        assertEquals(Selected("second-child"), nav.consumeResult("root", Selected::class) { it.value == "second-child" })
        assertEquals(Selected("first-child"), nav.consumeResult("root", Selected::class) { it.value == "first-child" })
        assertTrue(nav.snapshot.results.isEmpty())
    }

    @Test fun childResultsStayWithTheirOriginAndParentSelectionPreservesChildHistory() {
        val parent = navigator()
        val child = Navigator(mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("tab-root", Detail("projects"))))), parent = parent)
        val history = child.push(Detail("history"))
        child.push(Detail("filter"))
        assertTrue(child.finish(Selected("branch"), history))
        assertNull(parent.consumeResult(history, Selected::class))
        assertEquals(Selected("branch"), child.consumeResult(history, Selected::class))
        assertNull(child.consumeResult(history, Selected::class))
        val home = child.findEntry { it.destination == Root }!!
        assertTrue(child.popTo(home.id, Selected("favorites")))
        assertEquals(history, child.current.id)
        assertEquals(Selected("favorites"), parent.consumeResult(home.id, Selected::class))
        child.push(Detail("filter"))
        child.popTo("tab-root")
        child.push(Detail("later-filter"))
        assertFalse(child.finish(Selected("stale"), history))
    }

    @Test fun returningToRootRetainsItsIdentityAndClearsResultsOfRemovedVisits() {
        val nav = navigator()
        val history = nav.push(Detail("history"))
        nav.push(Detail("filter"))
        nav.finish(Selected("old-filter"), history)
        nav.push(Detail("build"))
        assertTrue(nav.popToRoot())
        assertEquals("root", nav.current.id)
        assertEquals(1, nav.snapshot.entries.size)
        assertTrue(nav.snapshot.results.isEmpty())
        assertTrue(nav.popToRoot())
        assertEquals("root", nav.current.id)
    }
}
