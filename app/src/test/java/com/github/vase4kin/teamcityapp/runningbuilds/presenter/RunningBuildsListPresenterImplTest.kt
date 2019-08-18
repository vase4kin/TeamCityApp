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

package com.github.vase4kin.teamcityapp.runningbuilds.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView
import com.github.vase4kin.teamcityapp.utils.eq
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RunningBuildsListPresenterImplTest {

    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<BuildDetails>>
    @Mock
    private lateinit var view: RunningBuildListView
    @Mock
    private lateinit var dataManager: RunningBuildsDataManager
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
    @Mock
    private lateinit var eventBus: EventBus
    private val filterProvider = FilterProvider()
    private lateinit var mPresenter: RunningBuildsListPresenterImpl

    @Before
    fun setUp() {
        mPresenter = RunningBuildsListPresenterImpl(view, dataManager, tracker, router, valueExtractor, interactor, onboardingManager, filterProvider, eventBus)
    }

    @Test
    fun testLoadData() {
        filterProvider.runningBuildsFilter = Filter.RUNNING_ALL
        mPresenter.loadData(loadingListener, false)
        verify(dataManager).load(eq(loadingListener), eq(false))
        verifyNoMoreInteractions(view, dataManager, tracker, router, valueExtractor, interactor)
    }
}