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

package com.github.vase4kin.teamcityapp.navigation.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.navigation.api.*
import io.reactivex.Single
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference

@OptIn(ExperimentalCoroutinesApi::class)
class AppNavigationRepositoryTest {
    private fun node(items: List<NavigationItem>): NavigationNode = mock { on { objects } doReturn items }

    @Test fun mapsProjectsAndConfigurationsInServerSectionOrder() = runTest {
        val project = Project().apply {
            id = "P"
            name = "Project"
            description = "description"
        }
        val configuration = BuildType().apply {
            id = "C"
            name = "Configuration"
            projectId = "P"
            projectName = "Project"
        }
        val legacy = mock<Repository>()
        doReturn(Single.just(node(listOf(project, configuration)))).whenever(legacy).listBuildTypes("_Root", false)
        val result = AppNavigationRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).entries("_Root", false)
        assertEquals(listOf(NavigationEntry.Project(ProjectReference("P", "Project"), "description"), NavigationEntry.Configuration(BuildConfigurationSummary("C", "Configuration", null, ProjectReference("P", "Project")))), result)
    }

    @Test fun refreshAndReturnPreserveTheirCachePoliciesAndEmptyNodes() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.just(node(emptyList()))).whenever(legacy).listBuildTypes(any(), any())
        val repository = AppNavigationRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        assertTrue(repository.entries("project", false).isEmpty())
        assertTrue(repository.entries("project", true).isEmpty())
        verify(legacy).listBuildTypes("project", false)
        verify(legacy).listBuildTypes("project", true)
    }

    @Test fun cancellationDisposesTheNodeSubscription() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        doReturn(Single.never<NavigationNode>().doOnDispose { disposed = true }).whenever(legacy).listBuildTypes(any(), any())
        val pending = async { AppNavigationRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).entries("project", false) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
    }

    @Test fun resolvesTheActiveAccountRepositoryForEveryRequest() = runTest {
        val first = mock<Repository>()
        val second = mock<Repository>()
        doReturn(Single.just(node(emptyList()))).whenever(first).listBuildTypes(any(), any())
        doReturn(Single.just(node(emptyList()))).whenever(second).listBuildTypes(any(), any())
        var active = first
        val repository = AppNavigationRepository(Provider { active }, StandardTestDispatcher(testScheduler))
        repository.entries("same-project", false)
        active = second
        repository.entries("same-project", false)
        verify(first).listBuildTypes("same-project", false)
        verify(second).listBuildTypes("same-project", false)
    }
}
