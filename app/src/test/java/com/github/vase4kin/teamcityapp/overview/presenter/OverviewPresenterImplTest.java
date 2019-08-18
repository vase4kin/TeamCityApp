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

package com.github.vase4kin.teamcityapp.overview.presenter;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker;
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class OverviewPresenterImplTest {

    private static final String HREF = "href";

    @Captor
    private ArgumentCaptor<OnboardingManager.OnPromptShownListener> mOnPromptShownListenerArgumentCaptor;

    @Mock
    private MenuItem mMenuItem;

    @Mock
    private Menu mMenu;

    @Mock
    private MenuInflater mMenuInflater;

    @Mock
    private OnLoadingListener<List<BuildElement>> mLoadingListener;

    @Mock
    private OverviewViewImpl mView;

    @Mock
    private OverViewInteractor mInteractor;

    @Mock
    private OverviewTracker mTracker;

    @Mock
    private BuildDetails mBuildDetails;

    @Mock
    private OnboardingManager mOnboardingManager;

    private OverviewPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new OverviewPresenterImpl(mView, mInteractor, mTracker, mOnboardingManager);
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mView, mInteractor, mTracker, mBuildDetails, mOnboardingManager);
    }

    @Test
    public void testOnCreateOptionsMenuIfBuildIsRunning() {
        when(mBuildDetails.isRunning()).thenReturn(true);
        mPresenter.onCreateOptionsMenu(mMenu, mMenuInflater);
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isRunning();
        verify(mView).createStopBuildOptionsMenu(eq(mMenu), eq(mMenuInflater));
    }

    @Test
    public void testOnCreateOptionsMenuIfBuildIsQueued() {
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(true);
        mPresenter.onCreateOptionsMenu(mMenu, mMenuInflater);
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).isQueued();
        verify(mView).createRemoveBuildFromQueueOptionsMenu(eq(mMenu), eq(mMenuInflater));
    }

    @Test
    public void testOnCreateOptionsMenuIfBuildIsFinished() {
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(false);
        mPresenter.onCreateOptionsMenu(mMenu, mMenuInflater);
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).isQueued();
        verify(mView).createDefaultOptionsMenu(eq(mMenu), eq(mMenuInflater));
    }

    @Test
    public void testOnPrepareOptionsMenu() {
        mPresenter.onPrepareOptionsMenu(mMenu);
    }

    @Test
    public void testOnOptionsItemSelected() {
        when(mView.onOptionsItemSelected(mMenuItem)).thenReturn(true);
        assertThat(mPresenter.onOptionsItemSelected(mMenuItem), is(true));
        verify(mView).onOptionsItemSelected(eq(mMenuItem));
    }

    @Test
    public void testOnCancelBuildContextMenuClick() {
        mPresenter.onCancelBuildContextMenuClick();
        verify(mInteractor).postStopBuildEvent();
        verify(mTracker).trackUserClickedCancelBuildOption();
    }

    @Test
    public void testOnShareButtonClick() {
        mPresenter.onShareButtonClick();
        verify(mInteractor).postShareBuildInfoEvent();
        verify(mTracker).trackUserSharedBuild();
    }

    @Test
    public void testOnRestartBuildButtonClick() {
        mPresenter.onRestartBuildButtonClick();
        verify(mInteractor).postRestartBuildEvent();
        verify(mTracker).trackUserRestartedBuild();
    }

    @Test
    public void testOnStart() {
        mPresenter.onStart();
        verify(mInteractor).subscribeToEventBusEvents();
    }

    @Test
    public void testOnStop() {
        mPresenter.onStop();
        verify(mInteractor).unsubsribeFromEventBusEvents();
    }

    @Test
    public void testOnCreate() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).setListener(eq(mPresenter));
        verify(mView).showSkeletonView();
        verify(mInteractor, times(2)).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mBuildDetails).isRunning();
        verify(mInteractor).load(eq(HREF), eq(mPresenter), eq(false));
    }

    @Test
    public void testOnCreateIfBuildIsRunning() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isRunning()).thenReturn(true);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).setListener(eq(mPresenter));
        verify(mView).showSkeletonView();
        verify(mInteractor, times(2)).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mBuildDetails).isRunning();
        verify(mInteractor).load(eq(HREF), eq(mPresenter), eq(true));
    }

    @Test
    public void testOnDestroy() {
        mPresenter.onDestroy();
        verify(mView).unbindViews();
        verify(mInteractor).unsubscribe();
    }

    @Test
    public void testOnDataRefreshEvent() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onDataRefreshEvent();
        verify(mView).showRefreshingProgress();
        verify(mView).hideErrorView();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mInteractor).load(eq(HREF), eq(mPresenter), eq(true));
    }

    @Test
    public void testOnRefresh() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onRefresh();
        verify(mView).hideErrorView();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mInteractor).load(eq(HREF), eq(mPresenter), eq(true));
    }

    @Test
    public void testOnRetry() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onRetry();
        verify(mView).hideErrorView();
        verify(mView).showRefreshingProgress();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mInteractor).load(eq(HREF), eq(mPresenter), eq(true));
    }

    @Test
    public void testOnFail() {
        mPresenter.onFail("error");
        verify(mView).hideCards();
        verify(mView).hideSkeletonView();
        verify(mView).hideRefreshingProgress();
        verify(mView).showErrorView();
    }

    @Test
    public void testOnBranchCardClick() {
        mPresenter.onBranchCardClick("br");
        verify(mView).showBranchCardBottomSheetDialog(eq("br"));
    }

    @Test
    public void testOnShowBuildsActionClick() {
        mPresenter.onNavigateToBuildListEvent("branch");
        verify(mInteractor).postStartBuildListActivityFilteredByBranchEvent(eq("branch"));
        verify(mTracker).trackUserWantsToSeeBuildListFilteredByBranch();
    }

    @Test
    public void testOnCardClick() {
        mPresenter.onCardClick("head", "descr");
        verify(mView).showDefaultCardBottomSheetDialog(eq("head"), eq("descr"));
    }

    @Test
    public void testOnNavigateToBuildListEvent() {
        mPresenter.onNavigateToBuildListEvent();
        verify(mInteractor).postStartBuildListActivityEvent();
        verify(mTracker).trackUserOpensBuildType();
    }

    @Test
    public void testOnNavigateToProjectEvent() {
        mPresenter.onNavigateToProjectEvent();
        verify(mInteractor).postStartProjectActivityEvent();
        verify(mTracker).trackUserOpensProject();
    }

    @Test
    public void testOnBottomSheetDismiss() {
        mPresenter.onBottomSheetDismiss();
        verify(mInteractor).postFABVisibleEvent();
    }

    @Test
    public void testOnBottomSheetShow() {
        mPresenter.onBottomSheetShow();
        verify(mInteractor).postFABGoneEvent();
    }

    @Test
    public void testRestartBuildPromptIsShownIfItIsShown() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isFinished()).thenReturn(true);
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(false);
        when(mOnboardingManager.isRestartBuildPromptShown()).thenReturn(true);
        when(mOnboardingManager.isStopBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isRemoveBuildFromQueuePromptShown()).thenReturn(false);
        mPresenter.onResume();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isFinished();
        verify(mOnboardingManager).isRestartBuildPromptShown();
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).isQueued();
    }

    @Test
    public void testRestartBuildPromptIsShownIfItIsNotShown() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isFinished()).thenReturn(true);
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(false);
        when(mOnboardingManager.isRestartBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isStopBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isRemoveBuildFromQueuePromptShown()).thenReturn(false);
        mPresenter.onResume();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isFinished();
        verify(mOnboardingManager).isRestartBuildPromptShown();
        verify(mView).showRestartBuildPrompt(mOnPromptShownListenerArgumentCaptor.capture());
        OnboardingManager.OnPromptShownListener listener = mOnPromptShownListenerArgumentCaptor.getValue();
        listener.onPromptShown();
        verify(mOnboardingManager).saveRestartBuildPromptShown();
    }

    @Test
    public void testStopBuildPromptIsShownIfItIsShown() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isFinished()).thenReturn(false);
        when(mBuildDetails.isRunning()).thenReturn(true);
        when(mBuildDetails.isQueued()).thenReturn(false);
        when(mOnboardingManager.isRestartBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isStopBuildPromptShown()).thenReturn(true);
        when(mOnboardingManager.isRemoveBuildFromQueuePromptShown()).thenReturn(false);
        mPresenter.onResume();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isFinished();
        verify(mBuildDetails).isRunning();
        verify(mOnboardingManager).isStopBuildPromptShown();
        verify(mBuildDetails).isQueued();
    }

    @Test
    public void testStopBuildPromptIsShownIfItIsNotShown() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isFinished()).thenReturn(false);
        when(mBuildDetails.isRunning()).thenReturn(true);
        when(mBuildDetails.isQueued()).thenReturn(false);
        when(mOnboardingManager.isRestartBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isStopBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isRemoveBuildFromQueuePromptShown()).thenReturn(false);
        mPresenter.onResume();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isFinished();
        verify(mBuildDetails).isRunning();
        verify(mOnboardingManager).isStopBuildPromptShown();
        verify(mView).showStopBuildPrompt(mOnPromptShownListenerArgumentCaptor.capture());
        OnboardingManager.OnPromptShownListener listener = mOnPromptShownListenerArgumentCaptor.getValue();
        listener.onPromptShown();
        verify(mOnboardingManager).saveStopBuildPromptShown();
    }

    @Test
    public void testRemoveBuildPromptIsShownIfItIsShown() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isFinished()).thenReturn(false);
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(true);
        when(mOnboardingManager.isRestartBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isStopBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isRemoveBuildFromQueuePromptShown()).thenReturn(true);
        mPresenter.onResume();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isFinished();
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).isQueued();
        verify(mOnboardingManager).isRemoveBuildFromQueuePromptShown();
    }

    @Test
    public void testRemoveBuildPromptIsShownIfItIsNotShown() {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.isFinished()).thenReturn(false);
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(true);
        when(mOnboardingManager.isRestartBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isStopBuildPromptShown()).thenReturn(false);
        when(mOnboardingManager.isRemoveBuildFromQueuePromptShown()).thenReturn(false);
        mPresenter.onResume();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isFinished();
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).isQueued();
        verify(mOnboardingManager).isRemoveBuildFromQueuePromptShown();
        verify(mView).showRemoveBuildFromQueuePrompt(mOnPromptShownListenerArgumentCaptor.capture());
        OnboardingManager.OnPromptShownListener listener = mOnPromptShownListenerArgumentCaptor.getValue();
        listener.onPromptShown();
        verify(mOnboardingManager).saveRemoveBuildFromQueuePromptShown();
    }
}