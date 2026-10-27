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

package com.github.vase4kin.teamcityapp.di.modules

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred

/** A controllable response whose waiters follow each request's coroutine lifetime. */
class PendingFixture<T> {
    private val result = CompletableDeferred<T>()
    private val waiters = AtomicInteger()
    val isAwaiting: Boolean get() = waiters.get() > 0

    suspend fun await(): T {
        waiters.incrementAndGet()
        try {
            return result.await()
        } finally {
            waiters.decrementAndGet()
        }
    }

    fun complete(value: T) {
        result.complete(value)
    }
    fun fail(error: Throwable) {
        result.completeExceptionally(error)
    }
}
