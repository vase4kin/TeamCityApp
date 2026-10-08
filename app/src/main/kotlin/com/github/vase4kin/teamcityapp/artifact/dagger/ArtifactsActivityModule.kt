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

package com.github.vase4kin.teamcityapp.artifact.dagger

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManager
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManagerImpl
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractorImpl
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManager
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManagerImpl
import com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl
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
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import org.greenrobot.eventbus.EventBus
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(ActivityComponent::class)
object ArtifactsActivityModule {

    @Provides
    fun provideOwner(owner: Activity): ArtifactListActivity = owner.requireScreenOwner<ArtifactListActivity>()

    @Provides
    @Named("ArtifactListActivity")
    fun providesArtifactDataManager(
        activity: ArtifactListActivity,
        repository: Repository,
        eventBus: EventBus
    ): ArtifactDataManager {
        val downloadDirectory =
            activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: activity.filesDir
        return ArtifactDataManagerImpl(repository, eventBus, downloadDirectory)
    }

    @Provides
    @Named("ArtifactListActivity")
    fun providesArtifactView(
        activity: ArtifactListActivity,
        @Named("ArtifactListActivity") adapter: ArtifactAdapter
    ): ArtifactView = ArtifactViewImpl(
        activity.findViewById(android.R.id.content),
        activity,
        R.string.empty_list_message_artifacts,
        adapter
    )

    @Provides
    @Named("ArtifactListActivity")
    fun providesArtifactRouter(activity: ArtifactListActivity, sharedUserStorage: SharedUserStorage): ArtifactRouter = ArtifactRouterImpl(
        sharedUserStorage,
        activity,
        ChromeCustomTabsImpl(activity)
    )

    @Provides
    @Named("ArtifactListActivity")
    fun providesArtifactValueExtractor(activity: ArtifactListActivity): ArtifactValueExtractor = ArtifactValueExtractorImpl(activity.intent.extras ?: Bundle.EMPTY)

    @Provides
    @Named("ArtifactListActivity")
    fun providesViewTracker(): ViewTracker = ViewTracker.STUB

    @Provides
    @Named("ArtifactListActivity")
    fun providesPermissionManager(activity: ArtifactListActivity): PermissionManager = PermissionManagerImpl(activity)

    @Provides
    @Named("ArtifactListActivity")
    fun providesArtifactAdapter(@Named("ArtifactListActivity") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<ArtifactDataModel>>): ArtifactAdapter = ArtifactAdapter(viewHolderFactories)

    @Provides
    @Named("ArtifactListActivity")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesArtifactViewHolderFactory(): ViewHolderFactory<ArtifactDataModel> = ArtifactViewHolderFactory()

    @Provides
    @Named("ArtifactListActivity")
    fun provideArtifactPresenterImpl(
        @Named("ArtifactListActivity") view: ArtifactView,
        @Named("ArtifactListActivity") dataManager: ArtifactDataManager,
        @Named("ArtifactListActivity") tracker: ViewTracker,
        @Named("ArtifactListActivity") valueExtractor: ArtifactValueExtractor,
        @Named("ArtifactListActivity") router: ArtifactRouter,
        @Named("ArtifactListActivity") permissionManager: PermissionManager
    ): ArtifactPresenterImpl = ArtifactPresenterImpl(
        view,
        dataManager,
        tracker,
        valueExtractor,
        router,
        permissionManager
    )
}
