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

package com.github.vase4kin.teamcityapp.build_details.presenter

import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractor
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouter
import com.github.vase4kin.teamcityapp.build_details.tracker.BuildDetailsTracker
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsView
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BuildDetailsPresenterImplTest {

    @Mock
    private lateinit var view: BuildDetailsView
    @Mock
    private lateinit var tracker: BuildDetailsTracker
    @Mock
    private lateinit var dataManager: BuildDetailsInteractor
    @Mock
    private lateinit var router: BuildDetailsRouter
    @Mock
    private lateinit var runBuildInteractor: RunBuildInteractor
    @Mock
    private lateinit var interactor: BuildInteractor

    private lateinit var presenter: BuildDetailsPresenterImpl

    @Before
    fun setUp() {
        presenter = BuildDetailsPresenterImpl(view, tracker, dataManager, router, runBuildInteractor, interactor)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, tracker, dataManager, router, runBuildInteractor, interactor)
    }

    @Test
    fun onShow() {
        presenter.onShow()
        verify(view).showRunBuildFloatActionButton()
    }

    @Test
    fun onHide() {
        presenter.onHide()
        verify(view).hideRunBuildFloatActionButton()
    }

}