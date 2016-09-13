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

package com.github.vase4kin.teamcityapp.navigation.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode;

import java.util.List;

/**
 * Impl of {@link NavigationDataManager}
 */
public class NavigationDataManagerImpl extends BaseListRxDataManagerImpl<NavigationNode, NavigationItem> implements NavigationDataManager {

    private TeamCityService mTeamCityService;

    public NavigationDataManagerImpl(TeamCityService teamCityService) {
        this.mTeamCityService = teamCityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String url, @NonNull OnLoadingListener<List<NavigationItem>> loadingListener) {
        load(mTeamCityService.listBuildTypes(url), loadingListener);
    }
}
