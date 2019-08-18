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

package com.github.vase4kin.teamcityapp.tests.presenter

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter
import com.github.vase4kin.teamcityapp.tests.view.TestsView
import com.github.vase4kin.teamcityapp.utils.any
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import com.mugen.MugenCallbacks
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TestsPresenterImplTest {

    @Captor
    private lateinit var onLoadingIntegerListenerArgumentCaptor: ArgumentCaptor<OnLoadingListener<Int>>
    @Captor
    private lateinit var onLoadMoreListenerArgumentCaptor: ArgumentCaptor<MugenCallbacks>
    @Captor
    private lateinit var onLoadingListenerArgumentCaptor: ArgumentCaptor<OnLoadingListener<List<TestOccurrences.TestOccurrence>>>
    @Mock
    private lateinit var menuItem: MenuItem
    @Mock
    private lateinit var menu: Menu
    @Mock
    private lateinit var menuInflater: MenuInflater
    @Mock
    private lateinit var testOccurrence: TestOccurrences.TestOccurrence
    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<TestOccurrences.TestOccurrence>>
    @Mock
    private lateinit var view: TestsView
    @Mock
    private lateinit var dataManager: TestsDataManager
    @Mock
    private lateinit var tracker: ViewTracker
    @Mock
    private lateinit var valueExtractor: TestsValueExtractor
    @Mock
    private lateinit var router: TestsRouter
    private lateinit var presenter: TestsPresenterImpl

    @Before
    fun setUp() {
        presenter = TestsPresenterImpl(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testLoadData() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).url
        verify(dataManager).loadFailedTests(eq("url"), eq(loadingListener), eq(false))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testCreateModel() {
        `when`(testOccurrence.name).thenReturn("name")
        assertThat(presenter.createModel(listOf(testOccurrence)).getName(0), `is`(equalTo("name")))
    }

    @Test
    fun testInitViews() {
        `when`(dataManager.canLoadMore()).thenReturn(false)
        presenter.initViews()
        verify(view).setOnLoadMoreListener(capture(onLoadMoreListenerArgumentCaptor))

        val onLoadMoreListener = onLoadMoreListenerArgumentCaptor.value
        assertThat(onLoadMoreListener.hasLoadedAllItems(), `is`(true))
        verify(dataManager).canLoadMore()

        onLoadMoreListener.onLoadMore()
        verify(view).addLoadMore()
        verify(dataManager).loadMore(capture(onLoadingListenerArgumentCaptor))
        assertThat(presenter.isLoadMoreLoading, `is`(true))

        val listOnLoadingListener = onLoadingListenerArgumentCaptor.value
        val tests = emptyList<TestOccurrences.TestOccurrence>()
        listOnLoadingListener.onSuccess(tests)
        verify(view).removeLoadMore()
        verify(view).addMoreBuilds(any())
        assertThat(presenter.isLoadMoreLoading, `is`(false))

        listOnLoadingListener.onFail("error")
        verify(view, times(2)).removeLoadMore()
        verify(view).showRetryLoadMoreSnackBar()
        assertThat(presenter.isLoadMoreLoading, `is`(false))
    }

    @Test
    fun testOnViewsCreated() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.onViewsCreated()
        verify(valueExtractor, times(2)).url
        verify(dataManager).loadTestDetails(eq("url"), capture(onLoadingIntegerListenerArgumentCaptor))

        val listener = onLoadingIntegerListenerArgumentCaptor.value
        listener.onSuccess(1)
        verify(dataManager).postChangeTabTitleEvent(eq(1))

        listener.onFail("error")
        verifyNoMoreInteractions(valueExtractor)
    }

    @Test
    fun testOnCreateOptionsMenu() {
        presenter.onCreateOptionsMenu(menu, menuInflater)
        verify(view).onCreateOptionsMenu(eq(menu), eq(menuInflater))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, menu, menuInflater)
    }

    @Test
    fun testOnPrepareOptionsMenu() {
        presenter.onPrepareOptionsMenu(menu)
        verify(view).onPrepareOptionsMenu(eq(menu))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, menu)
    }

    @Test
    fun testOnOptionsItemSelected() {
        `when`(view.onOptionsItemSelected(eq(menuItem))).thenReturn(true)
        assertThat(presenter.onOptionsItemSelected(menuItem), `is`(equalTo(true)))
        verify(view).showRefreshAnimation()
        verify(view).hideErrorView()
        verify(view).hideEmpty()
        verify(view).onOptionsItemSelected(eq(menuItem))
        verify(view).showData(any())
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, menuItem)
    }

    @Test
    fun testLoadFailedTests() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.loadFailedTests()
        verify(valueExtractor).url
        verify(dataManager).loadFailedTests(eq("url"))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testLoadSuccessTests() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.loadSuccessTests()
        verify(valueExtractor).url
        verify(dataManager).loadPassedTests(eq("url"))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testLoadIgnoredTests() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.loadIgnoredTests()
        verify(valueExtractor).url
        verify(dataManager).loadIgnoredTests(eq("url"))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testOnFailedTestClick() {
        presenter.onFailedTestClick("url")
        verify(router).openFailedTest(eq("url"))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }
}
