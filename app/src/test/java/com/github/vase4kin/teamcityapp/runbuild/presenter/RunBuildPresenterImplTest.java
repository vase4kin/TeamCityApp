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

package com.github.vase4kin.teamcityapp.runbuild.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter;
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker;
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RunBuildPresenterImplTest {

    @Captor
    private ArgumentCaptor<LoadingListenerWithForbiddenSupport<String>> mQueueLoadingListenerCaptor;
    @Captor
    private ArgumentCaptor<OnLoadingListener<List<String>>> mBranchLoadingListenerCaptor;
    @Mock
    private RunBuildView mView;
    @Mock
    private RunBuildInteractor mInteractor;
    @Mock
    private RunBuildRouter mRouter;
    @Mock
    private RunBuildTracker mTracker;

    private RunBuildPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPresenter = new RunBuildPresenterImpl(mView, mInteractor, mRouter, mTracker);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mView, mInteractor, mRouter, mTracker);
    }

    @Test
    public void testOnCreate() throws Exception {
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mInteractor).loadBranches(mBranchLoadingListenerCaptor.capture());
        OnLoadingListener<List<String>> loadingListener = mBranchLoadingListenerCaptor.getValue();
        List<String> singleBranchList = Collections.singletonList("Branch");
        loadingListener.onSuccess(singleBranchList);
        verify(mView).hideBranchesLoadingProgress();
        verify(mView).setupAutoCompleteForSingleBranch(eq(singleBranchList));
        verify(mView).showBranchesAutoComplete();
        List<String> multipleBranchList = new ArrayList<>();
        multipleBranchList.add("branch1");
        multipleBranchList.add("branch2");
        loadingListener.onSuccess(multipleBranchList);
        verify(mView, times(2)).hideBranchesLoadingProgress();
        verify(mView).setupAutoComplete(eq(multipleBranchList));
        verify(mView, times(2)).showBranchesAutoComplete();
        loadingListener.onFail("");
        verify(mView, times(3)).hideBranchesLoadingProgress();
        verify(mView).showNoBranchesAvailable();
    }

    @Test
    public void testOnDestroy() throws Exception {
        mPresenter.onDestroy();
        verify(mInteractor).unsubscribe();
        verify(mView).unbindViews();
    }

    @Test
    public void testOnBuildQueue() throws Exception {
        mPresenter.onBuildQueue("branch");
        verify(mView).showQueuingBuildProgress();
        verify(mInteractor).queueBuild(eq("branch"), mQueueLoadingListenerCaptor.capture());
        LoadingListenerWithForbiddenSupport<String> loadingListener = mQueueLoadingListenerCaptor.getValue();
        loadingListener.onSuccess("href");
        verify(mView).hideQueuingBuildProgress();
        verify(mRouter).closeOnSuccess(eq("href"));
        verify(mTracker).trackUserRunBuildSuccess();
        loadingListener.onFail("");
        verify(mView, times(2)).hideQueuingBuildProgress();
        verify(mView).showErrorSnackbar();
        verify(mTracker).trackUserRunBuildFailed();
        loadingListener.onForbiddenError();
        verify(mView, times(3)).hideQueuingBuildProgress();
        verify(mView).showForbiddenErrorSnackbar();
        verify(mTracker).trackUserRunBuildFailedForbidden();
    }

    @Test
    public void testOnClick() throws Exception {
        mPresenter.onClick();
        verify(mRouter).closeOnCancel();
    }

    @Test
    public void testOnBackPressed() throws Exception {
        mPresenter.onBackPressed();
        verify(mRouter).closeOnBackButtonPressed();
    }

    @Test
    public void testOnResume() throws Exception {
        mPresenter.onResume();
        verify(mTracker).trackView();
    }

}