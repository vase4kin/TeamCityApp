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

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModelImpl;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
    private ArgumentCaptor<OnLoadingListener<List<Build>>> mOnLoadingListenerArgumentCaptor;

    @Mock
    private Build mBuild;

    @Mock
    private OnLoadingListener<List<Build>> mLoadingListener;

    @Mock
    private Bundle mBundle;

    @Mock
    private BuildListView mView;

    @Mock
    private BuildListDataManager mDataManager;

    @Mock
    private ViewTracker mTracker;

    @Mock
    private BuildListRouter mRouter;

    @Mock
    private BaseValueExtractor mValueExtractor;

    private BuildListPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new BuildListPresenterImpl<>(mView, mDataManager, mTracker, mValueExtractor, mRouter);
    }

    @Test
    public void testLoadData() throws Exception {
        when(mValueExtractor.getId()).thenReturn("id");
        mPresenter.loadData(mLoadingListener);
        verify(mValueExtractor).getId();
        verify(mDataManager).load(eq("id"), eq(mLoadingListener));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mRouter, mValueExtractor);
    }

    @Test
    public void testInitViews() throws Exception {
        when(mValueExtractor.getName()).thenReturn("name");
        mPresenter.initViews();
        verify(mView).setTitle(eq("name"));

        verify(mView).setOnBuildListPresenterListener(mOnBuildListPresenterListenerArgumentCaptor.capture());
        OnBuildListPresenterListener onBuildListPresenterListener = mOnBuildListPresenterListenerArgumentCaptor.getValue();
        onBuildListPresenterListener.onBuildClick(mBuild);
        verify(mRouter).openBuildPage(eq(mBuild));

        when(mDataManager.canLoadMore()).thenReturn(false);
        assertThat(onBuildListPresenterListener.hasLoadedAllItems(), is(true));

        onBuildListPresenterListener.onLoadMore();
        assertThat(mPresenter.mIsLoadMoreLoading, is(true));
        verify(mView).addLoadMore();
        verify(mDataManager).loadMore(mOnLoadingListenerArgumentCaptor.capture());
        OnLoadingListener<List<Build>> loadingListener = mOnLoadingListenerArgumentCaptor.getValue();
        loadingListener.onSuccess(Collections.<Build>emptyList());
        verify(mView).removeLoadMore();
        verify(mView).addMoreBuilds(any(BuildListDataModelImpl.class));
        assertThat(mPresenter.mIsLoadMoreLoading, is(false));

        loadingListener.onFail("error");
        verify(mView, times(2)).removeLoadMore();
        verify(mView).showRetryLoadMoreSnackBar();
        assertThat(mPresenter.mIsLoadMoreLoading, is(false));
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
}