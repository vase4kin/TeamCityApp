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

package com.github.vase4kin.teamcityapp.runbuild.data

import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import com.google.gson.Gson
import io.reactivex.Single
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.HttpException
import retrofit2.Response
import teamcityapp.features.run_build.api.*

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeRunBuildRepositoryTest {
    @Test fun modernServerUsesTheCompatibleAgentLocatorAndPreservesAgentIds() = runTest {
        val legacy = mock<Repository>()
        val type = Gson().fromJson("""{"id":"bt1","compatibleAgents":{"href":"/agents"}}""", BuildType::class.java)
        whenever(legacy.buildType("bt1", false)).thenReturn(Single.just(type))
        whenever(legacy.listAgents(null, null, "compatible:(buildType:(id:bt1))", false)).thenReturn(Single.just(Agents(1, listOf(Agent("Linux").apply { id = "42" }))))
        assertEquals(listOf(BuildAgent("42", "Linux")), ComposeRunBuildRepository(Provider { legacy }).agents("bt1"))
    }

    @Test fun olderServerFallsBackToConnectedAgents() = runTest {
        val legacy = mock<Repository>()
        whenever(legacy.buildType("bt1", false)).thenReturn(Single.just(BuildType()))
        whenever(legacy.listAgents(false, null, null, false)).thenReturn(Single.just(Agents(0, emptyList())))
        assertTrue(ComposeRunBuildRepository(Provider { legacy }).agents("bt1").isEmpty())
        verify(legacy).listAgents(false, null, null, false)
    }

    @Test fun cancellationDisposesTheBranchSubscription() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        whenever(legacy.listBranches("bt1")).thenReturn(Single.never<Branches>().doOnDispose { disposed = true })
        val pending = async { ComposeRunBuildRepository(Provider { legacy }).branches("bt1") }
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
        val repository = ComposeRunBuildRepository(Provider { active })
        repository.branches("bt1")
        active = second
        repository.branches("bt1")
        verify(first).listBranches("bt1")
        verify(second).listBranches("bt1")
    }

    @Test fun forbiddenQueueResponseHasItsOwnResult() = runTest {
        val legacy = mock<Repository>()
        whenever(legacy.queueBuild(any())).thenReturn(Single.error(HttpException(Response.error<Any>(403, "Forbidden".toResponseBody()))))
        assertEquals(QueueBuildResult.Forbidden, ComposeRunBuildRepository(Provider { legacy }).queue(BuildRequest("bt1")))
    }
}
