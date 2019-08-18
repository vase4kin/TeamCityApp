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

package com.github.vase4kin.teamcityapp.splash.presenter

import com.github.vase4kin.teamcityapp.splash.data.SplashDataManager
import com.github.vase4kin.teamcityapp.splash.router.SplashRouter
import com.github.vase4kin.teamcityapp.splash.view.SplashView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SplashPresenterImplTest {

    @Mock
    private lateinit var router: SplashRouter
    @Mock
    private lateinit var dataManager: SplashDataManager
    @Mock
    private lateinit var viewModel: SplashView
    private lateinit var presenter: SplashPresenterImpl

    @Before
    fun setUp() {
        presenter = SplashPresenterImpl(router, dataManager, viewModel)
    }

    @Test
    fun testHandleOnCreateIfThereAreAccounts() {
        `when`(dataManager.hasUserAccounts()).thenReturn(true)
        presenter.onCreate()
        verify(dataManager).hasUserAccounts()
        verify(router).openProjectsRootPage()
        verify(viewModel).close()
        verifyNoMoreInteractions(dataManager, router, viewModel)
    }

    @Test
    fun testHandleOnCreateIfThereAreNoAccounts() {
        `when`(dataManager.hasUserAccounts()).thenReturn(false)
        presenter.onCreate()
        verify(dataManager).hasUserAccounts()
        verify(router).openLoginPage()
        verify(viewModel).close()
        verifyNoMoreInteractions(dataManager, router, viewModel)
    }
}