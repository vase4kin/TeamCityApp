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

package com.github.vase4kin.teamcityapp.overview.router

import android.app.Activity
import android.app.Application
import android.os.Looper
import android.view.View
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsArguments
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.overview.data.*
import java.time.Duration
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import teamcityapp.features.build_overview.api.BuildOverviewAction

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppBuildOverviewRouterImplTest {
    private val activity = mock<Activity>()
    private val arguments = BuildDetailsArguments(overviewArguments())
    private val mapper = AppBuildLaunchMapper()
    private val bus = mock<EventBus>()
    private val prompts = mock<BuildOverviewPrompts>()
    private val router = AppBuildOverviewRouterImpl(activity, arguments, mapper, bus, prompts)

    @Test fun shareAndBrowserListenersObserveCurrentWebUrlBeforeSynchronousDispatch() {
        doAnswer {
            assertEquals("https://ci.example/latest", arguments.current.webUrl)
            null
        }.whenever(bus).post(any())
        router.dispatch(activity, BuildOverviewAction.Share, loadedOverview)
        router.dispatch(activity, BuildOverviewAction.Browser, loadedOverview)
        verify(bus).post(isA<ShareBuildEvent>())
        verify(bus).post(isA<OpenBrowserEvent>())
    }

    @Test fun stopAndQueueRemovalUseCurrentStateHrefAndOwnUserAtDispatchTime() {
        doAnswer {
            assertEquals("/builds/42", arguments.current.href)
            assertTrue(arguments.current.isRunning)
            assertTrue(arguments.current.isTriggeredByUser("alice"))
            assertFalse(arguments.current.isTriggeredByUser("other"))
            null
        }.whenever(bus).post(any())
        router.dispatch(activity, BuildOverviewAction.Stop, loadedOverview)
        verify(bus).post(isA<StopBuildEvent>())
        reset(bus)
        val queued = loadedOverview.copy(state = "queued", href = "/queue/latest")
        doAnswer {
            assertTrue(arguments.current.isQueued)
            assertEquals("/queue/latest", arguments.current.href)
            null
        }.whenever(bus).post(any())
        router.dispatch(activity, BuildOverviewAction.RemoveFromQueue, queued)
        verify(bus).post(isA<StopBuildEvent>())
    }

    @Test fun restartUsesCurrentBranchAndCompletePropertiesWrapperBeforeConfirmation() {
        doAnswer {
            assertEquals("latest-branch", arguments.current.branchName)
            assertEquals(loadedOverview.properties, mapper.toLaunchData(requireNotNull(arguments.currentBuild)).properties)
            assertEquals("latest-configuration", arguments.current.buildTypeId)
            null
        }.whenever(bus).post(any())
        router.dispatch(activity, BuildOverviewAction.Restart, loadedOverview.copy(state = "finished"))
        verify(bus).post(isA<RestartBuildEvent>())
        assertEquals("/old-tests", arguments.initialDetails.testsHref)
    }

    @Test fun projectAndConfigurationNavigationUseTheLatestProjectAndConfiguration() {
        doAnswer {
            assertEquals("latest-configuration", arguments.current.buildTypeId)
            assertEquals("Latest configuration", arguments.current.buildTypeName)
            assertEquals("latest-project", arguments.current.projectId)
            assertEquals("Latest project", arguments.current.projectName)
            null
        }.whenever(bus).post(any())
        router.dispatch(activity, BuildOverviewAction.Configuration, loadedOverview)
        router.dispatch(activity, BuildOverviewAction.Configuration, loadedOverview, "raw branch:feature")
        router.dispatch(activity, BuildOverviewAction.Project, loadedOverview)
        verify(bus).post(isA<StartBuildsListActivityEvent>())
        val navigationEvents = argumentCaptor<Any>()
        verify(bus, times(3)).post(navigationEvents.capture())
        assertEquals("raw branch:feature", navigationEvents.allValues.filterIsInstance<StartBuildsListActivityFilteredByBranchEvent>().single().branchName)
        verify(bus).post(isA<StartProjectActivityEvent>())
    }

    @Test fun resumedOnboardingReceivesLoadedStatusAndCleanupIsUiScoped() {
        router.resumed(activity, loadedOverview)
        verify(prompts).resumed(loadedOverview)
        assertEquals("latest-number", arguments.current.number)
        router.dispose(activity)
        verify(prompts).dispose()
        verifyNoInteractions(bus)
    }

    @Test fun closingSheetPreservesDelayedFabRestoreAndCleanupCancelsStaleCallbacks() {
        router.sheetVisibility(activity, true)
        val hidden = argumentCaptor<Any>()
        verify(bus).post(hidden.capture())
        assertEquals(View.GONE, (hidden.firstValue as FloatButtonChangeVisibilityEvent).visibility)
        router.sheetVisibility(activity, false)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(499))
        verify(bus, times(1)).post(any())
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1))
        val events = argumentCaptor<Any>()
        verify(bus, times(2)).post(events.capture())
        assertEquals(listOf(View.GONE, View.VISIBLE), events.allValues.map { (it as FloatButtonChangeVisibilityEvent).visibility })
    }

    @Test fun disposalRestoresFabOnceAndCancelsTheDelayedOldHostRestore() {
        router.sheetVisibility(activity, true)
        router.sheetVisibility(activity, false)
        router.dispose(activity)
        router.dispose(activity)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
        val events = argumentCaptor<Any>()
        verify(bus, times(2)).post(events.capture())
        assertEquals(listOf(View.GONE, View.VISIBLE), events.allValues.map { (it as FloatButtonChangeVisibilityEvent).visibility })
    }

    @Test fun adapterRejectsAnotherActivityBeforePublishingDataOrEvents() {
        assertTrue(runCatching { router.dispatch(mock<Activity>(), BuildOverviewAction.Share, loadedOverview) }.exceptionOrNull() is IllegalArgumentException)
        assertEquals("old-number", arguments.current.number)
        verifyNoInteractions(bus, prompts)
    }
}
