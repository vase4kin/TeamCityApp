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

package com.github.vase4kin.teamcityapp.base.tabs.presenter

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.utils.eq
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BaseTabsPresenterImplTest {

    @Mock
    private lateinit var viewModel: BaseTabsViewModel

    @Mock
    private lateinit var tracker: ViewTracker

    @Mock
    private lateinit var dataManager: BaseTabsDataManager

    private lateinit var presenter: BaseTabsPresenterImpl<*, *, *>

    @Before
    fun setUp() {
        presenter = BaseTabsPresenterImpl(viewModel, tracker, dataManager)
    }

    @Test
    fun testHandleOnViewsCreated() {
        presenter.onViewsCreated()
        verify(viewModel).initViews()
        verifyNoMoreInteractions(viewModel, tracker, dataManager)
    }

    @Test
    fun testHandleOnViewsDestroyed() {
        presenter.onViewsDestroyed()
        verify(viewModel).unBindViews()
        verifyNoMoreInteractions(viewModel, tracker, dataManager)
    }

    @Test
    fun testHandleOnResume() {
        presenter.onResume()
        verify(dataManager).registerEventBus()
        verify(dataManager).setListener(eq(presenter))
        verify(tracker).trackView()
        verifyNoMoreInteractions(viewModel, tracker, dataManager)
    }

    @Test
    fun testHandleOnPause() {
        presenter.onPause()
        verify(dataManager).unregisterEventBus()
        verifyNoMoreInteractions(viewModel, tracker, dataManager)
    }
}
