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

package com.github.vase4kin.teamcityapp.buildlog.dagger;

import android.support.v4.app.Fragment;
import android.view.View;

import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractor;
import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractorImpl;
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter;
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouterImpl;
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider;
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProviderImpl;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogViewImpl;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogWebViewClient;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import dagger.Module;
import dagger.Provides;

@Module
public class BuildLogModule {

    private View mView;
    private Fragment mFragment;

    public BuildLogModule(View mView, Fragment mFragment) {
        this.mView = mView;
        this.mFragment = mFragment;
    }

    @Provides
    BuildLogView providesBuildLogViewModel(BuildLogWebViewClient client) {
        return new BuildLogViewImpl(mView, client);
    }

    @Provides
    BuildLogValueExtractor providesBuildLogValueExtractor() {
        return new BuildLogValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    BuildLogUrlProvider providesUrlProvider(BuildLogValueExtractor buildLogValueExtractor,
                                            SharedUserStorage sharedUserStorage) {
        return new BuildLogUrlProviderImpl(buildLogValueExtractor, sharedUserStorage.getActiveUser());
    }

    @Provides
    BuildLogRouter providesBuildLogRouter() {
        return new BuildLogRouterImpl(mFragment.getActivity());
    }
}
