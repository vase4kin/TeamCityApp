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

package teamcityapp.shared

import androidx.lifecycle.ViewModelStore
import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import teamcityapp.libraries.accounts.*

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationPersistenceTest {
    @Test fun failedReadCanRetryAndSuccessfulRestorationIsClaimedOnlyOnce() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val models = ViewModelStore()
        var fail = true
        var reads = 0
        val navigation = object : SavedNavigationStore {
            override suspend fun read(): String? {
                reads++
                check(!fail)
                return "saved public navigation"
            }
            override suspend fun write(snapshot: String) {}
        }
        try {
            val model = SharedAppViewModel(
                VaultAccountStorage(object : SecureVault {
                    override suspend fun read(): String? = null
                    override suspend fun write(value: String) {}
                }),
                navigation
            ).also { models.put("root", it) }
            assertEquals(NavigationRestoration.Failed, model.readInitialNavigation())
            fail = false
            assertEquals(NavigationRestoration.Snapshot("saved public navigation"), model.readInitialNavigation())
            assertEquals(NavigationRestoration.Unneeded, model.readInitialNavigation())
            assertEquals(2, reads)
        } finally {
            models.clear()
            Dispatchers.resetMain()
        }
    }

    @Test fun writeFailureIsReportedForInlineRetryAndCancellationRemainsCancellation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val models = ViewModelStore()
        var fail = true
        var cancel = false
        var saved: String? = null
        val navigation = object : SavedNavigationStore {
            override suspend fun read(): String? = null
            override suspend fun write(snapshot: String) {
                if (cancel) throw CancellationException()
                check(!fail)
                saved = snapshot
            }
        }
        try {
            val model = SharedAppViewModel(
                VaultAccountStorage(object : SecureVault {
                    override suspend fun read(): String? = null
                    override suspend fun write(value: String) {}
                }),
                navigation
            ).also { models.put("root", it) }
            assertFalse(model.saveNavigation("public destination"))
            assertNull(saved)
            fail = false
            assertTrue(model.saveNavigation("public destination"))
            assertEquals("public destination", saved)
            cancel = true
            assertFailsWith<CancellationException> { model.saveNavigation("new destination") }
            assertEquals("public destination", saved)
        } finally {
            models.clear()
            Dispatchers.resetMain()
        }
    }
}
