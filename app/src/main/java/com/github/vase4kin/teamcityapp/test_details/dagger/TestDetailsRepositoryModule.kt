package com.github.vase4kin.teamcityapp.test_details.dagger

import com.github.vase4kin.teamcityapp.api.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.test_details.repository.TestDetails
import teamcityapp.features.test_details.repository.TestDetailsRepository
import javax.inject.Inject
import javax.inject.Provider

class TestDetailsDispatchers @Inject constructor() {
    val io: CoroutineDispatcher get() = Dispatchers.IO
}

/** Resolves the active account on every request and cancels Rx work with the coroutine. */
class RxTestDetailsRepository(
    private val repository: Provider<Repository>,
    private val io: CoroutineDispatcher
) : TestDetailsRepository {
    override suspend fun details(url: String): TestDetails = withContext(io) {
        TestDetails(repository.get().testOccurrence(url).await().details.orEmpty())
    }
}

@Module
@InstallIn(SingletonComponent::class)
object TestDetailsRepositoryModule {
    @Provides
    fun providesTestDetailsRepository(
        repository: Provider<Repository>,
        dispatchers: TestDetailsDispatchers
    ): TestDetailsRepository = RxTestDetailsRepository(repository, dispatchers.io)
}
