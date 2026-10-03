package com.github.vase4kin.teamcityapp.runbuild.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractorImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorImpl
import com.github.vase4kin.teamcityapp.runbuild.presenter.RunBuildPresenterImpl
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouterImpl
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTrackerImpl
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentViewImpl
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildViewImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named

@Module
@InstallIn(ActivityComponent::class)
object RunBuildModule {

    @Provides
    fun provideOwner(owner: Activity): RunBuildActivity = owner.requireScreenOwner<RunBuildActivity>()

    @Provides
    fun providesRunBuildView(activity: RunBuildActivity): RunBuildView {
        return RunBuildViewImpl(activity)
    }

    @Provides
    @Named("RunBuildActivity")
    fun providesBranchesComponentView(activity: RunBuildActivity): BranchesComponentView {
        return BranchesComponentViewImpl(activity)
    }

    @Provides
    @Named("RunBuildActivity")
    fun providesRunBuildInteractor(activity: RunBuildActivity, repository: Repository): RunBuildInteractor {
        return RunBuildInteractorImpl(
            repository,
            activity.intent.getStringExtra(EXTRA_BUILD_TYPE_ID) ?: ""
        )
    }

    @Provides
    @Named("RunBuildActivity")
    fun providesBranchesInteractor(activity: RunBuildActivity, repository: Repository): BranchesInteractor {
        return BranchesInteractorImpl(
            repository,
            activity.intent.getStringExtra(EXTRA_BUILD_TYPE_ID) ?: ""
        )
    }

    @Provides
    fun providesRunBuildRouter(activity: RunBuildActivity): RunBuildRouter {
        return RunBuildRouterImpl(activity)
    }

    @Provides
    fun providesFirebaseRunBuildTracker(firebaseAnalytics: FirebaseAnalytics): RunBuildTracker {
        return RunBuildTrackerImpl(firebaseAnalytics)
    }

    @Provides
    fun provideRunBuildPresenterImpl(
        mView: RunBuildView,
        @Named("RunBuildActivity") mInteractor: RunBuildInteractor,
        mRouter: RunBuildRouter,
        mTracker: RunBuildTracker,
        @Named("RunBuildActivity") mBranchesComponentView: BranchesComponentView,
        @Named("RunBuildActivity") mBranchesInteractor: BranchesInteractor
    ): RunBuildPresenterImpl =
        RunBuildPresenterImpl(
            mView,
            mInteractor,
            mRouter,
            mTracker,
            mBranchesComponentView,
            mBranchesInteractor
        )
}
