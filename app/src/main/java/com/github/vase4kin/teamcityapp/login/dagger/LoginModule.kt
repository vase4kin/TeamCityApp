package com.github.vase4kin.teamcityapp.login.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManagerImpl
import teamcityapp.libraries.remote.url.UrlFormatter
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.login.presenter.LoginPresenterImpl
import com.github.vase4kin.teamcityapp.login.router.LoginRouter
import com.github.vase4kin.teamcityapp.login.router.LoginRouterImpl
import com.github.vase4kin.teamcityapp.login.tracker.LoginTracker
import com.github.vase4kin.teamcityapp.login.tracker.LoginTrackerImpl
import com.github.vase4kin.teamcityapp.login.view.LoginActivity
import com.github.vase4kin.teamcityapp.login.view.LoginView
import com.github.vase4kin.teamcityapp.login.view.LoginViewImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named
import okhttp3.OkHttpClient
import teamcityapp.libraries.remote.RemoteService

@Module
@InstallIn(ActivityComponent::class)
object LoginModule {

    @Provides
    fun provideOwner(owner: Activity): LoginActivity = owner.requireScreenOwner<LoginActivity>()

    @Provides
    fun providesLoginView(activity: LoginActivity): LoginView {
        return LoginViewImpl(activity)
    }

    @Provides
    @Named("LoginActivity")
    fun providesCreateAccountDataManager(
        activity: LoginActivity,
        @Named(CLIENT_BASE) baseOkHttpClient: OkHttpClient,
        @Named(CLIENT_BASE_UNSAFE) unsafeBaseOkHttpClient: OkHttpClient,
        sharedUserStorage: SharedUserStorage,
        urlFormatter: UrlFormatter
    ): CreateAccountDataManager {
        return CreateAccountDataManagerImpl(
            activity, baseOkHttpClient, unsafeBaseOkHttpClient, sharedUserStorage, urlFormatter
        )
    }

    @Provides
    fun providesLoginRouter(activity: LoginActivity): LoginRouter {
        return LoginRouterImpl(activity)
    }

    @Provides
    fun providesFirebaseLoginTracker(firebaseAnalytics: FirebaseAnalytics): LoginTracker {
        return LoginTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun provideLoginPresenterImpl(
        view: LoginView,
        @Named("LoginActivity") dataManager: CreateAccountDataManager,
        router: LoginRouter,
        tracker: LoginTracker,
        remoteService: RemoteService
    ): LoginPresenterImpl = LoginPresenterImpl(view, dataManager, router, tracker, remoteService)
}
