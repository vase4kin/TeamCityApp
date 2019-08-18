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

package com.github.vase4kin.teamcityapp.home.presenter

import com.github.vase4kin.teamcityapp.app_navigation.BottomNavigationView
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.home.data.HomeDataManager
import com.github.vase4kin.teamcityapp.home.router.HomeRouter
import com.github.vase4kin.teamcityapp.home.tracker.HomeTracker
import com.github.vase4kin.teamcityapp.home.view.HomeView
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import com.github.vase4kin.teamcityapp.utils.capture
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class HomePresenterImplTest {

    @Captor
    private lateinit var onPromptShownListenerArgumentCaptor: ArgumentCaptor<OnboardingManager.OnPromptShownListener>
    @Mock
    internal lateinit var view: HomeView
    @Mock
    internal lateinit var dataManager: HomeDataManager
    @Mock
    internal lateinit var router: HomeRouter
    @Mock
    internal lateinit var interactor: BuildLogInteractor
    @Mock
    internal lateinit var tracker: HomeTracker
    @Mock
    private lateinit var account: UserAccount
    @Mock
    private lateinit var onboardingManager: OnboardingManager
    @Mock
    private lateinit var bottomNavigationView: BottomNavigationView
    private val filterProvider = FilterProvider()
    private lateinit var presenter: HomePresenterImpl

    @Before
    fun setUp() {
        presenter = HomePresenterImpl(
            view,
            dataManager,
            tracker,
            router,
            interactor,
            onboardingManager,
            bottomNavigationView,
            filterProvider
        )
    }

    @Test
    fun testOnResume() {
        `when`(dataManager.activeUser).thenReturn(account)
        `when`(account.teamcityUrl).thenReturn("")
        presenter.onResume()
        verify(tracker).trackView()
    }

    @Test
    fun testPromptIfItIsShown() {
        `when`(dataManager.activeUser).thenReturn(account)
        `when`(account.teamcityUrl).thenReturn("")
        `when`(onboardingManager.isNavigationDrawerPromptShown).thenReturn(true)
        presenter.onResume()
        verify(onboardingManager).isNavigationDrawerPromptShown
    }

    @Test
    fun testPromptIfItIsNotShown() {
        `when`(dataManager.activeUser).thenReturn(account)
        `when`(account.teamcityUrl).thenReturn("")
        `when`(onboardingManager.isNavigationDrawerPromptShown).thenReturn(false)
        presenter.onResume()
        verify(onboardingManager).isNavigationDrawerPromptShown
        verify(view).showNavigationDrawerPrompt(capture(onPromptShownListenerArgumentCaptor))
        val listener = onPromptShownListenerArgumentCaptor.value
        listener.onPromptShown()
        verify(onboardingManager).saveNavigationDrawerPromptShown()
    }
}
