/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
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
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker;
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class OverviewPresenterImplTest {

    private static final String HREF = "href";

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

    private OverviewPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new OverviewPresenterImpl(mView, mInteractor, mTracker);
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mView, mInteractor, mTracker, mBuildDetails);
    }

    @Test
    public void testOnCreateOptionsMenuIfBuildIsRunning() throws Exception {
        when(mBuildDetails.isRunning()).thenReturn(true);
        mPresenter.onCreateOptionsMenu(mMenu, mMenuInflater);
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isRunning();
        verify(mView).createStopBuildOptionsMenu(eq(mMenu), eq(mMenuInflater));
    }

    @Test
    public void testOnCreateOptionsMenuIfBuildIsQueued() throws Exception {
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(true);
        mPresenter.onCreateOptionsMenu(mMenu, mMenuInflater);
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).isQueued();
        verify(mView).createRemoveBuildFromQueueOptionsMenu(eq(mMenu), eq(mMenuInflater));
    }

    @Test
    public void testOnCreateOptionsMenuIfBuildIsFinished() throws Exception {
        when(mBuildDetails.isRunning()).thenReturn(false);
        when(mBuildDetails.isQueued()).thenReturn(false);
        mPresenter.onCreateOptionsMenu(mMenu, mMenuInflater);
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).isRunning();
        verify(mBuildDetails).isQueued();
        verify(mView).createDefaultOptionsMenu(eq(mMenu), eq(mMenuInflater));
    }

    @Test
    public void testOnPrepareOptionsMenu() throws Exception {
        mPresenter.onPrepareOptionsMenu(mMenu);
    }

    @Test
    public void testOnOptionsItemSelected() throws Exception {
        when(mView.onOptionsItemSelected(mMenuItem)).thenReturn(true);
        assertThat(mPresenter.onOptionsItemSelected(mMenuItem), is(true));
        verify(mView).onOptionsItemSelected(eq(mMenuItem));
    }

    @Test
    public void testOnCancelBuildContextMenuClick() throws Exception {
        mPresenter.onCancelBuildContextMenuClick();
        verify(mInteractor).postStopBuildEvent();
        verify(mTracker).trackUserClickedCancelBuildOption();
    }

    @Test
    public void testOnShareButtonClick() throws Exception {
        mPresenter.onShareButtonClick();
        verify(mInteractor).postShareBuildInfoEvent();
        verify(mTracker).trackUserSharedBuild();
    }

    @Test
    public void testOnRestartBuildButtonClick() throws Exception {
        mPresenter.onRestartBuildButtonClick();
        verify(mInteractor).postRestartBuildEvent();
        verify(mTracker).trackUserRestartedBuild();
    }

    @Test
    public void testOnStart() throws Exception {
        mPresenter.onStart();
        verify(mInteractor).subscribeToEventBusEvents();
    }

    @Test
    public void testOnStop() throws Exception {
        mPresenter.onStop();
        verify(mInteractor).unsubsribeFromEventBusEvents();
    }

    @Test
    public void testOnCreate() throws Exception {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).setListener(eq(mPresenter));
        verify(mView).showProgressWheel();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mInteractor).load(eq(HREF), eq(mPresenter));
    }

    @Test
    public void testOnDestroy() throws Exception {
        mPresenter.onDestroy();
        verify(mView).unbindViews();
        verify(mInteractor).unsubscribe();
    }

    @Test
    public void testOnDataRefreshEvent() throws Exception {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onDataRefreshEvent();
        verify(mView).showRefreshingProgress();
        verify(mView).hideErrorView();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mInteractor).load(eq(HREF), eq(mPresenter));
    }

    @Test
    public void testOnRefresh() throws Exception {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onRefresh();
        verify(mView).hideErrorView();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mInteractor).load(eq(HREF), eq(mPresenter));
    }

    @Test
    public void testOnRetry() throws Exception {
        when(mInteractor.getBuildDetails()).thenReturn(mBuildDetails);
        when(mBuildDetails.getHref()).thenReturn(HREF);
        mPresenter.onRetry();
        verify(mView).hideErrorView();
        verify(mView).showRefreshingProgress();
        verify(mInteractor).getBuildDetails();
        verify(mBuildDetails).getHref();
        verify(mInteractor).load(eq(HREF), eq(mPresenter));
    }

    @Test
    public void testOnFail() throws Exception {
        mPresenter.onFail("error");
        verify(mView).hideCards();
        verify(mView).hideProgressWheel();
        verify(mView).hideRefreshingProgress();
        verify(mView).showErrorView(eq("error"));
    }

    @Test
    public void testOnBranchCardClick() throws Exception {
        mPresenter.onBranchCardClick("br");
        verify(mView).showBranchCardBottomSheetDialog(eq("br"));
    }

    @Test
    public void testOnShowBuildsActionClick() throws Exception {
        mPresenter.onShowBuildsActionClick("branch");
        verify(mInteractor).postStartBuildListActivityFilteredByBranchEvent(eq("branch"));
        verify(mTracker).trackUserWantsToSeeBuildListFilteredByBranch();
    }

    @Test
    public void testOnCopyActionClick() throws Exception {
        mPresenter.onCopyActionClick("br");
        verify(mInteractor).copyTextToClipBoard(eq("br"));
    }

    @Test
    public void testOnCardClick() throws Exception {
        mPresenter.onCardClick("head", "descr");
        verify(mView).showDefaultCardBottomSheetDialog(eq("head"), eq("descr"));
    }

    @Test
    public void testOnBottomSheetDismiss() throws Exception {
        mPresenter.onBottomSheetDismiss();
        verify(mInteractor).postFABVisibleEvent();
    }

    @Test
    public void testOnBottomSheetShow() throws Exception {
        mPresenter.onBottomSheetShow();
        verify(mInteractor).postFABGoneEvent();
    }

}