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

package com.github.vase4kin.teamcityapp.buildlist.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.Builds;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

import java.util.List;

/**
 * Data manager for {@link com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity}
 */
public interface BuildListDataManager extends BaseListRxDataManager<Builds, Build> {

    /**
     * Load more builds
     *
     * @param loadingListener - Listener to receive callbacks on {@link com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl}
     */
    void loadMore(@NonNull OnLoadingListener<List<BuildDetails>> loadingListener);

    /**
     * {@inheritDoc}
     */
    void load(@NonNull String id, @NonNull OnLoadingListener<List<BuildDetails>> loadingListener);

    /**
     * {@inheritDoc}
     */
    void load(@NonNull String id, BuildListFilter filter, @NonNull OnLoadingListener<List<BuildDetails>> loadingListener);

    /**
     * Is there any more builds to load
     */
    boolean canLoadMore();
}
