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

package com.github.vase4kin.teamcityapp.build_details.presenter;

import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractor;
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouter;
import com.github.vase4kin.teamcityapp.build_details.tracker.BuildDetailsTracker;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsView;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor;
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class BuildDetailsPresenterImplTest {

    @Mock
    private BuildDetailsView view;
    @Mock
    private BuildDetailsTracker tracker;
    @Mock
    private BuildDetailsInteractor dataManager;
    @Mock
    private BuildDetailsRouter router;
    @Mock
    private RunBuildInteractor runBuildInteractor;
    @Mock
    private BuildInteractor interactor;

    private BuildDetailsPresenterImpl presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new BuildDetailsPresenterImpl(view, tracker, dataManager, router, runBuildInteractor, interactor);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(view, tracker, dataManager, router, runBuildInteractor, interactor);
    }

    @Test
    public void onShow() throws Exception {
        presenter.onShow();
        verify(view).showRunBuildFloatActionButton();
    }

    @Test
    public void onHide() throws Exception {
        presenter.onHide();
        verify(view).hideRunBuildFloatActionButton();
    }

}