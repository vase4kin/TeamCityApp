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

package com.github.vase4kin.teamcityapp.api.cache

import dagger.Lazy
import io.reactivex.Observable
import io.rx_cache2.internal.RxCache
import io.victoralbertos.jolyglot.GsonSpeaker
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.*

class CacheManagerImplTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun aColdCacheCanBeEvictedBeforeTheAccountApiGraphHasBeenCreated() {
        val cache = RxCache.Builder().persistence(folder.newFolder(), GsonSpeaker())
        var initialized = false
        val manager = CacheManagerImpl(
            cache,
            Lazy {
                initialized = true
                cache.using(CacheProviders::class.java)
            }
        )
        assertFalse(initialized)
        manager.evictAllCache()
        assertTrue(initialized)
    }

    @Test fun evictionWaitsForCompletionAndPropagatesFailuresForTheScreenToRetry() {
        val cache = mock(RxCache::class.java)
        val failure = IllegalStateException("disk failure")
        `when`(cache.evictAll()).thenReturn(Observable.error(failure))
        val manager = CacheManagerImpl(cache, Lazy { mock(CacheProviders::class.java) })
        try {
            manager.evictAllCache()
            fail("Eviction failure should propagate")
        } catch (e: IllegalStateException) {
            assertSame(failure, e)
        }
    }
}
