package com.github.vase4kin.teamcityapp.drawer.dagger

import com.github.vase4kin.teamcityapp.drawer.router.DrawerAppRouterImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import teamcityapp.features.about.api.navigation.AboutNavigation
import teamcityapp.features.drawer.drawer.DrawerAppRouter
import teamcityapp.features.drawer.view.DrawerBottomSheetDialogFragment
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsNavigation
import teamcityapp.features.settings.api.navigation.SettingsNavigation

@Module
@InstallIn(FragmentComponent::class)
object DrawerRouterModule {

    @Provides
    fun providesAppRouter(fragment: DrawerBottomSheetDialogFragment, aboutNavigation: AboutNavigation, settingsNavigation: SettingsNavigation, manageAccountsNavigation: ManageAccountsNavigation): DrawerAppRouter = DrawerAppRouterImpl(fragment, aboutNavigation, settingsNavigation, manageAccountsNavigation)
}
