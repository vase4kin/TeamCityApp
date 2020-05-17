/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.buildlog.presenter

import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView
import com.github.vase4kin.teamcityapp.utils.eq
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BuildLogViewModelTest {

    @Mock
    private lateinit var view: BuildLogView
    @Mock
    private lateinit var buildLogUrlProvider: BuildLogUrlProvider
    @Mock
    private lateinit var interactor: BuildLogInteractor
    @Mock
    private lateinit var router: BuildLogRouter
    private lateinit var presenter: BuildLogViewModel

    @Before
    fun setUp() {
        `when`(buildLogUrlProvider.provideUrl()).thenReturn("http://fake-teamcity-url")
        presenter = BuildLogViewModel(view, buildLogUrlProvider, interactor, router)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, buildLogUrlProvider, interactor, router)
    }

    @Test
    fun testHandleOnCreateViewIfDialogIsNotShown() {
        `when`(interactor.isSslDisabled).thenReturn(false)
        `when`(interactor.isAuthDialogShown).thenReturn(true)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(interactor).isSslDisabled
        verify(interactor).isAuthDialogShown
        verify(buildLogUrlProvider).provideUrl()
        verify(view).loadBuildLog(eq("http://fake-teamcity-url"))
    }

    @Test
    fun testHandleOnCreateViewIfSslIsDisabled() {
        `when`(interactor.isSslDisabled).thenReturn(true)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(interactor).isSslDisabled
        verify(view).showSslWarningView()
    }

    @Test
    fun testHandleOnCreateViewIfGuestUser() {
        `when`(interactor.isAuthDialogShown).thenReturn(false)
        `when`(interactor.isSslDisabled).thenReturn(false)
        `when`(interactor.isGuestUser).thenReturn(true)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(interactor).isSslDisabled
        verify(interactor).isAuthDialogShown
        verify(interactor).isGuestUser
        verify(buildLogUrlProvider).provideUrl()
        verify(view).loadBuildLog(eq("http://fake-teamcity-url"))
    }

    @Test
    fun testHandleOnCreateViewIfNotGuest() {
        `when`(interactor.isGuestUser).thenReturn(false)
        `when`(interactor.isSslDisabled).thenReturn(false)
        `when`(interactor.isAuthDialogShown).thenReturn(false)
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(interactor).isSslDisabled
        verify(interactor).isGuestUser
        verify(interactor).isAuthDialogShown
        verify(view).showAuthView()
    }

    @Test
    fun testHandleOnDestroyView() {
        presenter.onDestroy()
        verify(view).unBindViews()
        verify(router).unbindCustomsTabs()
    }

    @Test
    fun testLoadBuildLog() {
        presenter.loadBuildLog()
        verify(buildLogUrlProvider).provideUrl()
        verify(view).loadBuildLog(eq("http://fake-teamcity-url"))
    }

    @Test
    fun testOpenBuildLog() {
        presenter.onOpenBuildLogInBrowser()
        verify(buildLogUrlProvider).provideUrl()
        verify(router).openUrl(eq("http://fake-teamcity-url"))
    }

    @Test
    fun testOnAuthButtonClick() {
        presenter.onAuthButtonClick()
        verify(view).hideAuthView()
        verify(interactor).setAuthDialogStatus(eq(true))
        verify(buildLogUrlProvider).provideUrl()
        verify(view).loadBuildLog(eq("http://fake-teamcity-url"))
    }
}
