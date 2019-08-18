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

package com.github.vase4kin.teamcityapp.drawer.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import com.github.vase4kin.teamcityapp.utils.any
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner
import org.powermock.api.mockito.PowerMockito.`when`

@RunWith(MockitoJUnitRunner::class)
class DrawerPresenterImplTest {

    @Captor
    private lateinit var integerLoadingListenerCaptor: ArgumentCaptor<OnLoadingListener<Int>>
    @Captor
    private lateinit var argumentCaptor: ArgumentCaptor<OnLoadingListener<List<UserAccount>>>
    @Mock
    private lateinit var dataManager: DrawerDataManager
    @Mock
    private lateinit var view: DrawerView
    @Mock
    private lateinit var router: DrawerRouter
    @Mock
    private lateinit var tracker: DrawerTracker
    private lateinit var presenter: DrawerPresenterImpl<*, *, *, *>

    @Before
    fun setUp() {
        presenter = DrawerPresenterImpl(view, dataManager, router, tracker)
    }

    @Test
    fun testHandleOnCreateIfModelIsEmpty() {
        `when`(view.isModelEmpty).thenReturn(true)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(dataManager).load(capture(argumentCaptor))
        val loadingListener = argumentCaptor.value
        loadingListener.onSuccess(emptyList())
        verify(view).showData(any())
        loadingListener.onFail("error")
        verify(view).isModelEmpty
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testHandleOnCreateIfModelIsNotEmpty() {
        `when`(view.isModelEmpty).thenReturn(false)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(dataManager).load(capture(argumentCaptor))
        val loadingListener = argumentCaptor.value
        loadingListener.onSuccess(emptyList())
        verify(view).showData(any())
        loadingListener.onFail("error")
        verify(view).isModelEmpty
        verify(dataManager).loadConnectedAgentsCount(capture(integerLoadingListenerCaptor))
        val agentsListener = integerLoadingListenerCaptor.allValues[0]
        agentsListener.onSuccess(67)
        verify(view).updateAgentsBadge(eq(67))
        agentsListener.onFail("error")
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testHandleOnBackButtonPressed() {
        presenter.onBackButtonPressed()
        verify(view).backButtonPressed()
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testSetActiveUser() {
        presenter.setActiveUser("email", "userName")
        verify(dataManager).setActiveUser(eq("email"), eq("userName"))
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testIsActiveProfile() {
        `when`(dataManager.isActiveUser("email", "userName")).thenReturn(true)
        assertThat(presenter.isActiveProfile("email", "userName"), `is`(equalTo(true)))
        verify(dataManager).isActiveUser(eq("email"), eq("userName"))
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testOnDrawerSlide() {
        presenter.onDrawerSlide()
        verify(dataManager).loadConnectedAgentsCount(any())
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testStartRootProjectsActivity() {
        presenter.startHomeActivity()
        verify(router).startHomeActivity()
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testStartAccountListActivity() {
        presenter.startAccountListActivity()
        verify(router).startAccountListActivity()
        verifyNoMoreInteractions(view, dataManager, router)
    }

    @Test
    fun testStartAgentActivity() {
        presenter.startAgentActivity()
        verify(router).startAgentActivity()
        verifyNoMoreInteractions(view, dataManager, router)
    }
}
