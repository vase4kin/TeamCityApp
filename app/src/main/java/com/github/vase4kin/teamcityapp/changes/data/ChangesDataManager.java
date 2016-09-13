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

package com.github.vase4kin.teamcityapp.changes.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManager;
import com.github.vase4kin.teamcityapp.changes.api.Changes;

import java.util.List;

/**
 * Data manager for {@link com.github.vase4kin.teamcityapp.changes.view.ChangesFragment}
 */
public interface ChangesDataManager extends BaseListRxDataManager<Changes, Changes.Change> {

    /**
     * Load tab title with number of changes
     *
     * @param url             - Url to load
     * @param loadingListener - Listener to receive server callbacks
     */
    void loadTabTitle(@NonNull String url, @NonNull OnLoadingListener<Integer> loadingListener);

    /**
     * {@inheritDoc}
     */
    void load(@NonNull String url, @NonNull OnLoadingListener<List<Changes.Change>> loadingListener);

    /**
     * Load more changes
     *
     * @param loadingListener - Listener to receive server callbacks
     */
    void loadMore(@NonNull OnLoadingListener<List<Changes.Change>> loadingListener);

    /**
     * Is it possible to load more
     */
    boolean canLoadMore();

    /**
     * Post change tab title event to {@link de.greenrobot.event.EventBus}
     * @param size - Size of items to update in the title
     */
    void postChangeTabTitleEvent(Integer size);
}
