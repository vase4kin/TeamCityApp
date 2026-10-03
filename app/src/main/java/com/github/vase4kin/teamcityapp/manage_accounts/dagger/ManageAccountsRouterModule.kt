package com.github.vase4kin.teamcityapp.manage_accounts.dagger

import com.github.vase4kin.teamcityapp.manage_accounts.router.ManageAccountsRouterImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.manage_accounts.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.view.ManageAccountsActivity

@Module
@InstallIn(ActivityComponent::class)
object ManageAccountsRouterModule {

    @Provides
    fun provideAccountListRouter(activity: ManageAccountsActivity): ManageAccountsRouter =
        ManageAccountsRouterImpl(activity)
}
