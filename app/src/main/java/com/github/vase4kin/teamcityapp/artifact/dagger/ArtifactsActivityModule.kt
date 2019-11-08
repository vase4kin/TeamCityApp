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

package com.github.vase4kin.teamcityapp.artifact.dagger

import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManager
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManagerImpl
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractorImpl
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManager
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManagerImpl
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouter
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouterImpl
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactAdapter
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListActivity
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactView
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactViewHolderFactory
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactViewImpl
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus
import teamcityapp.chrome_tabs.ChromeCustomTabsImpl

@Module
class ArtifactsActivityModule {

    @Provides
    fun providesArtifactDataManager(
        repository: Repository,
        eventBus: EventBus
    ): ArtifactDataManager {
        return ArtifactDataManagerImpl(repository, eventBus)
    }

    @Provides
    fun providesArtifactView(
        activity: ArtifactListActivity,
        adapter: ArtifactAdapter
    ): ArtifactView {
        return ArtifactViewImpl(
            activity.findViewById(android.R.id.content),
            activity,
            R.string.empty_list_message_artifacts,
            adapter
        )
    }

    @Provides
    fun providesArtifactRouter(
        activity: ArtifactListActivity,
        sharedUserStorage: SharedUserStorage
    ): ArtifactRouter {
        return ArtifactRouterImpl(
            sharedUserStorage,
            activity,
            ChromeCustomTabsImpl(activity)
        )
    }

    @Provides
    fun providesArtifactValueExtractor(activity: ArtifactListActivity): ArtifactValueExtractor {
        return ArtifactValueExtractorImpl(activity.intent.extras ?: Bundle.EMPTY)
    }

    @Provides
    fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @Provides
    fun providesPermissionManager(activity: ArtifactListActivity): PermissionManager {
        return PermissionManagerImpl(activity)
    }

    @Provides
    fun providesArtifactAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<ArtifactDataModel>>): ArtifactAdapter {
        return ArtifactAdapter(viewHolderFactories)
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesArtifactViewHolderFactory(): ViewHolderFactory<ArtifactDataModel> {
        return ArtifactViewHolderFactory()
    }
}
