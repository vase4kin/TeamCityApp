package com.github.vase4kin.teamcityapp.buildlog.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractorImpl
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouterImpl
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogWebViewClient
import com.github.vase4kin.teamcityapp.buildlog.stateholder.BuildLogStateHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Named
import teamcityapp.libraries.storage.Storage

@Module
@InstallIn(FragmentComponent::class)
object BuildLogModule {

    @Provides
    fun provideOwner(owner: Fragment): BuildLogFragment = owner.requireScreenOwner<BuildLogFragment>()

    @Provides
    fun providesBuildLogRouter(fragment: BuildLogFragment): BuildLogRouter {
        return BuildLogRouterImpl(fragment.requireActivity())
    }

    @Provides
    @Named("BuildLogFragment")
    fun providesBuildLogInteractor(fragment: BuildLogFragment, storage: Storage): BuildLogInteractor {
        return BuildLogInteractorImpl(
            storage,
            fragment.requireContext(),
            fragment.arguments
        )
    }

    @Provides
    @FragmentScoped
    fun provideStateHolder(
        fragment: BuildLogFragment,
        buildLogUrlProvider: BuildLogUrlProvider,
        @Named("BuildLogFragment") interactor: BuildLogInteractor,
        router: BuildLogRouter
    ): BuildLogStateHolder {
        return BuildLogStateHolder(
            buildLogUrlProvider = buildLogUrlProvider,
            interactor = interactor,
            router = router,
            initWebView = { fragment.initWebView() },
            loadUrl = { fragment.loadUrl(it) }
        )
    }

    @Provides
    fun providesBuildLogWebViewClient(
        fragment: BuildLogFragment,
        stateHolder: BuildLogStateHolder
    ): BuildLogWebViewClient {
        return BuildLogWebViewClient(stateHolder) { fragment.evaluateJs(it) }
    }
}
