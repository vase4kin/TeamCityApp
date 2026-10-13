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

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import io.rx_cache2.ConfigProvider
import io.rx_cache2.DynamicKey
import io.rx_cache2.EvictDynamicKey
import io.rx_cache2.internal.CacheProviderProxy
import io.rx_cache2.internal.ProcessorProviders
import java.util.NoSuchElementException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CacheProviderProxyTest {
    private val processor = mock<ProcessorProviders>()
    private val providers = CacheProviderProxy.create(processor)
    private val row = Gson().fromJson("""{"id":"1","href":"/builds/1","state":"finished"}""", Build::class.java)

    @Test fun missedStartupSignalReentersWithTheSameConfigAndLoadsOnlyOnce() {
        var subscriptions = 0
        whenever(processor.process<Build>(any()))
            .thenReturn(Observable.empty())
            .thenAnswer { it.getArgument<ConfigProvider>(0).loaderObservable }
        val response = providers.build(
            Single.fromCallable {
                subscriptions++
                row
            },
            DynamicKey(row.href),
            EvictDynamicKey(true)
        )
        // The recovery must stay deferred until an empty completion is observed.
        verify(processor).process<Build>(any())
        assertEquals(0, subscriptions)
        response.test().assertComplete().assertNoErrors().assertValue(row)
        assertEquals(1, subscriptions)
        val configs = argumentCaptor<ConfigProvider>()
        verify(processor, times(2)).process<Build>(configs.capture())
        assertSame(configs.firstValue, configs.secondValue)
    }

    @Test fun successfulResponseDoesNotReenterTheProcessor() {
        whenever(processor.process<Build>(any())).thenReturn(Observable.just(row))
        request().test().assertComplete().assertNoErrors().assertValue(row)
        verify(processor).process<Build>(any())
    }

    @Test fun failuresPropagateWithoutRetryingTheLoader() {
        val failure = IllegalStateException("disk or loader failure")
        whenever(processor.process<Build>(any())).thenReturn(Observable.error(failure))
        request().test().assertError(failure).assertNoValues()
        verify(processor).process<Build>(any())
    }

    @Test fun repeatedEmptyCompletionFailsAfterOneRecoveryAttempt() {
        whenever(processor.process<Build>(any())).thenReturn(Observable.empty())
        request().test().assertError(NoSuchElementException::class.java).assertNoValues()
        verify(processor, times(2)).process<Build>(any())
    }

    @Test fun cancellingTheRecoveredRequestDisposesItsLoader() {
        val pending = SingleSubject.create<Build>()
        whenever(processor.process<Build>(any()))
            .thenReturn(Observable.empty())
            .thenAnswer { it.getArgument<ConfigProvider>(0).loaderObservable }
        val observer = providers.build(pending, DynamicKey(row.href), EvictDynamicKey(true)).test()
        assertTrue(pending.hasObservers())
        observer.dispose()
        assertFalse(pending.hasObservers())
        pending.onSuccess(row)
        observer.assertNoValues()
        verify(processor, times(2)).process<Build>(any())
    }

    private fun request() = providers.build(Single.just(row), DynamicKey(row.href), EvictDynamicKey(false))
}
