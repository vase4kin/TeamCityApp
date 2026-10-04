package com.github.vase4kin.teamcityapp.properties.dagger

import com.github.vase4kin.teamcityapp.properties.router.PropertiesRouterImpl
import androidx.fragment.app.Fragment
import teamcityapp.libraries.utils.requireScreenOwner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import teamcityapp.features.properties.impl.router.PropertiesRouter
import teamcityapp.features.properties.impl.PropertiesFragment

@Module
@InstallIn(FragmentComponent::class)
object PropertiesRouterModule {

    @Provides
    fun provideRouter(fragment: Fragment): PropertiesRouter {
        return PropertiesRouterImpl(fragment.requireScreenOwner<PropertiesFragment>())
    }
}
