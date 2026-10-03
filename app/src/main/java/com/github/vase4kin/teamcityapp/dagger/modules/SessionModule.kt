package com.github.vase4kin.teamcityapp.dagger.modules

import android.app.Application
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatterImpl
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Resolves API dependencies from the current account graph when a new screen is injected. */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    @Provides
    fun repository(application: Application): Repository =
        (application as TeamCityApplicationBase).restApiInjector.repository()

    @Provides
    fun service(application: Application): TeamCityService =
        (application as TeamCityApplicationBase).restApiInjector.teamCityService()

    @Provides
    fun urlFormatter(): UrlFormatter = UrlFormatterImpl()
}
