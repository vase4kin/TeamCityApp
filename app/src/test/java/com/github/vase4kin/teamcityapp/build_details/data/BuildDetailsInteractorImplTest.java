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

package com.github.vase4kin.teamcityapp.build_details.data;

import android.view.View;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactErrorDownloadingEvent;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.overview.data.FloatButtonChangeVisibilityEvent;
import com.github.vase4kin.teamcityapp.overview.data.RestartBuildEvent;
import com.github.vase4kin.teamcityapp.overview.data.ShareBuildEvent;
import com.github.vase4kin.teamcityapp.overview.data.StartBuildsListActivityFilteredByBranchEvent;
import com.github.vase4kin.teamcityapp.overview.data.StopBuildEvent;
import com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BuildDetailsInteractorImplTest {

    @Mock
    private FloatButtonChangeVisibilityEvent floatButtonChangeVisibilityEvent;
    @Mock
    private OnBuildDetailsEventsListener listener;
    @Mock
    private EventBus eventBus;
    @Mock
    private BaseValueExtractor valueExtractor;
    @Mock
    private SharedUserStorage sharedUserStorage;
    @Mock
    private Repository repository;

    private BuildDetailsInteractorImpl interactor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interactor = new BuildDetailsInteractorImpl(eventBus, valueExtractor, sharedUserStorage, repository);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(eventBus, valueExtractor, sharedUserStorage, repository);
    }

    @Test
    public void onFloatButtonChangeVisibilityEventIfListenerIsNull() throws Exception {
        interactor.setOnBuildTabsEventsListener(null);
        interactor.onEvent(floatButtonChangeVisibilityEvent);
    }

    @Test
    public void onFloatButtonChangeVisibilityEventOnShow() throws Exception {
        when(floatButtonChangeVisibilityEvent.getVisibility()).thenReturn(View.VISIBLE);
        interactor.setOnBuildTabsEventsListener(listener);
        interactor.onEvent(floatButtonChangeVisibilityEvent);
        verify(listener).onShow();
    }

    @Test
    public void onFloatButtonChangeVisibilityEventOnHide() throws Exception {
        when(floatButtonChangeVisibilityEvent.getVisibility()).thenReturn(View.GONE);
        interactor.setOnBuildTabsEventsListener(listener);
        interactor.onEvent(floatButtonChangeVisibilityEvent);
        verify(listener).onHide();
    }

    @Test
    public void onFloatButtonChangeVisibilityEventOnNothing() throws Exception {
        when(floatButtonChangeVisibilityEvent.getVisibility()).thenReturn(View.INVISIBLE);
        interactor.setOnBuildTabsEventsListener(listener);
        interactor.onEvent(floatButtonChangeVisibilityEvent);
    }

    @Test
    public void onEvent1() throws Exception {
        interactor.onEvent(new StopBuildEvent());
    }

    @Test
    public void onEvent2() throws Exception {
        interactor.onEvent(new ShareBuildEvent());
    }

    @Test
    public void onEvent3() throws Exception {
        interactor.onEvent(new RestartBuildEvent());
    }

    @Test
    public void onEvent4() throws Exception {
        interactor.onEvent(new TextCopiedEvent());
    }

    @Test
    public void onEvent5() throws Exception {
        interactor.onEvent(new ArtifactErrorDownloadingEvent());
    }

    @Test
    public void onEvent6() throws Exception {
        interactor.onEvent(new StartBuildsListActivityFilteredByBranchEvent(""));
    }

}