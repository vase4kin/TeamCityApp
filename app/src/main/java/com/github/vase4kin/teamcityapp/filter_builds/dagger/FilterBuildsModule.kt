package com.github.vase4kin.teamcityapp.filter_builds.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.filter_builds.presenter.FilterBuildsPresenterImpl
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouterImpl
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FilterBuildsTracker
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FirebaseFilterBuildsTrackerImpl
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsViewImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractorImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentViewImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named

@Module
@InstallIn(ActivityComponent::class)
object FilterBuildsModule {

    @Provides
    fun provideOwner(owner: Activity): FilterBuildsActivity = owner.requireScreenOwner<FilterBuildsActivity>()

    @Provides
    fun providesFilterBuildsView(activity: FilterBuildsActivity): FilterBuildsView {
        return FilterBuildsViewImpl(activity)
    }

    @Provides
    @Named("FilterBuildsActivity")
    fun providesBranchesComponentView(activity: FilterBuildsActivity): BranchesComponentView {
        return BranchesComponentViewImpl(activity)
    }

    @Provides
    @Named("FilterBuildsActivity")
    fun providesBranchesInteractor(repository: Repository, activity: FilterBuildsActivity): BranchesInteractor {
        return BranchesInteractorImpl(
            repository,
            activity.intent.getStringExtra(EXTRA_BUILD_TYPE_ID) ?: ""
        )
    }

    @Provides
    fun providesFilterBuildsRouter(activity: FilterBuildsActivity): FilterBuildsRouter {
        return FilterBuildsRouterImpl(activity)
    }

    @Provides
    fun providesFirebaseFilterBuildsTracker(firebaseAnalytics: FirebaseAnalytics): FilterBuildsTracker {
        return FirebaseFilterBuildsTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun provideFilterBuildsPresenterImpl(
        view: FilterBuildsView,
        router: FilterBuildsRouter,
        @Named("FilterBuildsActivity") branchesInteractor: BranchesInteractor,
        @Named("FilterBuildsActivity") branchesComponentView: BranchesComponentView,
        tracker: FilterBuildsTracker
    ): FilterBuildsPresenterImpl =
        FilterBuildsPresenterImpl(
            view,
            router,
            branchesInteractor,
            branchesComponentView,
            tracker
        )
}
