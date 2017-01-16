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

package com.github.vase4kin.teamcityapp.filter_builds.presenter;

import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView;
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor;
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class FilterBuildsPresenterImplTest {

    @Captor
    private ArgumentCaptor<OnLoadingListener<List<String>>> mLoadingListenerCaptor;
    @Captor
    private ArgumentCaptor<BuildListFilter> mBuildListFilterCaptor;
    @Mock
    private FilterBuildsView mView;
    @Mock
    private FilterBuildsRouter mRouter;
    @Mock
    private BranchesInteractor mBranchesInteractor;
    @Mock
    private BranchesComponentView mBranchesComponentView;

    private FilterBuildsPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(anyString())).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence charSequence = (CharSequence) invocation.getArguments()[0];
                return charSequence == null || charSequence.length() == 0;
            }
        });
        mPresenter = new FilterBuildsPresenterImpl(mView, mRouter, mBranchesInteractor, mBranchesComponentView);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mView, mRouter, mBranchesInteractor, mBranchesComponentView);
    }

    @Test
    public void onCreate() throws Exception {
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mBranchesComponentView).initViews();
        verify(mBranchesInteractor).loadBranches(mLoadingListenerCaptor.capture());
        OnLoadingListener<List<String>> loadingListener = mLoadingListenerCaptor.getValue();
        loadingListener.onSuccess(Collections.<String>emptyList());
        verify(mBranchesComponentView).hideBranchesLoadingProgress();
        verify(mBranchesComponentView).showNoBranchesAvailableToFilter();
        loadingListener.onSuccess(Collections.singletonList("branch"));
        verify(mBranchesComponentView, times(2)).hideBranchesLoadingProgress();
        verify(mBranchesComponentView, times(2)).showNoBranchesAvailableToFilter();
        List<String> branches = new ArrayList<>();
        branches.add("1");
        branches.add("2");
        loadingListener.onSuccess(branches);
        verify(mBranchesComponentView, times(3)).hideBranchesLoadingProgress();
        verify(mBranchesComponentView).setupAutoComplete(branches);
        verify(mBranchesComponentView).setAutocompleteHintForFilter();
        verify(mBranchesComponentView).showBranchesAutoComplete();
        loadingListener.onFail("");
        verify(mBranchesComponentView, times(4)).hideBranchesLoadingProgress();
        verify(mBranchesComponentView).showNoBranchesAvailable();
    }

    @Test
    public void onResume() throws Exception {
        //test track view
    }

    @Test
    public void onDestroy() throws Exception {
        mPresenter.onDestroy();
        verify(mView).unbindViews();
        verify(mBranchesComponentView).unbindViews();
        verify(mBranchesInteractor).unsubscribe();
    }

    @Test
    public void onBackPressed() throws Exception {
        mPresenter.onBackPressed();
        verify(mRouter).closeOnBackButtonPressed();
    }

    @Test
    public void onClick() throws Exception {
        mPresenter.onClick();
        verify(mRouter).closeOnCancel();
    }

    @Test
    public void onFilterFabClick() throws Exception {
        when(mBranchesComponentView.getBranchName()).thenReturn("branch");
        mPresenter.onFilterFabClick(FilterBuildsView.FILTER_CANCELLED, true, true);
        verify(mBranchesComponentView).getBranchName();
        verify(mRouter).closeOnSuccess(mBuildListFilterCaptor.capture());
        BuildListFilter capturedFilter = mBuildListFilterCaptor.getValue();
        assertThat(capturedFilter.toLocator(), is(equalTo("canceled:true,branch:name:branch,personal:true,pinned:true,count:10")));
    }

    @Test
    public void onQueuedFilterSelected() throws Exception {
        mPresenter.onQueuedFilterSelected();
        verify(mView).hideSwitchForPinnedFilter();
    }

    @Test
    public void onOtherFiltersSelected() throws Exception {
        mPresenter.onOtherFiltersSelected();
        verify(mView).showSwitchForPinnedFilter();
    }

}