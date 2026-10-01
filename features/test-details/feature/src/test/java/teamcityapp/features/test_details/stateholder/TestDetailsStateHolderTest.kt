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

package teamcityapp.features.test_details.stateholder

import android.view.View
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import teamcityapp.features.test_details.data.TestDetailsDataManager
import teamcityapp.features.test_details.tracker.TestDetailsTracker

class TestDetailsStateHolderTest {

    private val tracker: TestDetailsTracker = mock()
    private val dataManager: TestDetailsDataManager = mock()
    private val finish: () -> Unit = mock()
    private val showErrorToast: () -> Unit = mock()
    private val url = "url"

    private lateinit var stateHolder: TestDetailsStateHolder

    @Before
    fun setUp() {
        stateHolder = TestDetailsStateHolder(
            dataManager,
            tracker,
            url,
            showErrorToast,
            finish
        )
    }

    @After
    fun tearDown() {
        Mockito.verifyNoMoreInteractions(dataManager, tracker, finish, showErrorToast)
    }

    @Test
    fun testLoadDataIfTestUrlIsEmpty() {
        stateHolder = TestDetailsStateHolder(
            dataManager,
            tracker,
            "",
            showErrorToast,
            finish
        )
        stateHolder.onCreate()
        verify(showErrorToast).invoke()
        verify(tracker).trackView()
        verify(finish).invoke()
    }

    @Test
    fun testRetry() {
        stateHolder.onRetry()
        verify(dataManager).loadData(any(), any(), any())
    }

    @Test
    fun testFinish() {
        stateHolder.finish()
        verify(finish).invoke()
    }

    @Test
    fun testOnCreateOnSuccess() {
        Assert.assertEquals(View.GONE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        stateHolder.onCreate()
        verify(tracker).trackView()
        Assert.assertEquals(View.VISIBLE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        val onSuccessCaptor = argumentCaptor<(test: String) -> Unit>()
        verify(dataManager)
            .loadData(onSuccessCaptor.capture(), any(), eq(url))
        val testDetails = "Test details"
        onSuccessCaptor.lastValue(testDetails)
        Assert.assertEquals(View.GONE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        Assert.assertEquals(View.VISIBLE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(testDetails, stateHolder.testDetailsText.get())
    }

    @Test
    fun testOnCreateOnSuccessIfDetailsIsEmpty() {
        Assert.assertEquals(View.GONE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        stateHolder.onCreate()
        verify(tracker).trackView()
        Assert.assertEquals(View.VISIBLE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        val onSuccessCaptor = argumentCaptor<(test: String) -> Unit>()
        verify(dataManager)
            .loadData(onSuccessCaptor.capture(), any(), eq(url))
        onSuccessCaptor.lastValue("")
        Assert.assertEquals(View.GONE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.VISIBLE, stateHolder.emptyVisibility.get())
    }

    @Test
    fun testOnCreateOnError() {
        Assert.assertEquals(View.GONE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        stateHolder.onCreate()
        verify(tracker).trackView()
        Assert.assertEquals(View.VISIBLE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.errorVisibility.get())
        val onErrorCaptor = argumentCaptor<() -> Unit>()
        verify(dataManager)
            .loadData(any(), onErrorCaptor.capture(), eq(url))
        onErrorCaptor.lastValue()
        Assert.assertEquals(View.GONE, stateHolder.progressVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.testDetailsVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(View.VISIBLE, stateHolder.errorVisibility.get())
    }

    @Test
    fun testOnDestroyViews() {
        stateHolder.onDestroy()
        verify(dataManager).unsubscribe()
    }
}
