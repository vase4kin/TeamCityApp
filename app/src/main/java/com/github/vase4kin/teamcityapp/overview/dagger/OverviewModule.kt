/*
 * Copyright 2020 Andrey Tolpeev
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
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel
import com.github.vase4kin.teamcityapp.overview.data.OverviewInteractorImpl
import com.github.vase4kin.teamcityapp.overview.data.OverviewValueExtractor
import com.github.vase4kin.teamcityapp.overview.data.OverviewValueExtractorImpl
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
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus

@Module
object OverviewModule {

    @JvmStatic
    @Provides
    fun providesOverViewDataManager(
        repository: Repository,
        eventBus: EventBus,
        valueExtractor: OverviewValueExtractor
    ): OverViewInteractor {
        return OverviewInteractorImpl(repository, eventBus, valueExtractor)
    }

    @JvmStatic
    @Provides
    fun providesBaseValueExtractor(fragment: OverviewFragment): OverviewValueExtractor {
        return OverviewValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @JvmStatic
    @Provides
    fun providesBaseListView(adapter: OverviewAdapter, fragment: OverviewFragment): OverviewView {
        return OverviewViewImpl(fragment.view!!, fragment.activity as AppCompatActivity, adapter)
    }

    @JvmStatic
    @Provides
    fun providesOverviewAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<OverviewDataModel>>): OverviewAdapter {
        return OverviewAdapter(viewHolderFactories)
    }

    @JvmStatic
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesOverviewViewHolderFactory(): ViewHolderFactory<OverviewDataModel> {
        return OverviewViewHolderFactory()
    }

    @JvmStatic
    @Provides
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): OverviewTracker {
        return FirebaseOverviewTrackerImpl(firebaseAnalytics)
    }
}
