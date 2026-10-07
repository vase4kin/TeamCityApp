/*
 * Copyright 2026 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.home.data

import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.loading.OnLoadingListener
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import teamcityapp.libraries.cache_manager.CacheManager

class HomeDataManagerImplTest {
    @Before
    fun useSynchronousSchedulers() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun resetSchedulers() {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

    @Test
    fun disposingHomeCancelsThePreviousAccountAgentRequest() {
        val repository = mock(Repository::class.java)
        val disposed = AtomicBoolean(false)
        `when`(repository.listAgents(false, "count", null, false))
            .thenReturn(Single.never<Agents>().doOnDispose { disposed.set(true) })
        val manager = HomeDataManagerImpl(
            repository,
            mock(SharedUserStorage::class.java),
            mock(CacheManager::class.java),
            mock(EventBus::class.java)
        )

        @Suppress("UNCHECKED_CAST")
        val listener = mock(OnLoadingListener::class.java) as OnLoadingListener<Int>

        manager.loadAgentsCount(listener, false)
        manager.unsubscribe()

        assertTrue(disposed.get())
    }
}
