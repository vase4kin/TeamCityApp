package com.github.vase4kin.teamcityapp.splash.dagger

import com.github.vase4kin.teamcityapp.splash.router.SplashRouterImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.splash.router.SplashRouter
import teamcityapp.features.splash.view.SplashActivity

@Module
@InstallIn(ActivityComponent::class)
object SplashRouterModule {

    @Provides
    fun providesSplashRouter(activity: SplashActivity): SplashRouter = SplashRouterImpl(activity)
}
