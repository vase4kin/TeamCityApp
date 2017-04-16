/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.artifact.dagger;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManager;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManagerImpl;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor;
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractorImpl;
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManager;
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManagerImpl;
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouter;
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouterImpl;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactAdapter;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactView;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactViewHolderFactory;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;
import de.greenrobot.event.EventBus;

@Module
public class ArtifactsModule {

    private View mView;
    private Fragment mFragment;

    public ArtifactsModule(View mView, Fragment fragment) {
        this.mView = mView;
        this.mFragment = fragment;
    }

    @Provides
    ArtifactDataManager providesArtifactDataManager(Repository repository, EventBus eventBus) {
        return new ArtifactDataManagerImpl(repository, eventBus);
    }

    @Provides
    ArtifactView providesArtifactView(ArtifactAdapter adapter) {
        return new ArtifactViewImpl(mView, mFragment.getActivity(), R.string.empty_list_message_artifacts, adapter);
    }

    @Provides
    ArtifactRouter providesArtifactRouter(SharedUserStorage sharedUserStorage) {
        return new ArtifactRouterImpl(sharedUserStorage, (AppCompatActivity) mFragment.getActivity());
    }

    @Provides
    ArtifactValueExtractor providesArtifactValueExtractor() {
        return new ArtifactValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    ViewTracker providesViewTracker() {
        return ViewTracker.STUB;
    }

    @Provides
    PermissionManager providesPermissionManager() {
        return new PermissionManagerImpl(mFragment);
    }

    @Provides
    ArtifactAdapter providesArtifactAdapter(Map<Integer, ViewHolderFactory<ArtifactDataModel>> viewHolderFactories) {
        return new ArtifactAdapter(viewHolderFactories);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<ArtifactDataModel> providesArtifactViewHolderFactory() {
        return new ArtifactViewHolderFactory();
    }
}
