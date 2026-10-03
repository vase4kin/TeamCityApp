package teamcityapp.features.splash.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.splash.presenter.SplashPresenterImpl
import teamcityapp.features.splash.router.SplashRouter
import teamcityapp.features.splash.view.SplashActivity
import teamcityapp.libraries.storage.Storage

@Module
@InstallIn(ActivityComponent::class)
object SplashActivityModule {

    @Provides
    fun provideOwner(owner: Activity): SplashActivity = owner.requireScreenOwner<SplashActivity>()

    @Provides
    fun provideSplashPresenterImpl(
        router: SplashRouter,
        storage: Storage
    ): SplashPresenterImpl = SplashPresenterImpl(router, storage)
}
