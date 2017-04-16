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

package com.github.vase4kin.teamcityapp.build_details.tracker;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.fabric.sdk.android.Fabric;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Fabric.class, Answers.class})
public class BuildTabsViewTrackerImplTest {

    @Mock
    private Answers mAnswers;

    private FabricBuildDetailsViewTrackerImpl mTracker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Fabric.class);
        PowerMockito.mockStatic(Answers.class);
        when(Answers.getInstance()).thenReturn(mAnswers);
        mTracker = new FabricBuildDetailsViewTrackerImpl();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mAnswers);
    }

    @Test
    public void testTrackViewIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackView();
    }

    @Test
    public void testTrackViewIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackView();
        verify(mAnswers).logContentView(any(ContentViewEvent.class));
    }

    @Test
    public void testTrackUserConfirmedCancelIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserConfirmedCancel(true);
    }

    @Test
    public void testTrackUserConfirmedCancelIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserConfirmedCancel(false);
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void testTrackUserGetsForbiddenErrorOnBuildCancelIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserGetsForbiddenErrorOnBuildCancel();
    }

    @Test
    public void testTrackUserGetsForbiddenErrorOnBuildCancelIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserGetsForbiddenErrorOnBuildCancel();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void testTrackUserGetsServerErrorOnBuildCancelIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserGetsServerErrorOnBuildCancel();
    }

    @Test
    public void testTrackUserGetsServerErrorOnBuildCancelIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserGetsServerErrorOnBuildCancel();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void testTrackUserCanceledBuildSuccessfullyIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserCanceledBuildSuccessfully();
    }

    @Test
    public void testUserCanceledBuildSuccessfullyIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserCanceledBuildSuccessfully();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void testTrackUserGetsForbiddenErrorOnBuildRestartIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserGetsForbiddenErrorOnBuildRestart();
    }

    @Test
    public void testTrackUserGetsForbiddenErrorOnBuildRestartIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserGetsForbiddenErrorOnBuildRestart();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void testTrackUserGetsServerErrorOnBuildRestartIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserGetsServerErrorOnBuildRestart();
    }

    @Test
    public void testTrackUserGetsServerErrorOnBuildRestartIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserGetsServerErrorOnBuildRestart();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void testTrackUserRestartedBuildSuccessfullyIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserRestartedBuildSuccessfully();
    }

    @Test
    public void testUserRestartedBuildSuccessfullyIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserRestartedBuildSuccessfully();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void trackUserWantsToSeeQueuedBuildDetailsIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserWantsToSeeQueuedBuildDetails();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void trackUserFailedToSeeQueuedBuildDetailsIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserFailedToSeeQueuedBuildDetails();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

    @Test
    public void trackUserWantsToSeeQueuedBuildDetailsIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserWantsToSeeQueuedBuildDetails();
    }

    @Test
    public void trackUserFailedToSeeQueuedBuildDetailsIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserFailedToSeeQueuedBuildDetails();
    }
}