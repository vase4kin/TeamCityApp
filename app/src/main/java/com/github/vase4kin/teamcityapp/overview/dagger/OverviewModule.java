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

package com.github.vase4kin.teamcityapp.overview.dagger;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.overview.data.OverViewDataManager;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataManagerImpl;
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class OverviewModule {

    private View mView;
    private Fragment mFragment;

    public OverviewModule(View mView, Fragment mFragment) {
        this.mView = mView;
        this.mFragment = mFragment;
    }

    @Provides
    OverViewDataManager providesOverViewDataManager(TeamCityService teamCityService, Context context) {
        return new OverviewDataManagerImpl(teamCityService, context);
    }

    @Provides
    BaseValueExtractor providesBaseValueExtractor() {
        return new BaseValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    BaseListView providesBaseListView() {
        return new OverviewViewImpl(mView, mFragment.getActivity(), R.string.empty);
    }
}
