/*
 * Copyright 2019 Andrey Tolpeev
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

import android.view.View
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractor
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsInteractorImpl
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
import org.greenrobot.eventbus.EventBus
import teamcityapp.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.utils.StatusBarUtils

@Module
class BuildDetailsModule {

    @Provides
    fun providesBuildTabsView(
        activity: BuildDetailsActivity,
        statusBarUtils: StatusBarUtils,
        valueExtractor: BaseValueExtractor
    ): BuildDetailsView {
        return BuildDetailsViewImpl(
            activity.findViewById<View>(android.R.id.content),
            activity,
            statusBarUtils,
            valueExtractor
        )
    }

    @Provides
    fun providesBuildTabsValueExtractor(activity: BuildDetailsActivity): BaseValueExtractor {
        return BaseValueExtractorImpl(activity.intent.extras!!)
    }

    @Provides
    fun providesBaseTabsDataManager(
        eventBus: EventBus,
        valueExtractor: BaseValueExtractor,
        sharedUserStorage: SharedUserStorage,
        repository: Repository
    ): BuildDetailsInteractor {
        return BuildDetailsInteractorImpl(eventBus, valueExtractor, sharedUserStorage, repository)
    }

    @Provides
    fun providesBuildTabsRouter(activity: BuildDetailsActivity): BuildDetailsRouter {
        return BuildDetailsRouterImpl(
            activity,
            ChromeCustomTabsImpl(activity)
        )
    }

    @Provides
    fun providesRunBuildInteractor(
        repository: Repository,
        valueExtractor: BaseValueExtractor
    ): RunBuildInteractor {
        return RunBuildInteractorImpl(repository, valueExtractor.buildDetails.buildTypeId)
    }

    @Provides
    fun providesBuildInteractor(teamCityService: TeamCityService): BuildInteractor {
        return BuildInteractorImpl(teamCityService)
    }

    @Provides
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): BuildDetailsTracker {
        return FirebaseBuildDetailsTrackerImpl(firebaseAnalytics)
    }
}
