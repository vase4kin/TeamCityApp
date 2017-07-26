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

package com.github.vase4kin.teamcityapp.buildlist.presenter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModelImpl;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BuildListPresenterImplTest {

    @Captor
    private ArgumentCaptor<OnBuildListPresenterListener> mOnBuildListPresenterListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<OnLoadingListener<List<BuildDetails>>> mOnLoadingListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<OnLoadingListener<Build>> mBuildArgumentCaptor;

    @Captor
    private ArgumentCaptor<OnboardingManager.OnPromptShownListener> mOnPromptShownListenerArgumentCaptor;

    @Mock
    private BuildListFilter mFilter;

    @Mock
    private MenuItem mMenuItem;

    @Mock
    private Menu mMenu;

    @Mock
    private MenuInflater mMenuInflater;

    @Mock
    private Build mBuild;

    @Mock
    private OnLoadingListener<List<BuildDetails>> mLoadingListener;

    @Mock
    private Bundle mBundle;

    @Mock
    private BuildListView mView;

    @Mock
    private BuildListDataManager mDataManager;

    @Mock
    private BuildListTracker mTracker;

    @Mock
    private BuildListRouter mRouter;

    @Mock
    private BaseValueExtractor mValueExtractor;

    @Mock
    private BuildInteractor mInteractor;

    @Mock
    private OnboardingManager mOnboardingManager;

    private BuildListPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new BuildListPresenterImpl<>(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor, mOnboardingManager);
    }

    @Test
    public void testLoadData() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        mPresenter.loadData(mLoadingListener, false);
        verify(mValueExtractor).getId();
        verify(mValueExtractor).getBuildListFilter();
        verify(mDataManager).load(eq("id"), eq(mLoadingListener), eq(false));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mRouter, mValueExtractor, mInteractor, mOnboardingManager);
    }

    @Test
    public void testLoadDataIfBuildListFilterIsProvided() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        when(mValueExtractor.getBuildListFilter()).thenReturn(mFilter);
        mPresenter.loadData(mLoadingListener, false);
        verify(mValueExtractor).getId();
        verify(mValueExtractor).getBuildListFilter();
        verify(mDataManager).load(eq("id"), eq(mFilter), eq(mLoadingListener), eq(false));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mRouter, mValueExtractor, mInteractor, mOnboardingManager);
    }

    @Test
    public void testInitViews() throws Exception {
        when(mValueExtractor.getName()).thenReturn("name");
        mPresenter.initViews();
        verify(mView).setTitle(eq("name"));
    }

    @Test
    public void testOnBuildClick() throws Exception {
        when(mValueExtractor.getName()).thenReturn("name");
        mPresenter.onBuildClick(mBuild);
        verify(mValueExtractor).getName();
        verify(mRouter).openBuildPage(eq(mBuild), eq("name"));
    }

    @Test
    public void testOnBuildClickIfBundleIsNull() throws Exception {
        when(mValueExtractor.isBundleNull()).thenReturn(true);
        mPresenter.onBuildClick(mBuild);
        verify(mValueExtractor).isBundleNull();
        verify(mRouter).openBuildPage(mBuild, null);
    }

    @Test
    public void testOnRunBuildFabClick() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        mPresenter.onRunBuildFabClick();
        verify(mRouter).openRunBuildPage(eq("id"));
        verify(mTracker).trackRunBuildButtonPressed();
    }

    @Test
    public void testOnShowQueuedBuildSnackBarClick() throws Exception {
        when(mValueExtractor.getName()).thenReturn("name");
        mPresenter.mQueuedBuildHref = "href";
        mPresenter.onShowQueuedBuildSnackBarClick();
        verify(mTracker).trackUserWantsToSeeQueuedBuildDetails();
        verify(mView).showBuildLoadingProgress();
        verify(mInteractor).loadBuild(eq("href"), mBuildArgumentCaptor.capture());
        OnLoadingListener<Build> listener = mBuildArgumentCaptor.getValue();
        listener.onSuccess(mBuild);
        verify(mView).hideBuildLoadingProgress();
        verify(mValueExtractor).getName();
        verify(mRouter).openBuildPage(eq(mBuild), eq("name"));
        listener.onFail("");
        verify(mView, times(2)).hideBuildLoadingProgress();
        verify(mView).showOpeningBuildErrorSnackBar();
    }

    @Test
    public void testOnLoadMore() throws Exception {
        mPresenter.onLoadMore();
        assertThat(mPresenter.mIsLoadMoreLoading, is(true));
        verify(mView).addLoadMore();
        verify(mDataManager).loadMore(mOnLoadingListenerArgumentCaptor.capture());
        OnLoadingListener<List<BuildDetails>> loadingListener = mOnLoadingListenerArgumentCaptor.getValue();

        loadingListener.onSuccess(Collections.<BuildDetails>emptyList());
        verify(mView).removeLoadMore();
        verify(mView).addMoreBuilds(any(BuildListDataModelImpl.class));
        assertThat(mPresenter.mIsLoadMoreLoading, is(false));

        loadingListener.onFail("error");
        verify(mView, times(2)).removeLoadMore();
        verify(mView).showRetryLoadMoreSnackBar();
        assertThat(mPresenter.mIsLoadMoreLoading, is(false));
    }

    @Test
    public void testHasLoadedAllItems() throws Exception {
        when(mDataManager.canLoadMore()).thenReturn(false);
        assertThat(mPresenter.hasLoadedAllItems(), is(true));
    }

    @Test
    public void testOnActivityResultIfResultIsOk() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        mPresenter.onRunBuildActivityResult("href");
        assertThat(mPresenter.mQueuedBuildHref, is(equalTo("href")));
        verify(mView).showBuildQueuedSuccessSnackBar();
        verify(mView).showRefreshAnimation();
        verify(mView).hideErrorView();
        verify(mView).hideEmpty();
        verify(mValueExtractor).getId();
        verify(mValueExtractor).getBuildListFilter();
        verify(mDataManager).load(eq("id"), Mockito.any(OnLoadingListener.class), eq(true));
        verify(mView).hideFiltersAppliedSnackBar();
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor);
    }

    @Test
    public void testOnFilterBuildsResultIfResultIsOk() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        mPresenter.onFilterBuildsActivityResult(mFilter);
        verify(mView).showBuildFilterAppliedSnackBar();
        verify(mView).disableSwipeToRefresh();
        verify(mView).showProgressWheel();
        verify(mView).hideErrorView();
        verify(mView).hideEmpty();
        verify(mView).showData(any(BuildListDataModel.class));
        verify(mValueExtractor).getId();
        verify(mDataManager).load(eq("id"), eq(mFilter), Mockito.any(OnLoadingListener.class), eq(true));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor);
    }

    @Test
    public void testOnResetFiltersSnackBarActionClick() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        mPresenter.onResetFiltersSnackBarActionClick();
        verify(mView).disableSwipeToRefresh();
        verify(mView).showProgressWheel();
        verify(mView).hideErrorView();
        verify(mView).hideEmpty();
        verify(mView).showData(any(BuildListDataModel.class));
        verify(mValueExtractor).getId();
        verify(mDataManager).load(eq("id"), Mockito.any(OnLoadingListener.class), eq(true));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor);
    }

    @Test
    public void testIsLoading() throws Exception {
        mPresenter.mIsLoadMoreLoading = true;
        assertThat(mPresenter.isLoading(), is(equalTo(true)));
    }

    @Test
    public void testCreateModel() throws Exception {
        BuildListDataModel dataModel = mPresenter.createModel(Collections.<Build>emptyList());
        assertThat(dataModel.getItemCount(), is(0));
    }

    @Test
    public void testOnSuccessCallBack() throws Exception {
        mPresenter.onSuccessCallBack(Collections.<Build>emptyList());
        verify(mView).showRunBuildFloatActionButton();
    }

    @Test
    public void testOnFailCallBack() throws Exception {
        mPresenter.onFailCallBack("");
        verify(mView).hideRunBuildFloatActionButton();
    }

    @Test
    public void testOnFilterBuildsOptionMenuClick() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        mPresenter.onFilterBuildsOptionMenuClick();
        verify(mValueExtractor).getId();
        verify(mRouter).openFilterBuildsPage(eq("id"));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor, mOnboardingManager);
    }

    @Test
    public void testOnCreateOptions() throws Exception {
        mPresenter.onCreateOptionsMenu(mMenu, mMenuInflater);
        verify(mView).createOptionsMenu(eq(mMenu), eq(mMenuInflater));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor, mOnboardingManager);
    }

    @Test
    public void testOnPrepareOptionsMenu() throws Exception {
        mPresenter.onPrepareOptionsMenu(mMenu);
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor, mOnboardingManager);
    }

    @Test
    public void testOnOptionsItemSelected() throws Exception {
        when(mView.onOptionsItemSelected(mMenuItem)).thenReturn(true);
        assertThat(mPresenter.onOptionsItemSelected(mMenuItem), is(true));
        verify(mView).onOptionsItemSelected(eq(mMenuItem));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter, mInteractor, mOnboardingManager);
    }

    @Test
    public void testRunBuildPromptIfItIsNotBuildListActivity() throws Exception {
        when(mView.isBuildListOpen()).thenReturn(false);
        mPresenter.onResume();
        verify(mView).isBuildListOpen();
    }

    @Test
    public void testRunBuildPromptIfItIsShownAlready() throws Exception {
        when(mView.isBuildListOpen()).thenReturn(true);
        when(mOnboardingManager.isRunBuildPromptShown()).thenReturn(true);
        mPresenter.onResume();
        verify(mView).isBuildListOpen();
        verify(mOnboardingManager).isRunBuildPromptShown();
    }

    @Test
    public void testRunBuildPromptIfItIsNotShownAlready() throws Exception {
        when(mView.isBuildListOpen()).thenReturn(true);
        when(mOnboardingManager.isRunBuildPromptShown()).thenReturn(false);
        mPresenter.onResume();
        verify(mView).isBuildListOpen();
        verify(mOnboardingManager).isRunBuildPromptShown();
        verify(mView).showRunBuildPrompt(mOnPromptShownListenerArgumentCaptor.capture());
        OnboardingManager.OnPromptShownListener runBuildPromptListener = mOnPromptShownListenerArgumentCaptor.getValue();
        when(mOnboardingManager.isFilterBuildsPromptShown()).thenReturn(true);
        runBuildPromptListener.onPromptShown();
        verify(mOnboardingManager).saveRunBuildPromptShown();
        verify(mOnboardingManager).isFilterBuildsPromptShown();
        when(mOnboardingManager.isFilterBuildsPromptShown()).thenReturn(false);
        runBuildPromptListener.onPromptShown();
        verify(mOnboardingManager, times(2)).saveRunBuildPromptShown();
        verify(mOnboardingManager, times(2)).isFilterBuildsPromptShown();
        verify(mView).showFilterBuildsPrompt(mOnPromptShownListenerArgumentCaptor.capture());
        OnboardingManager.OnPromptShownListener filterBuildsPromptListener = mOnPromptShownListenerArgumentCaptor.getValue();
        filterBuildsPromptListener.onPromptShown();
        verify(mOnboardingManager).saveFilterBuildsPromptShown();
    }

    @Test
    public void testOnSwipeToRefresh() throws Exception {
        mPresenter.onSwipeToRefresh();
        verify(mView).hideFiltersAppliedSnackBar();
    }
}