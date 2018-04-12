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

package com.github.vase4kin.teamcityapp.drawer.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModelImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

public class DrawerPresenterImplTest {

    @Captor
    private ArgumentCaptor<OnLoadingListener<Integer>> mIntegerLoadingListenerCaptor;

    @Captor
    private ArgumentCaptor<OnLoadingListener<List<UserAccount>>> mArgumentCaptor;

    @Mock
    private DrawerDataManager mDataManager;

    @Mock
    private DrawerView mView;

    @Mock
    private DrawerRouter mRouter;

    @Mock
    private DrawerTracker mTracker;

    private DrawerPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new DrawerPresenterImpl<>(mView, mDataManager, mRouter, mTracker);
    }

    @Test
    public void testHandleOnCreateIfModelIsEmpty() throws Exception {
        when(mView.isModelEmpty()).thenReturn(true);
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mDataManager).load(mArgumentCaptor.capture());
        OnLoadingListener<List<UserAccount>> loadingListener = mArgumentCaptor.getValue();
        loadingListener.onSuccess(Collections.<UserAccount>emptyList());
        verify(mView).showData(any(DrawerDataModelImpl.class));
        loadingListener.onFail("error");
        verify(mView).isModelEmpty();
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testHandleOnCreateIfModelIsNotEmpty() throws Exception {
        when(mView.isModelEmpty()).thenReturn(false);
        when(mDataManager.getFavoritesCount()).thenReturn(5);
        mPresenter.onCreate();
        verify(mView).initViews(eq(mPresenter));
        verify(mDataManager).load(mArgumentCaptor.capture());
        OnLoadingListener<List<UserAccount>> loadingListener = mArgumentCaptor.getValue();
        loadingListener.onSuccess(Collections.<UserAccount>emptyList());
        verify(mView).showData(any(DrawerDataModelImpl.class));
        loadingListener.onFail("error");
        verify(mView).isModelEmpty();
        verify(mDataManager).loadRunningBuildsCount(mIntegerLoadingListenerCaptor.capture());
        OnLoadingListener<Integer> runningListener = mIntegerLoadingListenerCaptor.getAllValues().get(0);
        runningListener.onSuccess(1);
        verify(mView).updateRunningBuildsBadge(eq(1));
        runningListener.onFail("error");
        verify(mDataManager).loadBuildQueueCount(mIntegerLoadingListenerCaptor.capture());
        OnLoadingListener<Integer> queuedListener = mIntegerLoadingListenerCaptor.getAllValues().get(1);
        queuedListener.onSuccess(34);
        verify(mView).updateBuildQueueBadge(eq(34));
        queuedListener.onFail("error");
        verify(mDataManager).loadConnectedAgentsCount(mIntegerLoadingListenerCaptor.capture());
        OnLoadingListener<Integer> agentsListener = mIntegerLoadingListenerCaptor.getAllValues().get(2);
        agentsListener.onSuccess(67);
        verify(mView).updateAgentsBadge(eq(67));
        agentsListener.onFail("error");
        verify(mDataManager).getFavoritesCount();
        verify(mView).updateFavoritesBadge(eq(5));
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testHandleOnBackButtonPressed() throws Exception {
        mPresenter.onBackButtonPressed();
        verify(mView).backButtonPressed();
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testSetActiveUser() throws Exception {
        mPresenter.setActiveUser("email", "userName");
        verify(mDataManager).setActiveUser(eq("email"), eq("userName"));
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testIsActiveProfile() throws Exception {
        when(mDataManager.isActiveUser("email", "userName")).thenReturn(true);
        assertThat(mPresenter.isActiveProfile("email", "userName"), is(equalTo(true)));
        verify(mDataManager).isActiveUser(eq("email"), eq("userName"));
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOnDrawerSlide() throws Exception {
        when(mDataManager.getFavoritesCount()).thenReturn(5);
        mPresenter.onDrawerSlide();
        verify(mDataManager).loadConnectedAgentsCount(Mockito.<OnLoadingListener>any());
        verify(mDataManager).loadBuildQueueCount(Mockito.<OnLoadingListener>any());
        verify(mDataManager).loadRunningBuildsCount(Mockito.<OnLoadingListener>any());
        verify(mDataManager).getFavoritesCount();
        verify(mView).updateFavoritesBadge(eq(5));
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testStartRootProjectsActivity() throws Exception {
        mPresenter.startRootProjectsActivity();
        verify(mRouter).startRootProjectsActivity();
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testStartAccountListActivity() throws Exception {
        mPresenter.startAccountListActivity();
        verify(mRouter).startAccountListActivity();
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testStartAgentActivity() throws Exception {
        mPresenter.startAgentActivity();
        verify(mRouter).startAgentActivity();
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testStartBuildRunningActivity() throws Exception {
        mPresenter.startBuildRunningActivity();
        verify(mRouter).startBuildRunningActivity();
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }

    @Test
    public void testStartQueuedBuildsActivity() throws Exception {
        mPresenter.startQueuedBuildsActivity();
        verify(mRouter).startQueuedBuildsActivity();
        verifyNoMoreInteractions(mView, mDataManager, mRouter);
    }
}