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

package teamcityapp.features.splash.presenter

import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import teamcityapp.features.splash.router.SplashRouter
import teamcityapp.libraries.storage.Storage

class SplashPresenterImplTest {

    private val router: SplashRouter = mock()
    private val storage: Storage = mock()
    private lateinit var presenter: SplashPresenterImpl

    @Before
    fun setUp() {
        presenter = SplashPresenterImpl(
            router,
            storage
        )
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(storage, router)
    }

    @Test
    fun testHandleOnCreateIfThereAreAccounts() {
        `when`(storage.hasUserAccounts()).thenReturn(true)
        presenter.onCreate()
        verify(storage).hasUserAccounts()
        verify(router).openProjectsRootPage()
        verify(router).close()
    }

    @Test
    fun testHandleOnCreateIfThereAreNoAccounts() {
        `when`(storage.hasUserAccounts()).thenReturn(false)
        presenter.onCreate()
        verify(storage).hasUserAccounts()
        verify(router).openLoginPage()
        verify(router).close()
    }
}
