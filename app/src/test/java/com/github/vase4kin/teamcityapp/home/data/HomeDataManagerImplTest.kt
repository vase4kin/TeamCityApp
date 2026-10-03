package com.github.vase4kin.teamcityapp.home.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agents
import com.github.vase4kin.teamcityapp.api.Repository
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
        val manager = HomeDataManagerImpl(repository, mock(SharedUserStorage::class.java),
            mock(CacheManager::class.java), mock(EventBus::class.java))
        @Suppress("UNCHECKED_CAST")
        val listener = mock(OnLoadingListener::class.java) as OnLoadingListener<Int>

        manager.loadAgentsCount(listener, false)
        manager.unsubscribe()

        assertTrue(disposed.get())
    }
}
