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

package com.github.vase4kin.teamcityapp.root.presenter;

import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.root.data.RootDataManager;
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManager;
import com.github.vase4kin.teamcityapp.root.router.RootRouter;
import com.github.vase4kin.teamcityapp.root.tracker.RootTracker;
import com.github.vase4kin.teamcityapp.root.view.OnAccountSwitchListener;
import com.github.vase4kin.teamcityapp.root.view.RootDrawerView;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class RootDrawerPresenterImplTest {

    @Mock
    RootDrawerView mView;
    @Mock
    RootDataManager mDataManager;
    @Mock
    OnAccountSwitchListener mListener;
    @Mock
    RootBundleValueManager mValueExtractor;
    @Mock
    RootRouter mRouter;
    @Mock
    BuildLogInteractor mInteractor;
    @Mock
    RootTracker mTracker;
    @Mock
    private UserAccount mUserAccount;
    @Mock
    private OnboardingManager mOnboardingManager;

    private RootDrawerPresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(TextUtils.class);
        mPresenter = new RootDrawerPresenterImpl(mView, mDataManager, mListener, mValueExtractor, mRouter, mInteractor, mTracker, mOnboardingManager);
    }

    @Test
    public void testOnResume() throws Exception {
        when(TextUtils.isEmpty(anyString())).thenReturn(false);
        when(mDataManager.getActiveUser()).thenReturn(mUserAccount);
        when(mUserAccount.getTeamcityUrl()).thenReturn("");
        mPresenter.onResume();
        verify(mTracker).trackView();
        verify(mView).showAppRateDialog(eq(mPresenter));
    }

    @Test
    public void testOnNeutralButtonClick() throws Exception {
        mPresenter.onNeutralButtonClick();
        verify(mTracker).trackUserDecidedToRateTheAppLater();
    }

    @Test
    public void testOnNegativeButtonClick() throws Exception {
        mPresenter.onNegativeButtonClick();
        verify(mTracker).trackUserDidNotRateTheApp();
    }

    @Test
    public void testOnPositiveButtonClick() throws Exception {
        mPresenter.onPositiveButtonClick();
        verify(mTracker).trackUserRatedTheApp();
    }

}