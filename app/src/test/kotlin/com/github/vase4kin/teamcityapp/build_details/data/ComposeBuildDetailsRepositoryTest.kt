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

package com.github.vase4kin.teamcityapp.build_details.data

import android.app.Application
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.storage.models.UserAccount

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ComposeBuildDetailsRepositoryTest {
    private val legacy = mock<Repository>()
    private val storage = mock<SharedUserStorage>()
    private val mapper = AppBuildLaunchMapper()
    private var active = UserAccount("https://same-server", "alice", byteArrayOf(), false, true)
    private var resolutions = 0
    private val snapshot = BuildLaunchData("42", "opaque&build:42", state = "running", buildTypeId = "latest", branchName = "branch:opaque", properties = BuildProperties(listOf(BuildProperty("name", "value", true, "property", "/property")), "properties", "/properties"))
    private fun repository(scope: TestScope) = ComposeBuildDetailsRepository(
        Provider {
            resolutions++
            legacy
        },
        storage,
        mapper,
        StandardTestDispatcher(scope.testScheduler)
    ).also { whenever(storage.activeUser).thenAnswer { active } }
    private fun http(code: Int) = HttpException(Response.error<Build>(code, "denied".toResponseBody()))

    @Test fun cancelUsesOpaqueCurrentHrefAndReAddFlagAndReturnsCompleteBuild() = runTest {
        val repository = repository(this)
        val response = snapshot.copy(state = "finished", canceledInfo = BuildCancellation("date", BuildUser("alice", "Alice")))
        whenever(legacy.cancelBuild(snapshot.href, BuildCancelRequest(true))).thenReturn(Single.just(mapper.toLegacyBuild(response)))
        assertEquals(BuildDetailsMutation.Success(response), repository.cancel(snapshot, true))
        verify(legacy).cancelBuild(snapshot.href, BuildCancelRequest(true))
        assertEquals(1, resolutions)
    }

    @Test fun restartMatchesInstalledMinimalRequestAndPreservesFullParameterMetadata() = runTest {
        val repository = repository(this)
        whenever(legacy.queueBuild(any())).thenReturn(Single.just(mapper.toLegacyBuild(snapshot.copy(href = "/queued"))))
        assertEquals(BuildDetailsMutation.Success("/queued"), repository.restart(snapshot.copy(personal = true, cleanSources = true, queueAtTop = true)))
        val request = argumentCaptor<Build>()
        verify(legacy).queueBuild(request.capture())
        val wire = request.firstValue
        assertEquals("latest", wire.buildType?.id)
        assertEquals("branch:opaque", wire.branchName)
        assertEquals("properties", wire.properties?.id)
        assertEquals("/properties", wire.properties?.href)
        assertEquals("name", wire.properties?.properties?.single()?.name)
        assertEquals("value", wire.properties?.properties?.single()?.value)
        assertEquals(true, wire.properties?.properties?.single()?.isOwn)
        assertEquals("property", wire.properties?.properties?.single()?.id)
        assertFalse(wire.isPersonal)
        assertFalse(wire.isCleanSources)
        assertFalse(wire.isQueueAtTop)
        assertEquals(1, resolutions)
    }

    @Test fun absentAndNullParameterListsRemainDistinctInRestartRequests() = runTest {
        val repository = repository(this)
        whenever(legacy.queueBuild(any())).thenReturn(Single.just(mapper.toLegacyBuild(snapshot)))
        repository.restart(snapshot.copy(properties = null))
        repository.restart(snapshot.copy(properties = BuildProperties(null)))
        repository.restart(snapshot.copy(properties = BuildProperties(emptyList())))
        val requests = argumentCaptor<Build>()
        verify(legacy, times(3)).queueBuild(requests.capture())
        assertNull(requests.allValues[0].properties)
        assertNotNull(requests.allValues[1].properties)
        assertNull(requests.allValues[1].properties?.properties)
        assertEquals(emptyList<Any>(), requests.allValues[2].properties?.properties)
    }

    @Test fun onlyUserTriggeredByActiveUsernameCountsAsOwnBuild() = runTest {
        val repository = repository(this)
        val build = snapshot.copy(triggered = BuildTrigger("user", user = BuildUser("alice", "Alice")))
        assertTrue(repository.triggeredByCurrentUser(build))
        assertFalse(repository.triggeredByCurrentUser(build.copy(triggered = build.triggered?.copy(type = "vcs"))))
        assertFalse(repository.triggeredByCurrentUser(build.copy(triggered = build.triggered?.copy(user = BuildUser("bob", "Bob")))))
        assertFalse(repository.triggeredByCurrentUser(snapshot))
    }

    @Test fun forbiddenAndServerFailuresUseExplicitResultsWithoutSwallowingCancellation() = runTest {
        val repository = repository(this)
        whenever(legacy.cancelBuild(any(), any())).thenReturn(Single.error(http(403)))
        assertEquals(BuildDetailsMutation.Forbidden, repository.cancel(snapshot, false))
        whenever(legacy.cancelBuild(any(), any())).thenReturn(Single.error(http(500)))
        assertEquals(BuildDetailsMutation.Failed, repository.cancel(snapshot, false))
        whenever(legacy.queueBuild(any())).thenReturn(Single.error(http(403)))
        assertEquals(BuildDetailsMutation.Forbidden, repository.restart(snapshot))
        whenever(legacy.queueBuild(any())).thenReturn(Single.error(IllegalStateException("offline")))
        assertEquals(BuildDetailsMutation.Failed, repository.restart(snapshot))
    }

    @Test fun cancellingOwnerDisposesPendingCancelRestartAndQueuedReads() = runTest {
        val repository = repository(this)
        var cancelDisposed = false
        var restartDisposed = false
        var readDisposed = false
        whenever(legacy.cancelBuild(any(), any())).thenReturn(Single.never<Build>().doOnDispose { cancelDisposed = true })
        whenever(legacy.queueBuild(any())).thenReturn(Single.never<Build>().doOnDispose { restartDisposed = true })
        whenever(legacy.build("/queued", false)).thenReturn(Single.never<Build>().doOnDispose { readDisposed = true })
        val cancel = async { repository.cancel(snapshot, false) }
        val restart = async { repository.restart(snapshot) }
        val read = async { repository.queuedBuild("/queued") }
        runCurrent()
        cancel.cancel()
        restart.cancel()
        read.cancel()
        runCurrent()
        assertTrue(cancelDisposed)
        assertTrue(restartDisposed)
        assertTrue(readDisposed)
        assertTrue(cancel.isCancelled)
        assertTrue(restart.isCancelled)
        assertTrue(read.isCancelled)
    }

    @Test fun accountSwitchWithSameServerRejectsOldSuccessAndOldFailure() = runTest {
        val repository = repository(this)
        val response = SingleSubject.create<Build>()
        whenever(legacy.cancelBuild(any(), any())).thenReturn(response)
        val old = async { repository.cancel(snapshot, false) }
        runCurrent()
        active = UserAccount("https://same-server", "bob", byteArrayOf(), false, true)
        response.onSuccess(mapper.toLegacyBuild(snapshot))
        runCurrent()
        assertTrue(old.isCancelled)
        val failedResponse = SingleSubject.create<Build>()
        whenever(legacy.queueBuild(any())).thenReturn(failedResponse)
        val failed = async { repository.restart(snapshot) }
        runCurrent()
        active = UserAccount("https://same-server", "carol", byteArrayOf(), false, true)
        failedResponse.onError(http(403))
        runCurrent()
        assertTrue(failed.isCancelled)
    }

    @Test fun queuedReadKeepsInstalledCachePolicyAndCompleteLaunchSnapshot() = runTest {
        val repository = repository(this)
        whenever(legacy.build("/queued", false)).thenReturn(Single.just(mapper.toLegacyBuild(snapshot)))
        assertEquals(snapshot, repository.queuedBuild("/queued"))
        verify(legacy).build("/queued", false)
        assertEquals(1, resolutions)
    }
}
