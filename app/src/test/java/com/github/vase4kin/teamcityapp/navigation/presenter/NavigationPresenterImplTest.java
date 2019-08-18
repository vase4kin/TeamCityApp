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

package com.github.vase4kin.teamcityapp.navigation.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager;
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor;
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter;
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class NavigationPresenterImplTest {

    @Mock
    private BuildType mBuildType;

    @Mock
    private NavigationItem mNavigationItem;

    @Mock
    private OnLoadingListener<List<NavigationItem>> mLoadingListener;

    @Mock
    private NavigationView mView;

    @Mock
    private NavigationDataManager mDataManager;

    @Mock
    private NavigationTracker mTracker;

    @Mock
    private NavigationValueExtractor mValueExtractor;

    @Mock
    private NavigationRouter mRouter;

    private NavigationPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new NavigationPresenterImpl(mView, mDataManager, mTracker, mValueExtractor, mRouter);
    }

    @Test
    public void testLoadData() {
        when(mValueExtractor.getId()).thenReturn("url");
        mPresenter.loadData(mLoadingListener, false);
        verify(mValueExtractor).getId();
        verify(mDataManager).load(eq("url"), eq(false), eq(mLoadingListener));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter);
    }

    @Test
    public void testInitViews() {
        when(mValueExtractor.getName()).thenReturn("name");
        mPresenter.initViews();
        verify(mView).setTitle(eq("name"));
        verify(mView).setNavigationAdapterClickListener(eq(mPresenter));
    }

    @Test
    public void testCreateModel() {
        List<NavigationItem> items = Collections.emptyList();
        assertThat(mPresenter.createModel(items).getItemCount(), is(0));
    }

    @Test
    public void testOnClickIfBuildType() {
        when(mBuildType.getName()).thenReturn("name");
        when(mBuildType.getId()).thenReturn("id");
        mPresenter.onClick(mBuildType);
        verify(mRouter).startBuildListActivity(eq("name"), eq("id"));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter);
    }

    @Test
    public void testOnClickIfNotBuildType() {
        when(mNavigationItem.getName()).thenReturn("name");
        when(mNavigationItem.getId()).thenReturn("id");
        mPresenter.onClick(mNavigationItem);
        verify(mRouter).startNavigationActivity(eq("name"), eq("id"));
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mValueExtractor, mRouter);
    }
}