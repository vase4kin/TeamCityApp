package com.github.vase4kin.teamcityapp.test_details.dagger

import com.github.vase4kin.teamcityapp.api.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.test_details.repository.TestDetailsRepository

@Module
@InstallIn(ActivityComponent::class)
object TestDetailsRepositoryModule {

    @Provides
    fun providesTestDetailsRepository(repository: Repository): TestDetailsRepository = repository
}
