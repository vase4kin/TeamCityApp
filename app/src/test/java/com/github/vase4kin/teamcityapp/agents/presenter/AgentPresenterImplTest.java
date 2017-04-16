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

package com.github.vase4kin.teamcityapp.agents.presenter;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel;
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager;
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class AgentPresenterImplTest {

    @Captor
    private ArgumentCaptor<OnTextTabChangeEvent> mArgumentCaptor;

    @Mock
    private List<Agent> mData;

    @Mock
    private Agent mAgent;

    @Mock
    private OnLoadingListener<List<Agent>> mLoadingListener;

    @Mock
    private Bundle mBundle;

    @Mock
    private BaseListView mView;

    @Mock
    private AgentsDataManager mDataManager;

    @Mock
    private AgentsValueExtractor mValueExtractor;

    @Mock
    private ViewTracker mTracker;

    private AgentPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new AgentPresenterImpl(mView, mDataManager, mTracker, mValueExtractor);
    }

    @Test
    public void testLoadData() throws Exception {
        when(mValueExtractor.includeDisconnected()).thenReturn(true);
        mPresenter.loadData(mLoadingListener, false);
        verify(mValueExtractor).includeDisconnected();
        verify(mDataManager).load(eq(true), eq(mLoadingListener), eq(false));
        verifyNoMoreInteractions(mView, mDataManager, mBundle, mTracker, mValueExtractor);
    }

    @Test
    public void testAddingNumberOfAgentsOnSuccessCallBackIfAgentsTypeIsDisconnected() throws Exception {
        when(mData.size()).thenReturn(34);
        when(mValueExtractor.includeDisconnected()).thenReturn(true);

        mPresenter.onSuccessCallBack(mData);
        verify(mValueExtractor).includeDisconnected();
        verify(mDataManager).postUpdateTabTitleEvent(eq(34), eq(1));
    }

    @Test
    public void testAddingNumberOfAgentsOnSuccessCallBackIfAgentsTypeIsConnected() throws Exception {
        when(mData.size()).thenReturn(34);
        when(mValueExtractor.includeDisconnected()).thenReturn(false);

        mPresenter.onSuccessCallBack(mData);
        verify(mValueExtractor).includeDisconnected();
        verify(mValueExtractor).includeDisconnected();
        verify(mDataManager).postUpdateTabTitleEvent(eq(34), eq(0));
    }

    @Test
    public void testCreateModel() throws Exception {
        when(mAgent.getName()).thenReturn("name");
        List<Agent> agents = new ArrayList<>();
        agents.add(mAgent);
        AgentDataModel dataModel = mPresenter.createModel(agents);
        assertThat(dataModel.getName(0), is("name"));
        assertThat(dataModel.getItemCount(), is(1));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor);
    }
}