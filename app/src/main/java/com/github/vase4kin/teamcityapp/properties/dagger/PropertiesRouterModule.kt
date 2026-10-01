package com.github.vase4kin.teamcityapp.properties.dagger

import com.github.vase4kin.teamcityapp.properties.router.PropertiesRouterImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import teamcityapp.features.properties.feature.router.PropertiesRouter
import teamcityapp.features.properties.feature.view.PropertiesFragment

@Module
@InstallIn(FragmentComponent::class)
object PropertiesRouterModule {

    @Provides
    fun provideRouter(fragment: PropertiesFragment): PropertiesRouter {
        return PropertiesRouterImpl(fragment)
    }
}
