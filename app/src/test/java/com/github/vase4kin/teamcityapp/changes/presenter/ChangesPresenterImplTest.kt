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

package com.github.vase4kin.teamcityapp.changes.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManager
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractor
import com.github.vase4kin.teamcityapp.changes.view.ChangesView
import com.github.vase4kin.teamcityapp.utils.any
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import com.mugen.MugenCallbacks
import org.hamcrest.core.Is.`is`
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
class ChangesPresenterImplTest {

    @Captor
    private lateinit var onLoadMoreListenerArgumentCaptor: ArgumentCaptor<MugenCallbacks>
    @Captor
    private lateinit var onChangesLoadingListener: ArgumentCaptor<OnLoadingListener<List<Changes.Change>>>
    @Captor
    private lateinit var onLoadingListenerArgumentCaptor: ArgumentCaptor<OnLoadingListener<Int>>
    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<Changes.Change>>
    @Mock
    private lateinit var view: ChangesView
    @Mock
    internal lateinit var dataManager: ChangesDataManager
    @Mock
    internal lateinit var tracker: ViewTracker
    @Mock
    internal lateinit var valueExtractor: ChangesValueExtractor
    private lateinit var presenter: ChangesPresenterImpl

    @Before
    fun setUp() {
        presenter = ChangesPresenterImpl(view, dataManager, tracker, valueExtractor)
    }

    @Test
    fun testInitViews() {
        `when`(dataManager.canLoadMore()).thenReturn(false)
        presenter.initViews()
        verify(view).setLoadMoreListener(capture(onLoadMoreListenerArgumentCaptor))

        val onLoadMoreListener = onLoadMoreListenerArgumentCaptor.value
        assertThat(onLoadMoreListener.hasLoadedAllItems(), `is`(true))
        verify(dataManager).canLoadMore()

        onLoadMoreListener.onLoadMore()
        verify(view).addLoadMore()
        verify(dataManager).loadMore(capture(onChangesLoadingListener))
        assertThat(presenter.isLoadMoreLoading, `is`(true))

        val onChangesLoadingListener = this.onChangesLoadingListener.value
        val changes = emptyList<Changes.Change>()
        onChangesLoadingListener.onSuccess(changes)
        verify(view).removeLoadMore()
        verify(view).addMoreBuilds(any())
        assertThat(presenter.isLoadMoreLoading, `is`(false))

        onChangesLoadingListener.onFail("error")
        verify(view, times(2)).removeLoadMore()
        verify(view).showRetryLoadMoreSnackBar()
        assertThat(presenter.isLoadMoreLoading, `is`(false))
    }

    @Test
    fun testLoadData() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).url
        verify(dataManager).loadLimited(eq("url"), eq(loadingListener), eq(false))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor)
    }

    @Test
    fun testCreateModel() {
        val changes = mutableListOf<Changes.Change>()
        assertThat(presenter.createModel(changes).itemCount, `is`(0))
    }

    @Test
    fun testOnViewsCreated() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.onViewsCreated()
        verify(valueExtractor, times(2)).url
        verify(dataManager).loadTabTitle(eq("url"), capture(onLoadingListenerArgumentCaptor))

        val listener = onLoadingListenerArgumentCaptor.value
        listener.onSuccess(1)
        verify(dataManager).postChangeTabTitleEvent(eq(1))
        listener.onFail("error")
        verifyNoMoreInteractions(valueExtractor)
    }
}
