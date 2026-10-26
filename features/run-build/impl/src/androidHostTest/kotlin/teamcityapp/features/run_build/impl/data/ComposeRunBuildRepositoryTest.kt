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

package teamcityapp.features.run_build.impl.data

import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.google.gson.Gson
import io.reactivex.Single
import java.util.concurrent.Executors
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.HttpException
import retrofit2.Response
import teamcityapp.features.run_build.api.*

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeRunBuildRepositoryTest {
    @Test fun accountResolutionAndAllSynchronousRequestsRunOnIo() = runBlocking {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher().use { io ->
            val callerThread = Thread.currentThread()
            val ioThread = withContext(io) { Thread.currentThread() }
            assertNotSame(callerThread, ioThread)
            val legacy = mock<Repository>()
            val branches = Gson().fromJson("""{"branch":[{"name":"main"}]}""", Branches::class.java)
            val agents = Agents(1, listOf(Agent("Linux").apply { id = "42" }))
            val queued = Gson().fromJson("""{"href":"/app/rest/buildQueue/id:42"}""", Build::class.java)
            doAnswer {
                assertSame(ioThread, Thread.currentThread())
                Single.fromCallable {
                    assertSame(ioThread, Thread.currentThread())
                    branches
                }
            }.whenever(legacy).listBranches("bt1")
            doAnswer {
                assertSame(ioThread, Thread.currentThread())
                Single.fromCallable {
                    assertSame(ioThread, Thread.currentThread())
                    BuildType()
                }
            }.whenever(legacy).buildType("bt1", false)
            doAnswer {
                assertSame(ioThread, Thread.currentThread())
                Single.fromCallable {
                    assertSame(ioThread, Thread.currentThread())
                    agents
                }
            }.whenever(legacy).listAgents(false, null, null, false)
            doAnswer {
                assertSame(ioThread, Thread.currentThread())
                Single.fromCallable {
                    assertSame(ioThread, Thread.currentThread())
                    queued
                }
            }.whenever(legacy).queueBuild(any())
            val repository = DefaultRunBuildRepository(
                Provider {
                    assertSame(ioThread, Thread.currentThread())
                    legacy
                },
                io
            )
            withTimeout(5_000) {
                assertEquals(listOf("main"), repository.branches("bt1"))
                assertEquals(listOf(BuildAgent("42", "Linux")), repository.agents("bt1"))
                assertEquals(QueueBuildResult.Success(queued.href), repository.queue(BuildRequest("bt1")))
            }
            assertSame(callerThread, Thread.currentThread())
        }
    }

    @Test fun cancellationDisposesTheQueueSubscriptionWithoutReturningAnError() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        whenever(legacy.queueBuild(any())).thenReturn(Single.never<Build>().doOnDispose { disposed = true })
        val repository = DefaultRunBuildRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        var result: QueueBuildResult? = null
        val pending = async { result = repository.queue(BuildRequest("bt1")) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
        assertTrue(pending.isCancelled)
        assertNull(result)
    }

    @Test fun cancellationDisposesTheAgentSubscription() = runTest {
        val legacy = mock<Repository>()
        whenever(legacy.buildType("bt1", false)).thenReturn(Single.just(BuildType()))
        var disposed = false
        whenever(legacy.listAgents(false, null, null, false)).thenReturn(Single.never<Agents>().doOnDispose { disposed = true })
        val pending = async { DefaultRunBuildRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).agents("bt1") }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
        assertTrue(pending.isCancelled)
    }

    @Test fun failedQueueRequestReturnsAnError() = runTest {
        val legacy = mock<Repository>()
        whenever(legacy.queueBuild(any())).thenReturn(Single.error(IllegalStateException("offline")))
        assertEquals(QueueBuildResult.Error, DefaultRunBuildRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).queue(BuildRequest("bt1")))
    }

    @Test fun modernServerUsesTheCompatibleAgentLocatorAndPreservesAgentIds() = runTest {
        val legacy = mock<Repository>()
        val type = Gson().fromJson("""{"id":"bt1","compatibleAgents":{"href":"/agents"}}""", BuildType::class.java)
        whenever(legacy.buildType("bt1", false)).thenReturn(Single.just(type))
        whenever(legacy.listAgents(null, null, "compatible:(buildType:(id:bt1))", false)).thenReturn(Single.just(Agents(1, listOf(Agent("Linux").apply { id = "42" }))))
        assertEquals(listOf(BuildAgent("42", "Linux")), DefaultRunBuildRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).agents("bt1"))
    }

    @Test fun olderServerFallsBackToConnectedAgents() = runTest {
        val legacy = mock<Repository>()
        whenever(legacy.buildType("bt1", false)).thenReturn(Single.just(BuildType()))
        whenever(legacy.listAgents(false, null, null, false)).thenReturn(Single.just(Agents(0, emptyList())))
        assertTrue(DefaultRunBuildRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).agents("bt1").isEmpty())
        verify(legacy).listAgents(false, null, null, false)
    }

    @Test fun cancellationDisposesTheBranchSubscription() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        whenever(legacy.listBranches("bt1")).thenReturn(Single.never<Branches>().doOnDispose { disposed = true })
        val pending = async { DefaultRunBuildRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).branches("bt1") }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
    }

    @Test fun accountRepositoryIsResolvedAgainForEachOperation() = runTest {
        val first = mock<Repository>()
        val second = mock<Repository>()
        val branches = Gson().fromJson("""{"branch":[{"name":"main"}]}""", Branches::class.java)
        whenever(first.listBranches("bt1")).thenReturn(Single.just(branches))
        whenever(second.listBranches("bt1")).thenReturn(Single.just(branches))
        var active = first
        val repository = DefaultRunBuildRepository(Provider { active }, StandardTestDispatcher(testScheduler))
        repository.branches("bt1")
        active = second
        repository.branches("bt1")
        verify(first).listBranches("bt1")
        verify(second).listBranches("bt1")
    }

    @Test fun forbiddenQueueResponseHasItsOwnResult() = runTest {
        val legacy = mock<Repository>()
        whenever(legacy.queueBuild(any())).thenReturn(Single.error(HttpException(Response.error<Any>(403, "Forbidden".toResponseBody()))))
        assertEquals(QueueBuildResult.Forbidden, DefaultRunBuildRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).queue(BuildRequest("bt1")))
    }
}
