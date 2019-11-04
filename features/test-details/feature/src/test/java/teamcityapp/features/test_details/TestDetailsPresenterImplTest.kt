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

package teamcityapp.features.test_details

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import teamcityapp.features.test_details.data.TestDetailsDataManager
import teamcityapp.features.test_details.presenter.TestDetailsPresenterImpl
import teamcityapp.features.test_details.repository.models.TestOccurrence
import teamcityapp.features.test_details.tracker.TestDetailsTracker
import teamcityapp.features.test_details.view.TestDetailsView

class TestDetailsPresenterImplTest {

    private val testOccurrence: TestOccurrence = mock()
    private val tracker: TestDetailsTracker = mock()
    private val viewModel: TestDetailsView = mock()
    private val dataManager: TestDetailsDataManager = mock()

    private val url = "url"

    private lateinit var presenter: TestDetailsPresenterImpl

    @Before
    fun setUp() {
        presenter = TestDetailsPresenterImpl(
            viewModel,
            dataManager,
            tracker,
            url
        )
    }

    @Test
    fun testOnViewPreparedAndOnRetryIfUrlIsNotNull() {
        presenter.onCreate()
        verify(viewModel).initViews(presenter)
        verify(viewModel).showProgress()
        val onSuccessCaptor = argumentCaptor<(test: TestOccurrence) -> Unit>()
        val onErrorCaptor = argumentCaptor<(errorMessage: String) -> Unit>()
        verify(dataManager).loadData(onSuccessCaptor.capture(), onErrorCaptor.capture(), eq(url))

        `when`(testOccurrence.details).thenReturn("Test details")
        onSuccessCaptor.lastValue(testOccurrence)
        verify(viewModel).hideProgress()
        verify(viewModel).showTestDetails(eq("Test details"))

        `when`(testOccurrence.details).thenReturn("")
        onSuccessCaptor.lastValue(testOccurrence)
        verify(viewModel, times(2)).hideProgress()
        verify(viewModel).showEmptyData()

        val error = "error"
        onErrorCaptor.lastValue(error)
        verify(viewModel, times(3)).hideProgress()
        verify(viewModel).showRetryView(eq(error))

        presenter.onRetry()

        verify(dataManager, times(2)).loadData(any(), any(), eq("url"))

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
        verify(dataManager).unsubscribe()
        verifyNoMoreInteractions(viewModel, dataManager, tracker)
    }
}
