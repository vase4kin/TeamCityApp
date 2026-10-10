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

package com.github.vase4kin.teamcityapp.favorites.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.favorites.api.FavoriteConfigurations
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference

@OptIn(ExperimentalCoroutinesApi::class)
class AppFavoritesRepositoryTest {
    private fun configuration(id: String, name: String = "Configuration $id", project: String = "Project") = BuildType().apply {
        setId(id)
        setName(name)
        setProjectId(project)
        setProjectName(project)
        setDescription(" raw description ")
    }

    private fun storage(ids: List<String>) = mock<SharedUserStorage> { on { favoriteBuildTypeIds } doReturn ids }

    @Test fun mapsImmutableConfigurationsInSavedOrderAndPreservesDescriptions() = runTest {
        val savedIds = listOf("second", "first")
        val storage = storage(savedIds)
        val legacy = mock<Repository>()
        doReturn(Single.just(configuration("second", "Second", "Z"))).whenever(legacy).buildType("second", false)
        doReturn(Single.just(configuration("first", "First", "A"))).whenever(legacy).buildType("first", false)
        val result = AppFavoritesRepository(storage, Provider { legacy }, StandardTestDispatcher(testScheduler)).favorites(false)
        assertEquals(
            FavoriteConfigurations(
                savedIds,
                listOf(
                    BuildConfigurationSummary("second", "Second", " raw description ", ProjectReference("Z", "Z")),
                    BuildConfigurationSummary("first", "First", " raw description ", ProjectReference("A", "A"))
                )
            ),
            result
        )
        verify(storage).favoriteBuildTypeIds
        verifyNoMoreInteractions(storage)
    }

    @Test fun aFailedIdDoesNotDiscardOtherResultsOrRemoveSavedFavorites() = runTest {
        val savedIds = listOf("first", "deleted", "last")
        val storage = storage(savedIds)
        val legacy = mock<Repository>()
        doReturn(Single.just(configuration("first"))).whenever(legacy).buildType("first", false)
        doReturn(Single.error<BuildType>(IllegalStateException("404"))).whenever(legacy).buildType("deleted", false)
        doReturn(Single.just(configuration("last"))).whenever(legacy).buildType("last", false)
        val result = AppFavoritesRepository(storage, Provider { legacy }, StandardTestDispatcher(testScheduler)).favorites(false)
        assertEquals(savedIds, result.savedIds)
        assertEquals(listOf("first", "last"), result.configurations.map { it.id })
        assertEquals(listOf("deleted"), result.unavailableIds)
        verify(storage).favoriteBuildTypeIds
        verifyNoMoreInteractions(storage)
        verify(legacy).buildType("first", false)
        verify(legacy).buildType("deleted", false)
        verify(legacy).buildType("last", false)
        verifyNoMoreInteractions(legacy)
    }

    @Test fun allFailuresKeepEverySavedIdAvailableForRetry() = runTest {
        val ids = listOf("first", "second")
        val storage = storage(ids)
        val legacy = mock<Repository>()
        doReturn(Single.error<BuildType>(IllegalStateException("offline"))).whenever(legacy).buildType(any(), any())
        val result = AppFavoritesRepository(storage, Provider { legacy }, StandardTestDispatcher(testScheduler)).favorites(true)
        assertEquals(FavoriteConfigurations(ids, emptyList(), ids), result)
        verify(storage).favoriteBuildTypeIds
        verifyNoMoreInteractions(storage)
    }

    @Test fun emptySavedIdsDoNotResolveAnApiGraphOrMakeRequests() = runTest {
        val storage = storage(emptyList())
        var resolutions = 0
        val legacy = mock<Repository>()
        val repository = AppFavoritesRepository(
            storage,
            Provider {
                resolutions++
                legacy
            },
            StandardTestDispatcher(testScheduler)
        )
        assertEquals(FavoriteConfigurations(emptyList(), emptyList()), repository.favorites(false))
        assertEquals(0, resolutions)
        verifyNoInteractions(legacy)
    }

    @Test fun normalReloadAndPullRefreshPreservePerIdCachePolicies() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.just(configuration("first"))).whenever(legacy).buildType(any(), any())
        var resolutions = 0
        val repository = AppFavoritesRepository(
            storage(listOf("first", "second")),
            Provider {
                resolutions++
                legacy
            },
            StandardTestDispatcher(testScheduler)
        )
        repository.favorites(false)
        repository.favorites(true)
        assertEquals(2, resolutions)
        verify(legacy).buildType("first", false)
        verify(legacy).buildType("second", false)
        verify(legacy).buildType("first", true)
        verify(legacy).buildType("second", true)
    }

    @Test fun missingServerIdUsesTheSavedIdAndMissingProjectFieldsStayEmpty() = runTest {
        val legacy = mock<Repository>()
        val details = mock<BuildType>()
        doReturn(Single.just(details)).whenever(legacy).buildType("saved-id", false)
        val result = AppFavoritesRepository(storage(listOf("saved-id")), Provider { legacy }, StandardTestDispatcher(testScheduler)).favorites(false)
        assertEquals(BuildConfigurationSummary("saved-id", "", null, ProjectReference("", "")), result.configurations.single())
        assertEquals(listOf("saved-id"), result.savedIds)
    }

    @Test fun cancellationDisposesEveryPendingParallelIdWithoutChangingStorage() = runTest {
        val legacy = mock<Repository>()
        val disposed = mutableSetOf<String>()
        val ids = listOf("first", "second", "third")
        ids.forEach { id ->
            doReturn(Single.never<BuildType>().doOnDispose { disposed += id }).whenever(legacy).buildType(id, false)
        }
        val storage = storage(ids)
        val pending = async { AppFavoritesRepository(storage, Provider { legacy }, StandardTestDispatcher(testScheduler)).favorites(false) }
        runCurrent()
        ids.forEach { verify(legacy).buildType(it, false) }
        pending.cancel()
        runCurrent()
        assertEquals(ids.toSet(), disposed)
        assertTrue(pending.isCancelled)
        verify(storage).favoriteBuildTypeIds
        verifyNoMoreInteractions(storage)
    }

    @Test fun repositoryCancellationIsNotRecordedAsAnUnavailableFavorite() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.error<BuildType>(CancellationException("cancelled"))).whenever(legacy).buildType("first", false)
        doReturn(Single.just(configuration("second"))).whenever(legacy).buildType("second", false)
        val repository = AppFavoritesRepository(storage(listOf("first", "second")), Provider { legacy }, StandardTestDispatcher(testScheduler))
        val failure = runCatching { repository.favorites(false) }.exceptionOrNull()
        assertTrue(failure is CancellationException)
    }

    @Test fun parallelResponsesCompleteOutOfOrderButResultsKeepSavedIdOrder() = runTest {
        val legacy = mock<Repository>()
        val first = SingleSubject.create<BuildType>()
        val second = SingleSubject.create<BuildType>()
        doReturn(first).whenever(legacy).buildType("first", false)
        doReturn(second).whenever(legacy).buildType("second", false)
        val pending = async { AppFavoritesRepository(storage(listOf("first", "second")), Provider { legacy }, StandardTestDispatcher(testScheduler)).favorites(false) }
        runCurrent()
        assertTrue(first.hasObservers())
        assertTrue(second.hasObservers())
        second.onSuccess(configuration("second"))
        runCurrent()
        assertFalse(pending.isCompleted)
        first.onSuccess(configuration("first"))
        runCurrent()
        assertEquals(listOf("first", "second"), pending.await().configurations.map { it.id })
    }

    @Test fun savedIdsAndMappedDtoFieldsAreSnapshotsRatherThanMutableStorageReferences() = runTest {
        val savedIds = mutableListOf("first", "second")
        val storage = storage(savedIds)
        val legacy = mock<Repository>()
        val first = configuration("first", "Original name")
        val pendingFirst = SingleSubject.create<BuildType>()
        doReturn(pendingFirst).whenever(legacy).buildType("first", false)
        doReturn(Single.just(configuration("second"))).whenever(legacy).buildType("second", false)
        val pending = async { AppFavoritesRepository(storage, Provider { legacy }, StandardTestDispatcher(testScheduler)).favorites(false) }
        runCurrent()
        savedIds.clear()
        savedIds += "new-favorite"
        pendingFirst.onSuccess(first)
        runCurrent()
        val result = pending.await()
        first.setName("Changed name")
        assertEquals(listOf("first", "second"), result.savedIds)
        assertEquals(listOf("first", "second"), result.configurations.map { it.id })
        assertEquals("Original name", result.configurations.first().name)
        verify(legacy, never()).buildType("new-favorite", false)
    }

    @Test fun capturesOneRepositoryForTheWholeBatchAndResolvesTheNewAccountNextTime() = runTest {
        val storage = storage(listOf("first", "second"))
        val firstAccount = mock<Repository>()
        val secondAccount = mock<Repository>()
        val firstResult = SingleSubject.create<BuildType>()
        doReturn(firstResult).whenever(firstAccount).buildType("first", false)
        doReturn(Single.just(configuration("second"))).whenever(firstAccount).buildType("second", false)
        doReturn(Single.just(configuration("new-account-favorite"))).whenever(secondAccount).buildType("new-account-favorite", false)
        var active = firstAccount
        var resolutions = 0
        val repository = AppFavoritesRepository(
            storage,
            Provider {
                resolutions++
                active
            },
            StandardTestDispatcher(testScheduler)
        )
        val firstBatch = async { repository.favorites(false) }
        runCurrent()
        active = secondAccount
        whenever(storage.favoriteBuildTypeIds).thenReturn(listOf("new-account-favorite"))
        firstResult.onSuccess(configuration("first"))
        runCurrent()
        assertEquals(listOf("first", "second"), firstBatch.await().savedIds)
        verify(firstAccount).buildType("second", false)
        verifyNoInteractions(secondAccount)
        val nextBatch = repository.favorites(false)
        assertEquals(listOf("new-account-favorite"), nextBatch.savedIds)
        assertEquals(listOf("new-account-favorite"), nextBatch.configurations.map { it.id })
        assertEquals(2, resolutions)
    }
}
