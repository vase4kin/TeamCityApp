/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.build_details.dagger

import android.app.Activity
import android.view.View
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractor
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractorImpl
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenterImpl
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouter
import com.github.vase4kin.teamcityapp.build_details.router.BuildDetailsRouterImpl
import com.github.vase4kin.teamcityapp.build_details.tracker.BuildDetailsTracker
import com.github.vase4kin.teamcityapp.build_details.tracker.FirebaseBuildDetailsTrackerImpl
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsView
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsViewImpl
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named
import org.greenrobot.eventbus.EventBus
import teamcityapp.features.properties.api.PropertiesNavigation
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(ActivityComponent::class)
object BuildDetailsModule {

    @Provides
    fun provideOwner(owner: Activity): BuildDetailsActivity = owner.requireScreenOwner<BuildDetailsActivity>()

    @Provides
    fun providesBuildTabsView(
        activity: BuildDetailsActivity,
        @Named("BuildDetailsActivity") valueExtractor: BaseValueExtractor,
        propertiesNavigation: PropertiesNavigation
    ): BuildDetailsView = BuildDetailsViewImpl(
        activity.findViewById<View>(android.R.id.content),
        activity,
        valueExtractor,
        propertiesNavigation
    )

    @Provides
    @Named("BuildDetailsActivity")
    fun providesBuildTabsValueExtractor(activity: BuildDetailsActivity): BaseValueExtractor = BaseValueExtractorImpl(activity.intent.extras!!)

    @Provides
    fun providesBaseTabsDataManager(
        eventBus: EventBus,
        @Named("BuildDetailsActivity") valueExtractor: BaseValueExtractor,
        sharedUserStorage: SharedUserStorage,
        repository: Repository
    ): BuildDetailsInteractor = BuildDetailsInteractorImpl(eventBus, valueExtractor, sharedUserStorage, repository)

    @Provides
    fun providesBuildTabsRouter(activity: BuildDetailsActivity): BuildDetailsRouter = BuildDetailsRouterImpl(
        activity,
        ChromeCustomTabsImpl(activity)
    )

    @Provides
    @Named("BuildDetailsActivity")
    fun providesRunBuildInteractor(
        repository: Repository,
        @Named("BuildDetailsActivity") valueExtractor: BaseValueExtractor
    ): RunBuildInteractor = RunBuildInteractorImpl(repository, valueExtractor.buildDetails.buildTypeId)

    @Provides
    @Named("BuildDetailsActivity")
    fun providesBuildInteractor(teamCityService: TeamCityService): BuildInteractor = BuildInteractorImpl(teamCityService)

    @Provides
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): BuildDetailsTracker = FirebaseBuildDetailsTrackerImpl(firebaseAnalytics)

    @Provides
    fun provideBuildDetailsPresenterImpl(
        view: BuildDetailsView,
        tracker: BuildDetailsTracker,
        dataManager: BuildDetailsInteractor,
        router: BuildDetailsRouter,
        @Named("BuildDetailsActivity") runBuildInteractor: RunBuildInteractor,
        @Named("BuildDetailsActivity") buildInteractor: BuildInteractor
    ): BuildDetailsPresenterImpl = BuildDetailsPresenterImpl(
        view,
        tracker,
        dataManager,
        router,
        runBuildInteractor,
        buildInteractor
    )
}
