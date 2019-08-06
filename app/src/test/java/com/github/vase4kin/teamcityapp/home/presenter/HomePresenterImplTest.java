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

package com.github.vase4kin.teamcityapp.home.presenter;

import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView;
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider;
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager;
import com.github.vase4kin.teamcityapp.home.extractor.HomeBundleValueManager;
import com.github.vase4kin.teamcityapp.home.router.HomeRouter;
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker;
import com.github.vase4kin.teamcityapp.home.view.HomeView;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class HomePresenterImplTest {

    @Captor
    private ArgumentCaptor<OnboardingManager.OnPromptShownListener> mOnPromptShownListenerArgumentCaptor;
    @Mock
    HomeView mView;
    @Mock
    HomeDataManager mDataManager;
    @Mock
    HomeBundleValueManager mValueExtractor;
    @Mock
    HomeRouter mRouter;
    @Mock
    BuildLogInteractor mInteractor;
    @Mock
    HomeTracker mTracker;
    @Mock
    private UserAccount mUserAccount;
    @Mock
    private OnboardingManager mOnboardingManager;
    @Mock
    private BottomNavigationView bottomNavigationView;

    private FilterProvider filterProvider = new FilterProvider();

    private HomePresenterImpl mPresenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(TextUtils.class);
        mPresenter = new HomePresenterImpl(mView, mDataManager, mTracker, mRouter, mValueExtractor, mInteractor, mOnboardingManager, bottomNavigationView, filterProvider);
    }

    @Test
    public void testOnResume() throws Exception {
        when(TextUtils.isEmpty(anyString())).thenReturn(false);
        when(mDataManager.getActiveUser()).thenReturn(mUserAccount);
        when(mUserAccount.getTeamcityUrl()).thenReturn("");
        mPresenter.onResume();
        verify(mTracker).trackView();
    }

    @Test
    public void testPromptIfItIsShown() throws Exception {
        when(TextUtils.isEmpty(anyString())).thenReturn(false);
        when(mDataManager.getActiveUser()).thenReturn(mUserAccount);
        when(mUserAccount.getTeamcityUrl()).thenReturn("");
        when(mOnboardingManager.isNavigationDrawerPromptShown()).thenReturn(true);
        mPresenter.onResume();
        verify(mOnboardingManager).isNavigationDrawerPromptShown();
    }

    @Test
    public void testPromptIfItIsNotShown() throws Exception {
        when(TextUtils.isEmpty(anyString())).thenReturn(false);
        when(mDataManager.getActiveUser()).thenReturn(mUserAccount);
        when(mUserAccount.getTeamcityUrl()).thenReturn("");
        when(mOnboardingManager.isNavigationDrawerPromptShown()).thenReturn(false);
        mPresenter.onResume();
        verify(mOnboardingManager).isNavigationDrawerPromptShown();
        verify(mView).showNavigationDrawerPrompt(mOnPromptShownListenerArgumentCaptor.capture());
        OnboardingManager.OnPromptShownListener listener = mOnPromptShownListenerArgumentCaptor.getValue();
        listener.onPromptShown();
        verify(mOnboardingManager).saveNavigationDrawerPromptShown();
    }
}