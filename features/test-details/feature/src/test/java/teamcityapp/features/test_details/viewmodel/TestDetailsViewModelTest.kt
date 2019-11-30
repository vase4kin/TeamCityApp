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

package teamcityapp.features.test_details.viewmodel

import android.view.View
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import teamcityapp.features.test_details.data.TestDetailsDataManager
import teamcityapp.features.test_details.repository.models.TestOccurrence
import teamcityapp.features.test_details.tracker.TestDetailsTracker

class TestDetailsViewModelTest {

    private val testOccurrence: TestOccurrence = mock()
    private val tracker: TestDetailsTracker = mock()
    private val dataManager: TestDetailsDataManager = mock()
    private val finish: () -> Unit = mock()

    private val url = "url"

    private lateinit var viewModel: TestDetailsViewModel

    @Before
    fun setUp() {
        viewModel = TestDetailsViewModel(
            dataManager,
            tracker,
            url,
            finish
        )
    }

    @After
    fun tearDown() {
        Mockito.verifyNoMoreInteractions(dataManager, tracker, finish)
    }

    @Ignore
    @Test
    fun testOnViewPreparedAndOnRetryIfUrlIsNotNull() {
        viewModel.onCreate()
        Mockito.verify(dataManager).loadData(any(), any(), eq(url))
        Mockito.verify(tracker).trackView()
        Assert.assertEquals(View.VISIBLE, viewModel.progressVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.emptyVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.errorVisibility.get())
        val onSuccessCaptor = argumentCaptor<(test: String) -> Unit>()
        val onErrorCaptor = argumentCaptor<(errorMessage: String) -> Unit>()
        Mockito.verify(dataManager)
            .loadData(onSuccessCaptor.capture(), onErrorCaptor.capture(), eq(url))

        Mockito.`when`(testOccurrence.details).thenReturn("")
        val testDetails = "Test details"
        onSuccessCaptor.lastValue(testDetails)
        Assert.assertEquals(View.GONE, viewModel.progressVisibility.get())
        Assert.assertEquals(View.VISIBLE, viewModel.testDetailsVisibility.get())
        Assert.assertEquals(testDetails, viewModel.testDetailsText.get())

        Mockito.`when`(testOccurrence.details).thenReturn("")
        onSuccessCaptor.lastValue(testDetails)
        Assert.assertEquals(View.GONE, viewModel.progressVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.errorVisibility.get())
        Assert.assertEquals(View.VISIBLE, viewModel.emptyVisibility.get())

        val error = "error"
        onErrorCaptor.lastValue(error)
        Assert.assertEquals(View.GONE, viewModel.progressVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.emptyVisibility.get())
        Assert.assertEquals(View.VISIBLE, viewModel.errorVisibility.get())
    }

    @Test
    fun testOnDestroyViews() {
        viewModel.onDestroy()
        Mockito.verify(dataManager).unsubscribe()
    }
}
