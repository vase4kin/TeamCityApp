package teamcityapp.features.change.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import com.google.firebase.analytics.FirebaseAnalytics
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Named
import teamcityapp.features.change.router.ChangeRouter
import teamcityapp.features.change.router.ChangeRouterImpl
import teamcityapp.features.change.tracker.ChangeTracker
import teamcityapp.features.change.tracker.ChangeTrackerImpl
import teamcityapp.features.change.view.ARG_BUNDLE_DATA
import teamcityapp.features.change.view.ChangeActivity
import teamcityapp.features.change.view.ChangeItemsFactory
import teamcityapp.features.change.view.ChangeItemsFactoryImpl
import teamcityapp.features.change.stateholder.ChangeStateHolder
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.storage.Storage

@Module
@InstallIn(ActivityComponent::class)
object ChangeActivityModule {

    @Provides
    fun provideOwner(owner: Activity): ChangeActivity = owner.requireScreenOwner<ChangeActivity>()

    @Provides
    @Named("ChangeActivity")
    @ActivityScoped
    fun provideChromeTabs(activity: ChangeActivity): ChromeCustomTabs =
        ChromeCustomTabsImpl(activity)

    @Provides
    fun provideRouter(@Named("ChangeActivity") chromeCustomTabs: ChromeCustomTabs, storage: Storage): ChangeRouter {
        return ChangeRouterImpl(chromeCustomTabs, storage)
    }

    @Provides
    @ActivityScoped
    fun provideStateHolder(
        activity: ChangeActivity,
        router: ChangeRouter,
        @Named("ChangeActivity") adapter: GroupAdapter<GroupieViewHolder>,
        itemsFactory: ChangeItemsFactory,
        tracker: ChangeTracker
    ): ChangeStateHolder {
        return ChangeStateHolder(
            bundleData = activity.intent.getParcelableExtra(ARG_BUNDLE_DATA),
            router = router,
            adapter = adapter,
            finish = { activity.finish() },
            itemsFactory = itemsFactory,
            tracker = tracker
        )
    }

    @Provides
    @Named("ChangeActivity")
    fun provideAdapter(): GroupAdapter<GroupieViewHolder> {
        return GroupAdapter<GroupieViewHolder>()
    }

    @Provides
    fun provideItemsFactory(
        router: ChangeRouter,
        tracker: ChangeTracker
    ): ChangeItemsFactory {
        return ChangeItemsFactoryImpl(router, tracker)
    }

    @Provides
    @ActivityScoped
    fun provideTracker(firebaseAnalytics: FirebaseAnalytics): ChangeTracker {
        return ChangeTrackerImpl(firebaseAnalytics)
    }
}
