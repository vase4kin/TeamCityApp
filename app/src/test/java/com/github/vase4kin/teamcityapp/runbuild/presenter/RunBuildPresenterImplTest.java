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
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter;
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker;
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RunBuildPresenterImpl}
 */
public class RunBuildPresenterImplTest {

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_VALUE = "value";

    @Captor
    private ArgumentCaptor<Properties> mPropertiesArgumentCaptor;
    @Captor
    private ArgumentCaptor<LoadingListenerWithForbiddenSupport<String>> mQueueLoadingListenerCaptor;
    @Captor
    private ArgumentCaptor<OnLoadingListener<List<String>>> mBranchLoadingListenerCaptor;
    @Captor
    private ArgumentCaptor<OnLoadingListener<List<Agent>>> mAgentsLoadingListenerCaptor;
    @Mock
    private Agent mAgent;
    @Mock
    private RunBuildView mView;
    @Mock
    private RunBuildInteractor mInteractor;
    @Mock
    private RunBuildRouter mRouter;
    @Mock
    private RunBuildTracker mTracker;
    @Mock
    private BranchesComponentView mBranchesComponentView;
    @Mock
    private BranchesInteractor mBranchesInteractor;

    private RunBuildPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPresenter = new RunBuildPresenterImpl(mView, mInteractor, mRouter, mTracker, mBranchesComponentView, mBranchesInteractor);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mView, mInteractor, mRouter, mTracker);
    }

    @Test
    public void testOnCreate() throws Exception {
        when(mAgent.getName()).thenReturn("agentName");
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mBranchesInteractor).loadBranches(mBranchLoadingListenerCaptor.capture());
        OnLoadingListener<List<String>> loadingListener = mBranchLoadingListenerCaptor.getValue();
        List<String> singleBranchList = Collections.singletonList("Branch");
        loadingListener.onSuccess(singleBranchList);
        verify(mBranchesComponentView).hideBranchesLoadingProgress();
        verify(mBranchesComponentView).setupAutoCompleteForSingleBranch(eq(singleBranchList));
        verify(mBranchesComponentView).showBranchesAutoComplete();
        List<String> multipleBranchList = new ArrayList<>();
        multipleBranchList.add("branch1");
        multipleBranchList.add("branch2");
        loadingListener.onSuccess(multipleBranchList);
        verify(mBranchesComponentView, times(2)).hideBranchesLoadingProgress();
        verify(mBranchesComponentView).setupAutoComplete(eq(multipleBranchList));
        verify(mBranchesComponentView, times(2)).showBranchesAutoComplete();
        loadingListener.onFail("");
        verify(mBranchesComponentView, times(3)).hideBranchesLoadingProgress();
        verify(mBranchesComponentView).showNoBranchesAvailable();
        verify(mView).disableAgentSelectionControl();
        verify(mInteractor).loadAgents(mAgentsLoadingListenerCaptor.capture());
        OnLoadingListener<List<Agent>> agentListOnLoadingListener = mAgentsLoadingListenerCaptor.getValue();
        agentListOnLoadingListener.onFail("fail");
        assertThat(mPresenter.getMAgents().isEmpty(), is(equalTo(true)));
        verify(mView).hideLoadingAgentsProgress();
        verify(mView).showNoAgentsAvailable();
        agentListOnLoadingListener.onSuccess(Collections.singletonList(mAgent));
        assertThat(mPresenter.getMAgents().size(), is(equalTo(1)));
        assertThat(mPresenter.getMAgents().get(0), is(equalTo(mAgent)));
        verify(mView, times(2)).hideLoadingAgentsProgress();
        verify(mView).showSelectedAgentView();
        verify(mView).enableAgentSelectionControl();
        verify(mView).setAgentListDialogWithAgentsList(Collections.singletonList("agentName"));
        agentListOnLoadingListener.onSuccess(Collections.<Agent>emptyList());
        assertThat(mPresenter.getMAgents().isEmpty(), is(equalTo(true)));
        verify(mView, times(3)).hideLoadingAgentsProgress();
        verify(mView, times(2)).showNoAgentsAvailable();
    }

    @Test
    public void testOnDestroy() throws Exception {
        mPresenter.onDestroy();
        verify(mInteractor).unsubscribe();
        verify(mBranchesInteractor).unsubscribe();
        verify(mView).unbindViews();
    }

    @Test
    public void testOnBuildQueue() throws Exception {
        mPresenter.setMSelectedAgent(mAgent);
        mPresenter.getMProperties().add(new Properties.Property(PROPERTY_NAME, PROPERTY_VALUE));
        when(mBranchesComponentView.getBranchName()).thenReturn("branch");
        mPresenter.onBuildQueue(true, true, true);
        verify(mBranchesComponentView).getBranchName();
        verify(mView).showQueuingBuildProgress();
        verify(mInteractor).queueBuild(eq("branch"), eq(mAgent), eq(true), eq(true), eq(true), mPropertiesArgumentCaptor.capture(), mQueueLoadingListenerCaptor.capture());
        Properties capturedProperties = mPropertiesArgumentCaptor.getValue();
        assertThat(capturedProperties.getObjects().size(), is(equalTo(1)));
        Properties.Property capturedProperty = capturedProperties.getObjects().get(0);
        assertThat(capturedProperty.getName(), is(equalTo(PROPERTY_NAME)));
        assertThat(capturedProperty.getValue(), is(equalTo(PROPERTY_VALUE)));
        LoadingListenerWithForbiddenSupport<String> loadingListener = mQueueLoadingListenerCaptor.getValue();
        loadingListener.onSuccess("href");
        verify(mView).hideQueuingBuildProgress();
        verify(mRouter).closeOnSuccess(eq("href"));
        verify(mTracker).trackUserRunBuildWithCustomParamsSuccess();
        mPresenter.getMProperties().clear();
        loadingListener.onSuccess("href");
        verify(mView, times(2)).hideQueuingBuildProgress();
        verify(mRouter, times(2)).closeOnSuccess(eq("href"));
        verify(mTracker).trackUserRunBuildSuccess();
        loadingListener.onFail("");
        verify(mView, times(3)).hideQueuingBuildProgress();
        verify(mView).showErrorSnackbar();
        verify(mTracker).trackUserRunBuildFailed();
        loadingListener.onForbiddenError();
        verify(mView, times(4)).hideQueuingBuildProgress();
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

    @Test
    public void testOnAgentSelectedIfAgentsAreEmpty() throws Exception {
        mPresenter.setMAgents(Collections.emptyList());
        mPresenter.onAgentSelected(0);
    }

    @Test
    public void testOnAgentSelectedIfAgentsAreNotEmpty() throws Exception {
        mPresenter.setMAgents(Collections.singletonList(mAgent));
        mPresenter.onAgentSelected(0);
        assertThat(mPresenter.getMSelectedAgent(), is(equalTo(mAgent)));
    }

    @Test
    public void testOnAddParameterButtonClick() throws Exception {
        mPresenter.onAddParameterButtonClick();
        verify(mView).showAddParameterDialog();
        verify(mTracker).trackUserClicksOnAddNewBuildParamButton();
    }

    @Test
    public void testOnClearAllParametersButtonClick() throws Exception {
        mPresenter.onClearAllParametersButtonClick();
        assertThat(mPresenter.getMProperties().size(), is(equalTo(0)));
        verify(mView).disableClearAllParametersButton();
        verify(mView).showNoneParametersView();
        verify(mView).removeAllParameterViews();
        verify(mTracker).trackUserClicksOnClearAllBuildParamsButton();
    }

    @Test
    public void testOnParameterAdded() throws Exception {
        mPresenter.onParameterAdded(PROPERTY_NAME, PROPERTY_VALUE);
        assertThat(mPresenter.getMProperties().size(), is(equalTo(1)));
        assertThat(mPresenter.getMProperties().get(0).getName(), is(equalTo(PROPERTY_NAME)));
        assertThat(mPresenter.getMProperties().get(0).getValue(), is(equalTo(PROPERTY_VALUE)));
        verify(mView).hideNoneParametersView();
        verify(mView).enableClearAllParametersButton();
        verify(mView).addParameterView(eq(PROPERTY_NAME), eq(PROPERTY_VALUE));
        verify(mTracker).trackUserAddsBuildParam();
    }

}