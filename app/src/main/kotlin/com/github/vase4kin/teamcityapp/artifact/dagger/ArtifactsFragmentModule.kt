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

import teamcityapp.libraries.utils.requireScreenOwner
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment
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
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import org.greenrobot.eventbus.EventBus
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl

@Module
@InstallIn(FragmentComponent::class)
object ArtifactsFragmentModule {

    @Provides
    fun provideOwner(owner: Fragment): ArtifactListFragment = owner.requireScreenOwner<ArtifactListFragment>()

    @Provides
    @Named("ArtifactListFragment")
    fun providesArtifactDataManager(
        fragment: ArtifactListFragment,
        repository: Repository,
        eventBus: EventBus
    ): ArtifactDataManager {
        val context = fragment.requireContext()
        val downloadDirectory =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        return ArtifactDataManagerImpl(repository, eventBus, downloadDirectory)
    }

    @Provides
    @Named("ArtifactListFragment")
    fun providesArtifactView(
        fragment: ArtifactListFragment,
        @Named("ArtifactListFragment") adapter: ArtifactAdapter
    ): ArtifactView {
        return ArtifactViewImpl(
            fragment.requireView(),
            fragment.requireActivity(),
            R.string.empty_list_message_artifacts,
            adapter
        )
    }

    @Provides
    @Named("ArtifactListFragment")
    fun providesArtifactRouter(fragment: ArtifactListFragment, sharedUserStorage: SharedUserStorage): ArtifactRouter {
        return ArtifactRouterImpl(
            sharedUserStorage,
            fragment.requireActivity().requireScreenOwner<AppCompatActivity>(),
            ChromeCustomTabsImpl(fragment.requireActivity())
        )
    }

    @Provides
    @Named("ArtifactListFragment")
    fun providesArtifactValueExtractor(fragment: ArtifactListFragment): ArtifactValueExtractor {
        return ArtifactValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    @Named("ArtifactListFragment")
    fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @Provides
    @Named("ArtifactListFragment")
    fun providesPermissionManager(fragment: ArtifactListFragment): PermissionManager {
        return PermissionManagerImpl(fragment.requireActivity().requireScreenOwner<AppCompatActivity>())
    }

    @Provides
    @Named("ArtifactListFragment")
    fun providesArtifactAdapter(@Named("ArtifactListFragment") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<ArtifactDataModel>>): ArtifactAdapter {
        return ArtifactAdapter(viewHolderFactories)
    }

    @Provides
    @Named("ArtifactListFragment")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesArtifactViewHolderFactory(): ViewHolderFactory<ArtifactDataModel> {
        return ArtifactViewHolderFactory()
    }

    @Provides
    @Named("ArtifactListFragment")
    fun provideArtifactPresenterImpl(
        @Named("ArtifactListFragment") view: ArtifactView,
        @Named("ArtifactListFragment") dataManager: ArtifactDataManager,
        @Named("ArtifactListFragment") tracker: ViewTracker,
        @Named("ArtifactListFragment") valueExtractor: ArtifactValueExtractor,
        @Named("ArtifactListFragment") router: ArtifactRouter,
        @Named("ArtifactListFragment") permissionManager: PermissionManager
    ): ArtifactPresenterImpl =
        ArtifactPresenterImpl(
            view,
            dataManager,
            tracker,
            valueExtractor,
            router,
            permissionManager
        )
}
