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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractor
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView
import com.github.vase4kin.teamcityapp.utils.eq
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BottomSheetPresenterImplTest {

    @Mock
    private lateinit var model: BottomSheetDataModel
    @Mock
    private lateinit var view: BottomSheetView
    @Mock
    private lateinit var interactor: BottomSheetInteractor

    private lateinit var presenter: BottomSheetPresenterImpl

    @Before
    fun setUp() {
        presenter = BottomSheetPresenterImpl(view, interactor)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, interactor)
    }

    @Test
    fun handleOnCreateView() {
        `when`(interactor.bottomSheetDataModel).thenReturn(model)
        `when`(interactor.title).thenReturn("title")
        presenter.handleOnCreateView()
        verify(interactor).bottomSheetDataModel
        verify(interactor).title
        verify(view).initViews(eq(presenter), eq(model), eq("title"))
    }

    @Test
    fun handleOnDestroyView() {
        presenter.handleOnDestroyView()
        verify(view).unbindViews()
    }

    @Test
    fun onCopyActionClick() {
        presenter.onCopyActionClick("br")
        verify(interactor).copyTextToClipBoard(eq("br"))
        verify(interactor).postTextCopiedEvent()
        verify(view).close()
    }

    @Test
    fun onShowBuildsActionClick() {
        presenter.onShowBuildsActionClick("branch")
        verify(interactor).postNavigateToBuildListEvent(eq("branch"))
        verify(view).close()
    }

    @Test
    fun onShowBuildTypeActionClick() {
        presenter.onShowBuildTypeActionClick()
        verify(interactor).postNavigateToBuildTypeEvent()
        verify(view).close()
    }

    @Test
    fun onShowProjectActionClick() {
        presenter.onShowProjectActionClick()
        verify(interactor).postNavigateToProjectEvent()
        verify(view).close()
    }

}