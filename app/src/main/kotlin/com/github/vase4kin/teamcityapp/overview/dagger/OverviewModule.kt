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

package com.github.vase4kin.teamcityapp.overview.dagger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel
import com.github.vase4kin.teamcityapp.overview.data.OverviewInteractorImpl
import com.github.vase4kin.teamcityapp.overview.data.OverviewValueExtractor
import com.github.vase4kin.teamcityapp.overview.data.OverviewValueExtractorImpl
import com.github.vase4kin.teamcityapp.overview.presenter.OverviewPresenterImpl
import com.github.vase4kin.teamcityapp.overview.tracker.FirebaseOverviewTrackerImpl
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker
import com.github.vase4kin.teamcityapp.overview.view.OverviewAdapter
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment
import com.github.vase4kin.teamcityapp.overview.view.OverviewView
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewHolderFactory
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(FragmentComponent::class)
object OverviewModule {

    @Provides
    fun provideOwner(owner: Fragment): OverviewFragment = owner.requireScreenOwner<OverviewFragment>()

    @Provides
    fun providesOverViewDataManager(
        repository: Repository,
        eventBus: EventBus,
        valueExtractor: OverviewValueExtractor
    ): OverViewInteractor = OverviewInteractorImpl(repository, eventBus, valueExtractor)

    @Provides
    fun providesBaseValueExtractor(fragment: OverviewFragment): OverviewValueExtractor = OverviewValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)

    @Provides
    fun providesBaseListView(
        adapter: OverviewAdapter,
        fragment: OverviewFragment,
        featureNavigation: teamcityapp.features.bottom_sheet.api.BottomSheetNavigation
    ): OverviewView = OverviewViewImpl(fragment.requireView(), fragment.requireActivity().requireScreenOwner<AppCompatActivity>(), adapter, featureNavigation)

    @Provides
    fun providesOverviewAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<OverviewDataModel>>): OverviewAdapter = OverviewAdapter(viewHolderFactories)

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesOverviewViewHolderFactory(): ViewHolderFactory<OverviewDataModel> = OverviewViewHolderFactory()

    @Provides
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): OverviewTracker = FirebaseOverviewTrackerImpl(firebaseAnalytics)

    @Provides
    fun presenter(
        view: OverviewView,
        interactor: OverViewInteractor,
        tracker: OverviewTracker,
        onboardingManager: OnboardingManager
    ): OverviewPresenterImpl = OverviewPresenterImpl(view, interactor, tracker, onboardingManager)
}
