package teamcityapp.features.about.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import javax.inject.Named
import teamcityapp.features.about.AboutFragment
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl

@Module
@InstallIn(FragmentComponent::class)
object AboutFragmentModule {

    @Provides
    fun provideOwner(owner: Fragment): AboutFragment = owner.requireScreenOwner<AboutFragment>()

    @Provides
    @Named("AboutFragment")
    fun provideChromeTabs(fragment: AboutFragment): ChromeCustomTabs =
        ChromeCustomTabsImpl(fragment.requireActivity())
}
