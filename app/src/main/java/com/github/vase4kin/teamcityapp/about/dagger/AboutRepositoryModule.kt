package com.github.vase4kin.teamcityapp.about.dagger

import com.github.vase4kin.teamcityapp.api.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.about.repository.AboutRepository

@Module
@InstallIn(ActivityComponent::class)
object AboutRepositoryModule {

    @Provides
    fun providesRepository(repository: Repository): AboutRepository = repository
}
