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

package com.github.vase4kin.teamcityapp.changes.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent;
import com.github.vase4kin.teamcityapp.buildlist.view.OnLoadMoreListener;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManager;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModelImpl;
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractor;
import com.github.vase4kin.teamcityapp.changes.view.ChangesView;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ChangesPresenterImplTest {

    @Captor
    private ArgumentCaptor<OnLoadMoreListener> mOnLoadMoreListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<OnLoadingListener<List<Changes.Change>>> mOnChangesLoadingListener;

    @Captor
    private ArgumentCaptor<OnLoadingListener<Integer>> mOnLoadingListenerArgumentCaptor;

    @Captor
    private ArgumentCaptor<OnTextTabChangeEvent> mEventCaptor;

    @Mock
    private OnLoadingListener<List<Changes.Change>> mLoadingListener;

    @Mock
    private ChangesView mView;

    @Mock
    ChangesDataManager mDataManager;

    @Mock
    ViewTracker mTracker;

    @Mock
    ChangesValueExtractor mValueExtractor;

    private ChangesPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new ChangesPresenterImpl(mView, mDataManager, mValueExtractor);
    }

    @Test
    public void testInitViews() throws Exception {
        when(mDataManager.canLoadMore()).thenReturn(false);
        mPresenter.initViews();
        verify(mView).setOnLoadMoreListener(mOnLoadMoreListenerArgumentCaptor.capture());

        OnLoadMoreListener onLoadMoreListener = mOnLoadMoreListenerArgumentCaptor.getValue();
        assertThat(onLoadMoreListener.isLoadedAllItems(), is(true));
        verify(mDataManager).canLoadMore();

        onLoadMoreListener.loadMore();
        verify(mView).addLoadMoreItem();
        verify(mDataManager).loadMore(mOnChangesLoadingListener.capture());

        OnLoadingListener<List<Changes.Change>> onChangesLoadingListener = mOnChangesLoadingListener.getValue();
        List<Changes.Change> changes = Collections.emptyList();
        onChangesLoadingListener.onSuccess(changes);
        verify(mView).removeLoadMoreItem();
        verify(mView).addMoreBuilds(any(ChangesDataModelImpl.class));

        onChangesLoadingListener.onFail("error");
        verify(mView, times(2)).removeLoadMoreItem();
        verify(mView).showRetryLoadMoreSnackBar();
    }

    @Test
    public void testLoadData() throws Exception {
        when(mValueExtractor.getUrl()).thenReturn("url");
        mPresenter.loadData(mLoadingListener);
        verify(mValueExtractor).getUrl();
        verify(mDataManager).loadLimited(eq("url"), eq(mLoadingListener));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor);
    }

    @Test
    public void testCreateModel() throws Exception {
        List<Changes.Change> changes = Collections.emptyList();
        assertThat(mPresenter.createModel(changes).getItemCount(), is(0));
    }

    @Test
    public void testOnViewsCreated() throws Exception {
        when(mValueExtractor.getUrl()).thenReturn("url");
        mPresenter.onViewsCreated();
        verify(mValueExtractor, times(2)).getUrl();
        verify(mDataManager).loadTabTitle(eq("url"), mOnLoadingListenerArgumentCaptor.capture());

        OnLoadingListener<Integer> listener = mOnLoadingListenerArgumentCaptor.getValue();
        listener.onSuccess(1);
        verify(mDataManager).postChangeTabTitleEvent(eq(1));
        listener.onFail("error");
        verifyNoMoreInteractions(mValueExtractor);
    }
}