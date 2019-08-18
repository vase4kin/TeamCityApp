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

package com.github.vase4kin.teamcityapp.buildlist.presenter

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.utils.any
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BuildListPresenterImplTest {

    @Captor
    private lateinit var onLoadingListenerArgumentCaptor: ArgumentCaptor<OnLoadingListener<List<BuildDetails>>>
    @Captor
    private lateinit var buildArgumentCaptor: ArgumentCaptor<OnLoadingListener<Build>>
    @Captor
    private lateinit var onPromptShownListenerArgumentCaptor: ArgumentCaptor<OnboardingManager.OnPromptShownListener>
    @Mock
    private lateinit var filter: BuildListFilter
    @Mock
    private lateinit var menuItem: MenuItem
    @Mock
    private lateinit var menu: Menu
    @Mock
    private lateinit var menuInflater: MenuInflater
    @Mock
    private lateinit var build: Build
    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<BuildDetails>>
    @Mock
    private lateinit var view: BuildListView
    @Mock
    private lateinit var dataManager: BuildListDataManager
    @Mock
    private lateinit var tracker: BuildListTracker
    @Mock
    private lateinit var router: BuildListRouter
    @Mock
    private lateinit var valueExtractor: BaseValueExtractor
    @Mock
    private lateinit var interactor: BuildInteractor
    @Mock
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var presenter: BuildListPresenterImpl<*, *>

    @Before
    fun setUp() {
        presenter = BuildListPresenterImpl(view, dataManager, tracker, valueExtractor, router, interactor, onboardingManager)
    }

    @Test
    fun testLoadData() {
        `when`(valueExtractor.id).thenReturn("id")
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).id
        verify(valueExtractor).buildListFilter
        verify(dataManager).load(eq("id"), eq(loadingListener), eq(false))
        verifyNoMoreInteractions(view, dataManager, tracker, router, valueExtractor, interactor, onboardingManager)
    }

    @Test
    fun testLoadDataIfBuildListFilterIsProvided() {
        `when`(valueExtractor.id).thenReturn("id")
        `when`(valueExtractor.buildListFilter).thenReturn(filter)
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).id
        verify(valueExtractor).buildListFilter
        verify(dataManager).load(eq("id"), eq(filter), eq(loadingListener), eq(false))
        verifyNoMoreInteractions(view, dataManager, tracker, router, valueExtractor, interactor, onboardingManager)
    }

    @Test
    fun testInitViews() {
        `when`(valueExtractor.name).thenReturn("name")
        presenter.initViews()
        verify(view).setTitle(eq("name"))
    }

    @Test
    fun testOnBuildClick() {
        `when`(valueExtractor.name).thenReturn("name")
        presenter.onBuildClick(build)
        verify(valueExtractor).name
        verify(router).openBuildPage(eq(build), eq("name"))
    }

    @Test
    fun testOnBuildClickIfBundleIsNull() {
        `when`(valueExtractor.isBundleNullOrEmpty).thenReturn(true)
        presenter.onBuildClick(build)
        verify(valueExtractor).isBundleNullOrEmpty
        verify(router).openBuildPage(build, null)
    }

    @Test
    fun testOnRunBuildFabClick() {
        `when`(valueExtractor.id).thenReturn("id")
        presenter.onRunBuildFabClick()
        verify(router).openRunBuildPage(eq("id"))
        verify(tracker).trackRunBuildButtonPressed()
    }

    @Test
    fun testOnShowQueuedBuildSnackBarClick() {
        `when`(valueExtractor.name).thenReturn("name")
        presenter.mQueuedBuildHref = "href"
        presenter.onShowQueuedBuildSnackBarClick()
        verify(tracker).trackUserWantsToSeeQueuedBuildDetails()
        verify(view).showBuildLoadingProgress()
        verify(interactor).loadBuild(eq("href"), capture(buildArgumentCaptor))
        val listener = buildArgumentCaptor.value
        listener.onSuccess(build)
        verify(view).hideBuildLoadingProgress()
        verify(valueExtractor).name
        verify(router).openBuildPage(eq(build), eq("name"))
        listener.onFail("")
        verify(view, times(2)).hideBuildLoadingProgress()
        verify(view).showOpeningBuildErrorSnackBar()
    }

    @Test
    fun testOnLoadMore() {
        presenter.onLoadMore()
        assertThat(presenter.mIsLoadMoreLoading, `is`(true))
        verify(view).addLoadMore()
        verify(dataManager).loadMore(capture(onLoadingListenerArgumentCaptor))
        val loadingListener = onLoadingListenerArgumentCaptor.value

        loadingListener.onSuccess(emptyList())
        verify(view).removeLoadMore()
        verify(view).addMoreBuilds(any())
        assertThat(presenter.mIsLoadMoreLoading, `is`(false))

        loadingListener.onFail("error")
        verify(view, times(2)).removeLoadMore()
        verify(view).showRetryLoadMoreSnackBar()
        assertThat(presenter.mIsLoadMoreLoading, `is`(false))
    }

    @Test
    fun testHasLoadedAllItems() {
        `when`(dataManager.canLoadMore()).thenReturn(false)
        assertThat(presenter.hasLoadedAllItems(), `is`(true))
    }

    @Test
    fun testOnActivityResultIfResultIsOk() {
        `when`(valueExtractor.id).thenReturn("id")
        presenter.onRunBuildActivityResult("href")
        assertThat(presenter.mQueuedBuildHref, `is`(equalTo("href")))
        verify(view).showBuildQueuedSuccessSnackBar()
        verify(view).showRefreshAnimation()
        verify(view).hideErrorView()
        verify(view).hideEmpty()
        verify(valueExtractor).id
        verify(valueExtractor).buildListFilter
        verify(dataManager).load(eq("id"), any(), eq(true))
        verify(view).hideFiltersAppliedSnackBar()
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, interactor)
    }

    @Test
    fun testOnFilterBuildsResultIfResultIsOk() {
        `when`(valueExtractor.id).thenReturn("id")
        presenter.onFilterBuildsActivityResult(filter)
        verify(view).showBuildFilterAppliedSnackBar()
        verify(view).disableSwipeToRefresh()
        verify(view).showRefreshAnimation()
        verify(view).hideErrorView()
        verify(view).hideEmpty()
        verify(view).showData(any())
        verify(valueExtractor).id
        verify(dataManager).load(eq("id"), eq(filter), any(), eq(true))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, interactor)
    }

    @Test
    fun testOnResetFiltersSnackBarActionClick() {
        `when`(valueExtractor.id).thenReturn("id")
        presenter.onResetFiltersSnackBarActionClick()
        verify(view).disableSwipeToRefresh()
        verify(view).showRefreshAnimation()
        verify(view).hideErrorView()
        verify(view).hideEmpty()
        verify(view).showData(any())
        verify(valueExtractor).id
        verify(dataManager).load(eq("id"), any(), eq(true))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, interactor)
    }

    @Test
    fun testIsLoading() {
        presenter.mIsLoadMoreLoading = true
        assertThat(presenter.isLoading, `is`(equalTo(true)))
    }

    @Test
    fun testCreateModel() {
        val dataModel = presenter.createModel(mutableListOf())
        assertThat(dataModel.itemCount, `is`(0))
    }

    @Test
    fun testOnSuccessCallBack() {
        presenter.onSuccessCallBack(mutableListOf())
        verify(view).showRunBuildFloatActionButton()
    }

    @Test
    fun testOnFailCallBack() {
        presenter.onFailCallBack("")
        verify(view).hideRunBuildFloatActionButton()
    }

    @Test
    fun testOnFilterBuildsOptionMenuClick() {
        `when`(valueExtractor.id).thenReturn("id")
        presenter.onFilterBuildsOptionMenuClick()
        verify(valueExtractor).id
        verify(router).openFilterBuildsPage(eq("id"))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, interactor, onboardingManager)
    }

    @Test
    fun testOnCreateOptions() {
        `when`(valueExtractor.id).thenReturn("id")
        `when`(dataManager.hasBuildTypeAsFavorite(eq("id"))).thenReturn(true)
        presenter.onCreateOptionsMenu(menu, menuInflater)
        verify(valueExtractor).id
        verify(dataManager).hasBuildTypeAsFavorite(eq("id"))
        verify(view).createFavOptionsMenu(eq(menu), eq(menuInflater))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, interactor, onboardingManager)
    }

    @Test
    fun testOnPrepareOptionsMenu() {
        presenter.onPrepareOptionsMenu(menu)
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, interactor, onboardingManager)
    }

    @Test
    fun testOnOptionsItemSelected() {
        `when`(view.onOptionsItemSelected(menuItem)).thenReturn(true)
        assertThat(presenter.onOptionsItemSelected(menuItem), `is`(true))
        verify(view).onOptionsItemSelected(eq(menuItem))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router, interactor, onboardingManager)
    }

    @Test
    fun testRunBuildPromptIfItIsNotBuildListActivity() {
        `when`(view.isBuildListOpen).thenReturn(false)
        presenter.onResume()
        verify(view).isBuildListOpen
    }

    @Test
    fun testRunBuildPromptIfItIsShownAlready() {
        `when`(view.isBuildListOpen).thenReturn(true)
        `when`(onboardingManager.isRunBuildPromptShown).thenReturn(true)
        presenter.onResume()
        verify(view).isBuildListOpen
        verify(onboardingManager).isRunBuildPromptShown
    }

    @Test
    fun testRunBuildPromptIfItIsNotShownAlready() {
        `when`(view.isBuildListOpen).thenReturn(true)
        `when`(onboardingManager.isRunBuildPromptShown).thenReturn(false)
        presenter.onResume()
        verify(view).isBuildListOpen
        verify(onboardingManager).isRunBuildPromptShown
        verify(view).showRunBuildPrompt(capture(onPromptShownListenerArgumentCaptor))
        val runBuildPromptListener = onPromptShownListenerArgumentCaptor.value
        `when`(onboardingManager.isFilterBuildsPromptShown).thenReturn(true)
        runBuildPromptListener.onPromptShown()
        verify(onboardingManager).saveRunBuildPromptShown()
        verify(onboardingManager).isFilterBuildsPromptShown
        `when`(onboardingManager.isFilterBuildsPromptShown).thenReturn(false)
        runBuildPromptListener.onPromptShown()
        verify(onboardingManager, times(2)).saveRunBuildPromptShown()
        verify(onboardingManager, times(2)).isFilterBuildsPromptShown
        verify(view).showFilterBuildsPrompt(capture(onPromptShownListenerArgumentCaptor))
        val filterBuildsPromptListener = onPromptShownListenerArgumentCaptor.value
        filterBuildsPromptListener.onPromptShown()
        verify(onboardingManager).saveFilterBuildsPromptShown()
    }

    @Test
    fun testOnSwipeToRefresh() {
        presenter.onSwipeToRefresh()
        verify(view).hideFiltersAppliedSnackBar()
    }
}
