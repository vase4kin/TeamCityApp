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

package teamcityapp.libraries.network

import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*

class TeamCityResponseCacheTest {
    private class Store : ResponseCacheStore {
        var value: String? = null
        var fail = false
        override suspend fun read(): String? {
            check(!fail)
            return value
        }
        override suspend fun write(value: String) {
            check(!fail)
            this.value = value
        }
    }
    private fun document(state: String = "finished") = buildJsonObject {
        put("id", "1")
        put("href", "/app/rest/builds/id:1")
        put("state", state)
    }

    @Test fun durableBuildCacheHonorsLiveAndFinishedExpiryAndRefresh() = runTest {
        val store = Store()
        var time = 100L
        var calls = 0
        val fetch: suspend () -> JsonObject = {
            calls++
            document("running")
        }
        val cache = TeamCityResponseCache("alice", store) { time }
        cache.get("/builds/id:1", false, fetch)
        time += 29_999
        TeamCityResponseCache("alice", store) { time }.get("/builds/id:1", false, fetch)
        assertEquals(1, calls)
        time++
        cache.get("/builds/id:1", false, fetch)
        assertEquals(2, calls)
        cache.get("/builds/id:1", true) {
            calls++
            document()
        }
        time += 3_599_999
        TeamCityResponseCache("alice", store) { time }.get("/builds/id:1", false, fetch)
        assertEquals(3, calls)
        time++
        TeamCityResponseCache("alice", store) { time }.get("/builds/id:1", false, fetch)
        assertEquals(4, calls)
    }

    @Test fun otherAccountsCorruptDataAndClockRollbackCannotServeCachedResults() = runTest {
        val store = Store()
        var calls = 0
        TeamCityResponseCache("alice", store) { 100 }.get("/private", false) { document() }
        TeamCityResponseCache("bob", store) { 101 }.get("/private", false) {
            calls++
            document()
        }
        assertEquals(1, calls)
        TeamCityResponseCache("bob", store) { 99 }.get("/private", false) {
            calls++
            document()
        }
        assertEquals(2, calls)
        store.value = "invalid cache"
        TeamCityResponseCache("bob", store).get("/private", false) {
            calls++
            document()
        }
        assertEquals(3, calls)
    }

    @Test fun invalidationPreventsAnOlderInFlightReadFromRepopulatingTheCache() = runTest {
        val cache = TeamCityResponseCache("alice", Store())
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val old = async {
            cache.get("/builds/id:1", false) {
                entered.complete(Unit)
                release.await()
                document("running")
            }
        }
        entered.await()
        cache.invalidate()
        release.complete(Unit)
        old.await()
        var fetched = false
        cache.get("/builds/id:1", false) {
            fetched = true
            document()
        }
        assertTrue(fetched)
    }

    @Test fun storageFailureDoesNotTurnSuccessfulNetworkResultsIntoFailures() = runTest {
        val cache = TeamCityResponseCache("alice", Store().apply { fail = true })
        assertEquals(document(), cache.get("/builds/id:1", false) { document() })
        assertEquals(document(), cache.get("/builds/id:1", false) { error("memory hit expected") })
    }

    @Test fun cancellationIsNeverSwallowedOrStored() = runTest {
        val store = Store()
        val cache = TeamCityResponseCache("alice", store)
        assertFailsWith<CancellationException> { cache.get("/builds/id:1", false) { throw CancellationException() } }
        assertNull(store.value)
        assertEquals(document(), cache.get("/builds/id:1", false) { document() })
    }

    @Test fun oversizedDocumentsAreDeliveredButNotRetained() = runTest {
        val store = Store()
        val cache = TeamCityResponseCache("alice", store)
        val large = buildJsonObject { put("value", "x".repeat(1_000_001)) }
        assertEquals(large, cache.get("/large", false) { large })
        var fetched = false
        cache.get("/large", false) {
            fetched = true
            document()
        }
        assertTrue(fetched)
        assertFalse(store.value!!.contains("x".repeat(100)))
    }
}
