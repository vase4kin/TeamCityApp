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

package com.github.vase4kin.teamcityapp.filter_builds.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FilterBuildsTracker
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class FilterBuildsPresenterImplTest {

    @Captor
    private lateinit var loadingListenerCaptor: ArgumentCaptor<OnLoadingListener<List<String>>>
    @Captor
    private lateinit var buildListFilterCaptor: ArgumentCaptor<BuildListFilter>
    @Mock
    private lateinit var view: FilterBuildsView
    @Mock
    private lateinit var router: FilterBuildsRouter
    @Mock
    private lateinit var branchesInteractor: BranchesInteractor
    @Mock
    private lateinit var branchesComponentView: BranchesComponentView
    @Mock
    private lateinit var tracker: FilterBuildsTracker

    private lateinit var presenter: FilterBuildsPresenterImpl

    @Before
    fun setUp() {
        presenter = FilterBuildsPresenterImpl(view, router, branchesInteractor, branchesComponentView, tracker)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, router, branchesInteractor, branchesComponentView, tracker)
    }

    @Test
    fun onCreate() {
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(branchesComponentView).initViews()
        verify(branchesInteractor).loadBranches(capture(loadingListenerCaptor))
        val loadingListener = loadingListenerCaptor.value
        loadingListener.onSuccess(emptyList())
        verify(branchesComponentView).hideBranchesLoadingProgress()
        verify(branchesComponentView).showNoBranchesAvailableToFilter()
        loadingListener.onSuccess(listOf("branch"))
        verify(branchesComponentView, times(2)).hideBranchesLoadingProgress()
        verify(branchesComponentView, times(2)).showNoBranchesAvailableToFilter()
        val branches = ArrayList<String>()
        branches.add("1")
        branches.add("2")
        loadingListener.onSuccess(branches)
        verify(branchesComponentView, times(3)).hideBranchesLoadingProgress()
        verify(branchesComponentView).setupAutoComplete(branches)
        verify(branchesComponentView).setAutocompleteHintForFilter()
        verify(branchesComponentView).showBranchesAutoComplete()
        loadingListener.onFail("")
        verify(branchesComponentView, times(4)).hideBranchesLoadingProgress()
        verify(branchesComponentView).showNoBranchesAvailable()
    }

    @Test
    fun onResume() {
        presenter.onResume()
        verify(tracker).trackView()
    }

    @Test
    fun onDestroy() {
        presenter.onDestroy()
        verify(view).unbindViews()
        verify(branchesComponentView).unbindViews()
        verify(branchesInteractor).unsubscribe()
    }

    @Test
    fun onBackPressed() {
        presenter.onBackPressed()
        verify(router).closeOnBackButtonPressed()
    }

    @Test
    fun onClick() {
        presenter.onClick()
        verify(router).closeOnCancel()
    }

    @Test
    fun onFilterFabClick() {
        `when`(branchesComponentView.branchName).thenReturn("branch")
        presenter.onFilterFabClick(FilterBuildsView.FILTER_CANCELLED, true, true)
        verify(branchesComponentView).branchName
        verify(tracker).trackUserFilteredBuilds()
        verify(router).closeOnSuccess(capture(buildListFilterCaptor))
        val capturedFilter = buildListFilterCaptor.value
        assertThat(capturedFilter.toLocator(), `is`(equalTo("canceled:true,branch:name:branch,personal:true,pinned:true,count:10")))
    }

    @Test
    fun onQueuedFilterSelected() {
        presenter.onQueuedFilterSelected()
        verify(view).hideSwitchForPinnedFilter()
    }

    @Test
    fun onOtherFiltersSelected() {
        presenter.onOtherFiltersSelected()
        verify(view).showSwitchForPinnedFilter()
    }

}