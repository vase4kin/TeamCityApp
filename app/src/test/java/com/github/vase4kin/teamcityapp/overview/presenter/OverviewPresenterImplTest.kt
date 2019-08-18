/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.overview.presenter

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations

private const val HREF = "href"

class OverviewPresenterImplTest {

    @Captor
    private lateinit var onPromptShownListenerArgumentCaptor: ArgumentCaptor<OnboardingManager.OnPromptShownListener>
    @Mock
    private lateinit var menuItem: MenuItem
    @Mock
    private lateinit var menu: Menu
    @Mock
    private lateinit var menuInflater: MenuInflater
    @Mock
    private lateinit var view: OverviewViewImpl
    @Mock
    private lateinit var interactor: OverViewInteractor
    @Mock
    private lateinit var tracker: OverviewTracker
    @Mock
    private lateinit var buildDetails: BuildDetails
    @Mock
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var presenter: OverviewPresenterImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = OverviewPresenterImpl(view, interactor, tracker, onboardingManager)
        `when`(interactor.buildDetails).thenReturn(buildDetails)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, interactor, tracker, buildDetails, onboardingManager)
    }

    @Test
    fun testOnCreateOptionsMenuIfBuildIsRunning() {
        `when`(buildDetails.isRunning).thenReturn(true)
        presenter.onCreateOptionsMenu(menu, menuInflater)
        verify(interactor).buildDetails
        verify(buildDetails).isRunning
        verify(view).createStopBuildOptionsMenu(eq(menu), eq(menuInflater))
    }

    @Test
    fun testOnCreateOptionsMenuIfBuildIsQueued() {
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(true)
        presenter.onCreateOptionsMenu(menu, menuInflater)
        verify(interactor).buildDetails
        verify(buildDetails).isRunning
        verify(buildDetails).isQueued
        verify(view).createRemoveBuildFromQueueOptionsMenu(eq(menu), eq(menuInflater))
    }

    @Test
    fun testOnCreateOptionsMenuIfBuildIsFinished() {
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(false)
        presenter.onCreateOptionsMenu(menu, menuInflater)
        verify(interactor).buildDetails
        verify(buildDetails).isRunning
        verify(buildDetails).isQueued
        verify(view).createDefaultOptionsMenu(eq(menu), eq(menuInflater))
    }

    @Test
    fun testOnPrepareOptionsMenu() {
        presenter.onPrepareOptionsMenu(menu)
    }

    @Test
    fun testOnOptionsItemSelected() {
        `when`(view.onOptionsItemSelected(menuItem)).thenReturn(true)
        assertThat(presenter.onOptionsItemSelected(menuItem), `is`(true))
        verify(view).onOptionsItemSelected(eq(menuItem))
    }

    @Test
    fun testOnCancelBuildContextMenuClick() {
        presenter.onCancelBuildContextMenuClick()
        verify(interactor).postStopBuildEvent()
        verify(tracker).trackUserClickedCancelBuildOption()
    }

    @Test
    fun testOnShareButtonClick() {
        presenter.onShareButtonClick()
        verify(interactor).postShareBuildInfoEvent()
        verify(tracker).trackUserSharedBuild()
    }

    @Test
    fun testOnRestartBuildButtonClick() {
        presenter.onRestartBuildButtonClick()
        verify(interactor).postRestartBuildEvent()
        verify(tracker).trackUserRestartedBuild()
    }

    @Test
    fun testOnBrowseBuildButtonClick() {
        presenter.onOpenBrowser()
        verify(interactor).postOpenBrowserEvent()
        verify(tracker).trackUserOpenBrowser()
    }

    @Test
    fun testOnStart() {
        presenter.onStart()
        verify(interactor).subscribeToEventBusEvents()
    }

    @Test
    fun testOnStop() {
        presenter.onStop()
        verify(interactor).unsubsribeFromEventBusEvents()
    }

    @Test
    fun testOnCreate() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.href).thenReturn(HREF)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(interactor).setListener(eq(presenter))
        verify(view).showSkeletonView()
        verify(interactor, times(2)).buildDetails
        verify(buildDetails).href
        verify(buildDetails).isRunning
        verify(interactor).load(eq(HREF), eq(presenter), eq(false))
    }

    @Test
    fun testOnCreateIfBuildIsRunning() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isRunning).thenReturn(true)
        `when`(buildDetails.href).thenReturn(HREF)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(interactor).setListener(eq(presenter))
        verify(view).showSkeletonView()
        verify(interactor, times(2)).buildDetails
        verify(buildDetails).href
        verify(buildDetails).isRunning
        verify(interactor).load(eq(HREF), eq(presenter), eq(true))
    }

    @Test
    fun testOnDestroy() {
        presenter.onDestroy()
        verify(view).unbindViews()
        verify(interactor).unsubscribe()
    }

    @Test
    fun testOnDataRefreshEvent() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.href).thenReturn(HREF)
        presenter.onDataRefreshEvent()
        verify(view).showRefreshingProgress()
        verify(view).hideErrorView()
        verify(interactor).buildDetails
        verify(buildDetails).href
        verify(interactor).load(eq(HREF), eq(presenter), eq(true))
    }

    @Test
    fun testOnRefresh() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.href).thenReturn(HREF)
        presenter.onRefresh()
        verify(view).hideErrorView()
        verify(interactor).buildDetails
        verify(buildDetails).href
        verify(interactor).load(eq(HREF), eq(presenter), eq(true))
    }

    @Test
    fun testOnRetry() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.href).thenReturn(HREF)
        presenter.onRetry()
        verify(view).hideErrorView()
        verify(view).showRefreshingProgress()
        verify(interactor).buildDetails
        verify(buildDetails).href
        verify(interactor).load(eq(HREF), eq(presenter), eq(true))
    }

    @Test
    fun testOnFail() {
        presenter.onFail("error")
        verify(view).hideCards()
        verify(view).hideSkeletonView()
        verify(view).hideRefreshingProgress()
        verify(view).showErrorView()
    }

    @Test
    fun testOnBranchCardClick() {
        presenter.onBranchCardClick("br")
        verify(view).showBranchCardBottomSheetDialog(eq("br"))
    }

    @Test
    fun testOnShowBuildsActionClick() {
        presenter.onNavigateToBuildListEvent("branch")
        verify(interactor).postStartBuildListActivityFilteredByBranchEvent(eq("branch"))
        verify(tracker).trackUserWantsToSeeBuildListFilteredByBranch()
    }

    @Test
    fun testOnCardClick() {
        presenter.onCardClick("head", "descr")
        verify(view).showDefaultCardBottomSheetDialog(eq("head"), eq("descr"))
    }

    @Test
    fun testOnNavigateToBuildListEvent() {
        presenter.onNavigateToBuildListEvent()
        verify(interactor).postStartBuildListActivityEvent()
        verify(tracker).trackUserOpensBuildType()
    }

    @Test
    fun testOnNavigateToProjectEvent() {
        presenter.onNavigateToProjectEvent()
        verify(interactor).postStartProjectActivityEvent()
        verify(tracker).trackUserOpensProject()
    }

    @Test
    fun testOnBottomSheetDismiss() {
        presenter.onBottomSheetDismiss()
        verify(interactor).postFABVisibleEvent()
    }

    @Test
    fun testOnBottomSheetShow() {
        presenter.onBottomSheetShow()
        verify(interactor).postFABGoneEvent()
    }

    @Test
    fun testRestartBuildPromptIsShownIfItIsShown() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isFinished).thenReturn(true)
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(false)
        `when`(onboardingManager.isRestartBuildPromptShown).thenReturn(true)
        `when`(onboardingManager.isStopBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isRemoveBuildFromQueuePromptShown).thenReturn(false)
        presenter.onResume()
        verify(interactor).buildDetails
        verify(buildDetails).isFinished
        verify(onboardingManager).isRestartBuildPromptShown
        verify(buildDetails).isRunning
        verify(buildDetails).isQueued
    }

    @Test
    fun testRestartBuildPromptIsShownIfItIsNotShown() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isFinished).thenReturn(true)
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(false)
        `when`(onboardingManager.isRestartBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isStopBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isRemoveBuildFromQueuePromptShown).thenReturn(false)
        presenter.onResume()
        verify(interactor).buildDetails
        verify(buildDetails).isFinished
        verify(onboardingManager).isRestartBuildPromptShown
        verify(view).showRestartBuildPrompt(capture(onPromptShownListenerArgumentCaptor))
        val listener = onPromptShownListenerArgumentCaptor.value
        listener.onPromptShown()
        verify(onboardingManager).saveRestartBuildPromptShown()
    }

    @Test
    fun testStopBuildPromptIsShownIfItIsShown() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isFinished).thenReturn(false)
        `when`(buildDetails.isRunning).thenReturn(true)
        `when`(buildDetails.isQueued).thenReturn(false)
        `when`(onboardingManager.isRestartBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isStopBuildPromptShown).thenReturn(true)
        `when`(onboardingManager.isRemoveBuildFromQueuePromptShown).thenReturn(false)
        presenter.onResume()
        verify(interactor).buildDetails
        verify(buildDetails).isFinished
        verify(buildDetails).isRunning
        verify(onboardingManager).isStopBuildPromptShown
        verify(buildDetails).isQueued
    }

    @Test
    fun testStopBuildPromptIsShownIfItIsNotShown() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isFinished).thenReturn(false)
        `when`(buildDetails.isRunning).thenReturn(true)
        `when`(buildDetails.isQueued).thenReturn(false)
        `when`(onboardingManager.isRestartBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isStopBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isRemoveBuildFromQueuePromptShown).thenReturn(false)
        presenter.onResume()
        verify(interactor).buildDetails
        verify(buildDetails).isFinished
        verify(buildDetails).isRunning
        verify(onboardingManager).isStopBuildPromptShown
        verify(view).showStopBuildPrompt(capture(onPromptShownListenerArgumentCaptor))
        val listener = onPromptShownListenerArgumentCaptor.value
        listener.onPromptShown()
        verify(onboardingManager).saveStopBuildPromptShown()
    }

    @Test
    fun testRemoveBuildPromptIsShownIfItIsShown() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isFinished).thenReturn(false)
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(true)
        `when`(onboardingManager.isRestartBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isStopBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isRemoveBuildFromQueuePromptShown).thenReturn(true)
        presenter.onResume()
        verify(interactor).buildDetails
        verify(buildDetails).isFinished
        verify(buildDetails).isRunning
        verify(buildDetails).isQueued
        verify(onboardingManager).isRemoveBuildFromQueuePromptShown
    }

    @Test
    fun testRemoveBuildPromptIsShownIfItIsNotShown() {
        `when`(interactor.buildDetails).thenReturn(buildDetails)
        `when`(buildDetails.isFinished).thenReturn(false)
        `when`(buildDetails.isRunning).thenReturn(false)
        `when`(buildDetails.isQueued).thenReturn(true)
        `when`(onboardingManager.isRestartBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isStopBuildPromptShown).thenReturn(false)
        `when`(onboardingManager.isRemoveBuildFromQueuePromptShown).thenReturn(false)
        presenter.onResume()
        verify(interactor).buildDetails
        verify(buildDetails).isFinished
        verify(buildDetails).isRunning
        verify(buildDetails).isQueued
        verify(onboardingManager).isRemoveBuildFromQueuePromptShown
        verify(view).showRemoveBuildFromQueuePrompt(capture(onPromptShownListenerArgumentCaptor))
        val listener = onPromptShownListenerArgumentCaptor.value
        listener.onPromptShown()
        verify(onboardingManager).saveRemoveBuildFromQueuePromptShown()
    }
}
