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

package com.github.vase4kin.teamcityapp.build_details.data

import android.view.View
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactErrorDownloadingEvent
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.overview.data.*
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BuildDetailsInteractorImplTest {

    @Mock
    lateinit var listener: OnBuildDetailsEventsListener
    @Mock
    lateinit var eventBus: EventBus
    @Mock
    lateinit var valueExtractor: BaseValueExtractor
    @Mock
    lateinit var sharedUserStorage: SharedUserStorage
    @Mock
    lateinit var repository: Repository
    lateinit var interactor: BuildDetailsInteractorImpl

    @Before
    fun setUp() {
        interactor = BuildDetailsInteractorImpl(eventBus, valueExtractor, sharedUserStorage, repository)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(eventBus, valueExtractor, sharedUserStorage, repository)
    }

    @Test
    fun onFloatButtonChangeVisibilityEventIfListenerIsNull() {
        interactor.setOnBuildTabsEventsListener(null)
        interactor.onEvent(FloatButtonChangeVisibilityEvent(View.VISIBLE))
    }

    @Test
    fun onFloatButtonChangeVisibilityEventOnShow() {
        interactor.setOnBuildTabsEventsListener(listener)
        interactor.onEvent(FloatButtonChangeVisibilityEvent(View.VISIBLE))
        verify(listener).onShow()
    }

    @Test
    fun onFloatButtonChangeVisibilityEventOnHide() {
        interactor.setOnBuildTabsEventsListener(listener)
        interactor.onEvent(FloatButtonChangeVisibilityEvent(View.GONE))
        verify(listener).onHide()
    }

    @Test
    fun onFloatButtonChangeVisibilityEventOnNothing() {
        interactor.setOnBuildTabsEventsListener(listener)
        interactor.onEvent(FloatButtonChangeVisibilityEvent(View.INVISIBLE))
    }

    @Test
    fun onEvent1() {
        interactor.onEvent(StopBuildEvent())
    }

    @Test
    fun onEvent2() {
        interactor.onEvent(ShareBuildEvent())
    }

    @Test
    fun onEvent3() {
        interactor.onEvent(RestartBuildEvent())
    }

    @Test
    fun onEvent4() {
        interactor.onEvent(TextCopiedEvent())
    }

    @Test
    fun onEvent5() {
        interactor.onEvent(ArtifactErrorDownloadingEvent())
    }

    @Test
    fun onEvent6() {
        interactor.onEvent(StartBuildsListActivityFilteredByBranchEvent(""))
    }
}
