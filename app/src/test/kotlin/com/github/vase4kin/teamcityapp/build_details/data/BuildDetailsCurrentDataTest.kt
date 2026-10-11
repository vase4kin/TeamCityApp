/*
 * Copyright 2019 Andrey Tolpeev
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
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.overview.data.*
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.libraries.storage.models.UserAccount

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildDetailsCurrentDataTest {
    private val arguments = BuildDetailsArguments(overviewArguments())
    private val mapper = AppBuildLaunchMapper()
    private val storage = mock<SharedUserStorage>()
    private val repository = mock<Repository>()
    private val interactor = BuildDetailsInteractorImpl(mock<EventBus>(), arguments, storage, repository)

    @Before fun schedulers() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After fun resetSchedulers() {
        interactor.unsubsribe()
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    @Test fun currentGettersAndOwnUserStopDecisionReplaceIncomingSnapshot() {
        val user = mock<UserAccount> { on { userName } doReturn "alice" }
        whenever(storage.activeUser).thenReturn(user)
        assertFalse(interactor.isBuildTriggeredByMe())
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        assertTrue(interactor.isBuildTriggeredByMe())
        assertEquals("https://ci.example/latest", interactor.getWebUrl())
        assertEquals("Latest configuration", interactor.getBuildTypeName())
        assertEquals("latest-project", interactor.getProjectId())
        assertEquals("Latest project", interactor.getProjectName())
        assertSame(arguments.current, interactor.getBuildDetails())
    }

    @Test fun stopRequestUsesTheCurrentlyLoadedHrefAndReturnsExistingCompatibilityBuild() {
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        val stopped = mapper.toLegacyBuild(loadedOverview.copy(state = "finished"))
        doReturn(Single.just(stopped)).whenever(repository).cancelBuild(eq("/builds/42"), any())
        val callback = mock<LoadingListenerWithForbiddenSupport<Build>>()
        interactor.cancelBuild(callback, false)
        verify(repository).cancelBuild(eq("/builds/42"), any())
        verify(callback).onSuccess(stopped)
        verify(repository, never()).cancelBuild(eq("/queue/42"), any())
    }

    @Test fun missingCurrentConfigurationNameUsesTheIncomingNameFallback() {
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview.copy(configuration = null)))
        assertEquals("Incoming name", interactor.getBuildTypeName())
        assertEquals("", interactor.getProjectId())
        assertEquals("", interactor.getProjectName())
    }

    @Test fun partialLoadedBuildUsesNestedConfigurationAndMissingUrlWithoutChangingTheWireSnapshot() {
        val mapper = com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper()
        val model = teamcityapp.libraries.builds.BuildLaunchData(
            id = "partial",
            href = "/builds/partial",
            configuration = teamcityapp.libraries.builds.BuildConfigurationData("nested-id", "Configuration")
        )
        val arguments = BuildDetailsArguments(null)
        arguments.publishLoaded(mapper.toLegacyBuild(model))
        org.junit.Assert.assertEquals("nested-id", arguments.current.buildTypeId)
        org.junit.Assert.assertEquals("", arguments.current.webUrl)
        org.junit.Assert.assertEquals(model, mapper.toLaunchData(arguments.current.toBuild()))
    }
}
