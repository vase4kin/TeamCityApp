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

package com.github.vase4kin.teamcityapp.testdetails.presenter;

import android.os.Bundle;
import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManager;
import com.github.vase4kin.teamcityapp.testdetails.extractor.TestDetailsValueExtractor;
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsView;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@PrepareForTest(TextUtils.class)
@RunWith(PowerMockRunner.class)
public class TestOccurrencePresenterImplTest {

    @Captor
    private ArgumentCaptor<OnLoadingListener<TestOccurrences.TestOccurrence>> mArgumentCaptor;

    @Mock
    private TestOccurrences.TestOccurrence mTestOccurrence;

    @Mock
    private ViewTracker mTracker;

    @Mock
    private Bundle mBundle;

    @Mock
    private TestDetailsView mViewModel;

    @Mock
    private TestDetailsDataManager mDataManager;

    @Mock
    private TestDetailsValueExtractor mValueExtractor;

    private TestDetailsPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(TextUtils.class);
        mPresenter = new TestDetailsPresenterImpl(mViewModel, mDataManager, mTracker, mValueExtractor);
    }

    @Test
    public void testOnViewPreparedAndOnRetryIfUrlIsNotNull() throws Exception {
        when(mValueExtractor.getTestUrl()).thenReturn("url");
        mPresenter.onCreate();
        verify(mViewModel).initViews(eq(mPresenter));
        verify(mViewModel).showProgress();
        verify(mDataManager).loadData(mArgumentCaptor.capture(), eq("url"));
        verify(mValueExtractor).getTestUrl();

        when(mTestOccurrence.getDetails()).thenReturn("Test details");
        when(TextUtils.isEmpty(anyString())).thenReturn(false);
        OnLoadingListener<TestOccurrences.TestOccurrence> listener = mArgumentCaptor.getValue();
        listener.onSuccess(mTestOccurrence);
        verify(mViewModel).hideProgress();
        verify(mViewModel).showTestDetails(eq("Test details"));

        when(mTestOccurrence.getDetails()).thenReturn("");
        when(TextUtils.isEmpty(anyString())).thenReturn(true);
        listener.onSuccess(mTestOccurrence);
        verify(mViewModel, times(2)).hideProgress();
        verify(mViewModel).showEmptyData();

        listener.onFail("error");
        verify(mViewModel, times(3)).hideProgress();
        verify(mViewModel).showRetryView(eq("error"));

        mPresenter.onRetry();

        verify(mDataManager, times(2)).loadData(Mockito.<OnLoadingListener<TestOccurrences.TestOccurrence>>any(), eq("url"));

        verifyNoMoreInteractions(mViewModel, mDataManager, mTracker);
    }

    @Test
    public void testOnResume() throws Exception {
        mPresenter.onResume();
        verify(mTracker).trackView();
        verifyNoMoreInteractions(mViewModel, mDataManager, mTracker);
    }

    @Test
    public void testOnDestroyViews() throws Exception {
        mPresenter.onDestroy();
        verify(mViewModel).unBindViews();
        verify(mDataManager).unsubscribe();
        verifyNoMoreInteractions(mViewModel, mDataManager, mTracker);
    }
}