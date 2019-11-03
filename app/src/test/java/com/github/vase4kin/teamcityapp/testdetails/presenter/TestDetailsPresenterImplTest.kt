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

package com.github.vase4kin.teamcityapp.testdetails.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManager
import com.github.vase4kin.teamcityapp.testdetails.tracker.TestDetailsTracker
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsView
import com.github.vase4kin.teamcityapp.utils.any
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner
import teamcity.features.test_details.api.models.TestOccurrence

@RunWith(MockitoJUnitRunner::class)
class TestDetailsPresenterImplTest {

    @Captor
    private lateinit var argumentCaptor: ArgumentCaptor<OnLoadingListener<TestOccurrence>>
    @Mock
    private lateinit var testOccurrence: TestOccurrence
    @Mock
    private lateinit var tracker: TestDetailsTracker
    @Mock
    private lateinit var viewModel: TestDetailsView
    @Mock
    private lateinit var dataManager: TestDetailsDataManager

    private val url = "url"

    private lateinit var presenter: TestDetailsPresenterImpl

    @Before
    fun setUp() {
        presenter = TestDetailsPresenterImpl(viewModel, dataManager, tracker, url)
    }

    @Test
    fun testOnViewPreparedAndOnRetryIfUrlIsNotNull() {
        presenter.onCreate()
        verify(viewModel).initViews(eq(presenter))
        verify(viewModel).showProgress()
        verify(dataManager).loadData(capture(argumentCaptor), eq(url))

        `when`(testOccurrence.details).thenReturn("Test details")
        val listener = argumentCaptor.value
        listener.onSuccess(testOccurrence)
        verify(viewModel).hideProgress()
        verify(viewModel).showTestDetails(eq("Test details"))

        `when`(testOccurrence.details).thenReturn("")
        listener.onSuccess(testOccurrence)
        verify(viewModel, times(2)).hideProgress()
        verify(viewModel).showEmptyData()

        listener.onFail("error")
        verify(viewModel, times(3)).hideProgress()
        verify(viewModel).showRetryView(eq("error"))

        presenter.onRetry()

        verify(dataManager, times(2)).loadData(any(), eq("url"))

        verifyNoMoreInteractions(viewModel, dataManager, tracker)
    }

    @Test
    fun testOnResume() {
        presenter.onResume()
        verify(tracker).trackView()
        verifyNoMoreInteractions(viewModel, dataManager, tracker)
    }

    @Test
    fun testOnDestroyViews() {
        presenter.onDestroy()
        verify(viewModel).unBindViews()
        verify(dataManager).unsubscribe()
        verifyNoMoreInteractions(viewModel, dataManager, tracker)
    }
}
