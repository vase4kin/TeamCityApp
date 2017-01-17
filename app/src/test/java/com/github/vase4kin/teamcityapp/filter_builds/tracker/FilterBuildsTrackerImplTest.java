package com.github.vase4kin.teamcityapp.filter_builds.tracker;

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
public class FilterBuildsTrackerImplTest {

    @Mock
    private Answers mAnswers;

    private FilterBuildsTrackerImpl mTracker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Fabric.class);
        PowerMockito.mockStatic(Answers.class);
        when(Answers.getInstance()).thenReturn(mAnswers);
        mTracker = new FilterBuildsTrackerImpl();
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
    public void trackUserFilteredBuildsIfFabricIsNotInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(false);
        mTracker.trackUserFilteredBuilds();
    }

    @Test
    public void trackUserFilteredBuildsIfFabricIsInitialized() throws Exception {
        when(Fabric.isInitialized()).thenReturn(true);
        mTracker.trackUserFilteredBuilds();
        verify(mAnswers).logCustom(any(CustomEvent.class));
    }

}