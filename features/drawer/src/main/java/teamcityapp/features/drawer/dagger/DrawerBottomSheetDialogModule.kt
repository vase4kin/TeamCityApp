package teamcityapp.features.drawer.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import teamcityapp.features.drawer.drawer.DrawerAppRouter
import teamcityapp.features.drawer.drawer.DrawerRouter
import teamcityapp.features.drawer.drawer.DrawerRouterImpl
import teamcityapp.features.drawer.tracker.DrawerTracker
import teamcityapp.features.drawer.tracker.DrawerTrackerImpl
import teamcityapp.features.drawer.view.AccountViewHolderFactory
import teamcityapp.features.drawer.view.AccountsDividerViewHolderFactory
import teamcityapp.features.drawer.view.BaseDrawerItem
import teamcityapp.features.drawer.view.BaseDrawerViewHolderFactory
import teamcityapp.features.drawer.view.BottomViewHolderFactory
import teamcityapp.features.drawer.view.DividerViewHolderFactory
import teamcityapp.features.drawer.view.DrawerAdapter
import teamcityapp.features.drawer.view.DrawerBottomSheetDialogFragment
import teamcityapp.features.drawer.view.MenuViewHolderFactory
import teamcityapp.features.drawer.view.TYPE_ACCOUNT
import teamcityapp.features.drawer.view.TYPE_ACCOUNTS_DIVIDER
import teamcityapp.features.drawer.view.TYPE_BOTTOM
import teamcityapp.features.drawer.view.TYPE_DIVIDER
import teamcityapp.features.drawer.view.TYPE_MENU
import teamcityapp.features.drawer.stateholder.DrawerStateHolder
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.storage.Storage

@Module
@InstallIn(FragmentComponent::class)
object DrawerBottomSheetDialogModule {

    @Provides
    fun provideOwner(owner: Fragment): DrawerBottomSheetDialogFragment = owner.requireScreenOwner<DrawerBottomSheetDialogFragment>()

    @Provides
    @FragmentScoped
    fun providesStateHolder(
        fragment: DrawerBottomSheetDialogFragment,
        storage: Storage,
        @Named("HomeActivity") chromeCustomTabs: ChromeCustomTabs,
        tracker: DrawerTracker
    ): DrawerStateHolder {
        val setAdapter: (items: List<BaseDrawerItem>) -> Unit = {
            fragment.setAdapter(it)
        }
        return DrawerStateHolder(
            storage,
            chromeCustomTabs,
            setAdapter,
            tracker
        )
    }

    @Provides
    @FragmentScoped
    fun providesRouter(
        fragment: DrawerBottomSheetDialogFragment,
        storage: Storage,
        @Named("HomeActivity") chromeCustomTabs: ChromeCustomTabs,
        router: DrawerAppRouter
    ): DrawerRouter {
        return DrawerRouterImpl(
            fragment,
            storage,
            chromeCustomTabs,
            router
        )
    }

    @Provides
    fun providesAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards BaseDrawerViewHolderFactory>): DrawerAdapter {
        return DrawerAdapter(mutableListOf(), viewHolderFactories)
    }

    @Provides
    fun providesTracker(firebaseAnalytics: FirebaseAnalytics): DrawerTracker {
        return DrawerTrackerImpl(firebaseAnalytics)
    }

    @Provides
    @IntoMap
    @IntKey(TYPE_ACCOUNTS_DIVIDER)
    fun providesAccountsDividerViewHolderFactory(): BaseDrawerViewHolderFactory {
        return AccountsDividerViewHolderFactory()
    }

    @Provides
    @IntoMap
    @IntKey(TYPE_DIVIDER)
    fun providesDividerViewHolderFactory(): BaseDrawerViewHolderFactory {
        return DividerViewHolderFactory()
    }

    @Provides
    @IntoMap
    @IntKey(TYPE_BOTTOM)
    fun providesBottomViewHolderFactory(
        router: DrawerRouter,
        tracker: DrawerTracker
    ): BaseDrawerViewHolderFactory {
        return BottomViewHolderFactory(router, tracker)
    }

    @Provides
    @IntoMap
    @IntKey(TYPE_MENU)
    fun providesMenuViewHolderFactory(
        router: DrawerRouter,
        tracker: DrawerTracker
    ): BaseDrawerViewHolderFactory {
        return MenuViewHolderFactory(router, tracker)
    }

    @Provides
    @IntoMap
    @IntKey(TYPE_ACCOUNT)
    fun providesAccountViewHolderFactory(
        router: DrawerRouter,
        tracker: DrawerTracker
    ): BaseDrawerViewHolderFactory {
        return AccountViewHolderFactory(router, tracker)
    }
}
