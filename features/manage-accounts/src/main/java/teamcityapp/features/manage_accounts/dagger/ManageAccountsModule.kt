package teamcityapp.features.manage_accounts.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import com.google.firebase.analytics.FirebaseAnalytics
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named
import teamcityapp.features.manage_accounts.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.tracker.ManageAccountsTracker
import teamcityapp.features.manage_accounts.tracker.ManageAccountsTrackerImpl
import teamcityapp.features.manage_accounts.view.AccountItemFactory
import teamcityapp.features.manage_accounts.view.AccountItemFactoryImpl
import teamcityapp.features.manage_accounts.view.ManageAccountsActivity
import teamcityapp.features.manage_accounts.stateholder.ManageAccountsStateHolder
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.storage.Storage

@Module
@InstallIn(ActivityComponent::class)
object ManageAccountsModule {

    @Provides
    fun provideOwner(owner: Activity): ManageAccountsActivity = owner.requireScreenOwner<ManageAccountsActivity>()

    @Provides
    fun providesViewFirebaseTracker(firebaseAnalytics: FirebaseAnalytics): ManageAccountsTracker {
        return ManageAccountsTrackerImpl(
            firebaseAnalytics
        )
    }

    @Provides
    @ActivityScoped
    fun providesStateHolder(
        storage: Storage,
        router: ManageAccountsRouter,
        tracker: ManageAccountsTracker,
        cacheManager: CacheManager,
        @Named("ManageAccountsActivity") adapter: GroupAdapter<GroupieViewHolder>,
        itemsFactory: AccountItemFactory
    ): ManageAccountsStateHolder {
        return ManageAccountsStateHolder(
            storage,
            router,
            tracker,
            cacheManager,
            itemsFactory,
            adapter
        )
    }

    @Provides
    @Named("ManageAccountsActivity")
    fun providesAdapter(): GroupAdapter<GroupieViewHolder> = GroupAdapter<GroupieViewHolder>()

    @Provides
    fun provideAccountItemFactory(
        tracker: ManageAccountsTracker,
        activity: ManageAccountsActivity
    ): AccountItemFactory {
        val showSslDisabledInfoDialog: () -> Unit = { activity.showSslDisabledInfoDialog() }
        val showRemoveAccountDialog: (onAccountRemove: () -> Unit) -> Unit = {
            activity.showRemoveAccountDialog(it)
        }
        return AccountItemFactoryImpl(
            tracker = tracker,
            showRemoveAccountDialog = showRemoveAccountDialog,
            showSslDisabledInfoDialog = showSslDisabledInfoDialog
        )
    }
}
