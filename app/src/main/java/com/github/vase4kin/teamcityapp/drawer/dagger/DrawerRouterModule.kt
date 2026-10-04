package com.github.vase4kin.teamcityapp.drawer.dagger

import com.github.vase4kin.teamcityapp.drawer.router.DrawerAppRouterImpl
import teamcityapp.features.about.api.navigation.AboutNavigation
import teamcityapp.features.settings.api.navigation.SettingsNavigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import teamcityapp.features.drawer.drawer.DrawerAppRouter
import teamcityapp.features.drawer.view.DrawerBottomSheetDialogFragment

@Module
@InstallIn(FragmentComponent::class)
object DrawerRouterModule {

    @Provides
    fun providesAppRouter(fragment: DrawerBottomSheetDialogFragment, aboutNavigation: AboutNavigation, settingsNavigation: SettingsNavigation): DrawerAppRouter {
        return DrawerAppRouterImpl(fragment, aboutNavigation, settingsNavigation)
    }
}
