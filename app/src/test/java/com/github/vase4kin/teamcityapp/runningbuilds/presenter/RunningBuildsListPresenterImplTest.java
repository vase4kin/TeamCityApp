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

package com.github.vase4kin.teamcityapp.runningbuilds.presenter;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker;
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RunningBuildsListPresenterImplTest {

    @Mock
    private OnLoadingListener<List<Build>> mLoadingListener;

    @Mock
    private RunningBuildListView mView;

    @Mock
    private RunningBuildsDataManager mDataManager;

    @Mock
    private BuildListTracker mTracker;

    @Mock
    private BuildListRouter mRouter;

    @Mock
    private BaseValueExtractor mValueExtractor;

    @Mock
    private BuildInteractor mInteractor;

    private RunningBuildsListPresenterImpl mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new RunningBuildsListPresenterImpl(mView, mDataManager, mTracker, mRouter, mValueExtractor, mInteractor);
    }

    @Test
    public void testLoadData() throws Exception {
        mPresenter.loadData(mLoadingListener);
        verify(mDataManager).load(mLoadingListener);
        verifyNoMoreInteractions(mView, mDataManager, mTracker, mRouter, mValueExtractor, mInteractor);
    }

    @Test
    public void testOnSuccessCallBack() throws Exception {
        mPresenter.onSuccessCallBack(Collections.singletonList(new Build()));
        verify(mView).updateTitle(eq(1));
    }

    @Test
    public void testOnFailCallBack() throws Exception {
        mPresenter.onFailCallBack("error");
        verify(mView).updateTitle(eq(0));
    }
}