package com.github.vase4kin.teamcityapp.account.create.dagger

import android.app.Activity
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManagerImpl
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataModel
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataModelImpl
import com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouter
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouterImpl
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTracker
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTrackerImpl
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountView
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountViewImpl
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named
import okhttp3.OkHttpClient
import teamcityapp.libraries.remote.url.UrlFormatter
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(ActivityComponent::class)
object CreateAccountModule {

    @Provides
    fun provideOwner(owner: Activity): CreateAccountActivity = owner.requireScreenOwner<CreateAccountActivity>()

    @Provides
    fun providesCreateAccountView(activity: CreateAccountActivity): CreateAccountView = CreateAccountViewImpl(activity)

    @Provides
    @Named("CreateAccountActivity")
    fun providesCreateAccountDataManager(
        activity: CreateAccountActivity,
        @Named(CLIENT_BASE) okHttpClient: OkHttpClient,
        @Named(CLIENT_BASE_UNSAFE) unsafeOkHttpClient: OkHttpClient,
        sharedUserStorage: SharedUserStorage,
        urlFormatter: UrlFormatter
    ): CreateAccountDataManager = CreateAccountDataManagerImpl(activity, okHttpClient, unsafeOkHttpClient, sharedUserStorage, urlFormatter)

    @Provides
    fun providesCreateAccountDataModel(sharedUserStorage: SharedUserStorage): CreateAccountDataModel = CreateAccountDataModelImpl(sharedUserStorage)

    @Provides
    fun providesCreateAccountRouter(activity: CreateAccountActivity): CreateAccountRouter = CreateAccountRouterImpl(activity)

    @Provides
    fun providesFirebaseCreateAccountTracker(firebaseAnalytics: FirebaseAnalytics): CreateAccountTracker = CreateAccountTrackerImpl(firebaseAnalytics)

    @Provides
    fun provideCreateAccountPresenterImpl(
        view: CreateAccountView,
        @Named("CreateAccountActivity") dataManager: CreateAccountDataManager,
        dataModel: CreateAccountDataModel,
        router: CreateAccountRouter,
        tracker: CreateAccountTracker
    ): CreateAccountPresenterImpl = CreateAccountPresenterImpl(view, dataManager, dataModel, router, tracker)
}
