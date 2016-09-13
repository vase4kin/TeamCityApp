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

package com.github.vase4kin.teamcityapp.changes.view;

import com.github.vase4kin.teamcityapp.base.list.adapter.LoadMore;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.buildlist.view.OnLoadMoreListener;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;

/**
 * View interactions of {@link ChangesFragment}
 */
public interface ChangesView extends BaseListView<ChangesDataModel>, LoadMore<ChangesDataModel> {

    /**
     * @param mOnLoadMoreListener - Listener to receive load more callbacks
     */
    void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener);

    /**
     * {@inheritDoc}
     */
    void addLoadMoreItem();

    /**
     * {@inheritDoc}
     */
    void removeLoadMoreItem();

    /**
     * {@inheritDoc}
     */
    void addMoreBuilds(ChangesDataModel dataModel);

    void showRetryLoadMoreSnackBar();
}
