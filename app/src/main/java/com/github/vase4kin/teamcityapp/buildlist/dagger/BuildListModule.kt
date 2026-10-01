package com.github.vase4kin.teamcityapp.buildlist.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouterImpl
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker
import com.github.vase4kin.teamcityapp.buildlist.tracker.FirebaseBuildListTrackerImpl
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListViewImpl
import com.github.vase4kin.teamcityapp.buildlist.view.BuildsViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.view.LoadMoreViewHolderFactory
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import teamcityapp.libraries.onboarding.OnboardingManager

@Module
@InstallIn(ActivityComponent::class)
object BuildListModule {

    @Provides
    fun provideOwner(owner: Activity): BuildListActivity = owner.requireScreenOwner<BuildListActivity>()

    @Provides
    fun providesBuildListDataManager(repository: Repository, storage: SharedUserStorage): BuildListDataManager {
        return BuildListDataManagerImpl(repository, storage)
    }

    @Provides
    fun providesBuildListView(
        activity: BuildListActivity,
        @Named("BuildListActivity") adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>
    ): BuildListView {
        return BuildListViewImpl(
            activity.findViewById(android.R.id.content),
            activity,
            R.string.empty_list_message_builds,
            adapter
        )
    }

    @Provides
    @Named("BuildListActivity")
    fun providesBuildListRouter(activity: BuildListActivity): BuildListRouter {
        return BuildListRouterImpl(activity)
    }

    @Provides
    @Named("BuildListActivity")
    fun providesBuildListValueExtractor(activity: BuildListActivity): BaseValueExtractor {
        return BaseValueExtractorImpl(activity.intent.extras ?: Bundle.EMPTY)
    }

    @Provides
    @Named("BuildListActivity")
    fun providesBuildInteractor(teamCityService: TeamCityService): BuildInteractor {
        return BuildInteractorImpl(teamCityService)
    }

    @Provides
    @Named("BuildListActivity")
    fun providesFirebaseBuildListTracker(firebaseAnalytics: FirebaseAnalytics): BuildListTracker {
        return FirebaseBuildListTrackerImpl(firebaseAnalytics, BuildListTracker.SCREEN_NAME_BUILD_LIST)
    }

    @Provides
    @Named("BuildListActivity")
    fun providesSimpleSectionedRecyclerViewAdapter(
        activity: BuildListActivity,
        @Named("BuildListActivity") adapter: BuildListAdapter
    ): SimpleSectionedRecyclerViewAdapter<BuildListAdapter> {
        return SimpleSectionedRecyclerViewAdapter(activity, adapter)
    }

    @Provides
    @Named("BuildListActivity")
    fun providesBuildListAdapter(@Named("BuildListActivity") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<BuildListDataModel>>): BuildListAdapter {
        return BuildListAdapter(viewHolderFactories)
    }

    @Provides
    @Named("BuildListActivity")
    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<BuildListDataModel> {
        return LoadMoreViewHolderFactory()
    }

    @Provides
    @Named("BuildListActivity")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesBuildViewHolderFactory(): ViewHolderFactory<BuildListDataModel> {
        return BuildsViewHolderFactory()
    }
    @Provides
    fun presenter(
        view: BuildListView,
        dataManager: BuildListDataManager,
        @Named("BuildListActivity") tracker: BuildListTracker,
        @Named("BuildListActivity") valueExtractor: BaseValueExtractor,
        @Named("BuildListActivity") router: BuildListRouter,
        @Named("BuildListActivity") buildInteractor: BuildInteractor,
        onboardingManager: OnboardingManager
    ): BuildListPresenterImpl<BuildListView, BuildListDataManager> =
        BuildListPresenterImpl(view, dataManager, tracker, valueExtractor, router, buildInteractor, onboardingManager)
}
