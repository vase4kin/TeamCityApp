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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter;

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractor;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BottomSheetPresenterImplTest {

    @Mock
    private BottomSheetDataModel model;
    @Mock
    private BottomSheetView view;
    @Mock
    private BottomSheetInteractor interactor;

    private BottomSheetPresenterImpl presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new BottomSheetPresenterImpl(view, interactor);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(view, interactor);
    }

    @Test
    public void handleOnCreateView() throws Exception {
        when(interactor.getBottomSheetDataModel()).thenReturn(model);
        when(interactor.getTitle()).thenReturn("title");
        presenter.handleOnCreateView();
        verify(interactor).getBottomSheetDataModel();
        verify(interactor).getTitle();
        verify(view).initViews(eq(presenter), eq(model), eq("title"));
    }

    @Test
    public void handleOnDestroyView() throws Exception {
        presenter.handleOnDestroyView();
        verify(view).unbindViews();
    }

    @Test
    public void onCopyActionClick() throws Exception {
        presenter.onCopyActionClick("br");
        verify(interactor).copyTextToClipBoard(eq("br"));
        verify(interactor).postTextCopiedEvent();
        verify(view).close();
    }

    @Test
    public void onShowBuildsActionClick() throws Exception {
        presenter.onShowBuildsActionClick("branch");
        verify(interactor).postNavigateToBuildListEvent(eq("branch"));
        verify(view).close();
    }

}