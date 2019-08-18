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

package com.github.vase4kin.teamcityapp.navigation.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NavigationPresenterImplTest {

    @Mock
    private lateinit var buildType: BuildType
    @Mock
    private lateinit var navigationItem: NavigationItem
    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<NavigationItem>>
    @Mock
    private lateinit var view: NavigationView
    @Mock
    private lateinit var dataManager: NavigationDataManager
    @Mock
    private lateinit var tracker: NavigationTracker
    @Mock
    private lateinit var valueExtractor: NavigationValueExtractor
    @Mock
    private lateinit var router: NavigationRouter
    private lateinit var presenter: NavigationPresenterImpl

    @Before
    fun setUp() {
        presenter = NavigationPresenterImpl(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testLoadData() {
        `when`(valueExtractor.id).thenReturn("url")
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).id
        verify(dataManager).load(eq("url"), eq(false), eq(loadingListener))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testInitViews() {
        `when`(valueExtractor.name).thenReturn("name")
        presenter.initViews()
        verify(view).setTitle(eq("name"))
        verify(view).setNavigationAdapterClickListener(eq(presenter))
    }

    @Test
    fun testCreateModel() {
        val items = emptyList<NavigationItem>()
        assertThat(presenter.createModel(items.toMutableList()).itemCount, `is`(0))
    }

    @Test
    fun testOnClickIfBuildType() {
        `when`(buildType.name).thenReturn("name")
        `when`(buildType.getId()).thenReturn("id")
        presenter.onClick(buildType)
        verify(router).startBuildListActivity(eq("name"), eq("id"))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }

    @Test
    fun testOnClickIfNotBuildType() {
        `when`(navigationItem.name).thenReturn("name")
        `when`(navigationItem.getId()).thenReturn("id")
        presenter.onClick(navigationItem)
        verify(router).startNavigationActivity(eq("name"), eq("id"))
        verifyNoMoreInteractions(view, dataManager, tracker, valueExtractor, router)
    }
}
