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

package com.github.vase4kin.teamcityapp.buildlist.view;

import com.github.vase4kin.teamcityapp.base.list.adapter.ViewLoadMore;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;

/**
 * View for handling {@link BuildListActivity}
 */
public interface BuildListView extends BaseListView<BuildListDataModel>, ViewLoadMore<BuildListDataModel> {

    /**
     * @param mOnBuildListPresenterListener - Listener to handle view callbacks
     */
    void setOnBuildListPresenterListener(OnBuildListPresenterListener mOnBuildListPresenterListener);

    /**
     * Show retry load more snack bar
     */
    void showRetryLoadMoreSnackBar();

    /**
     * Set toolbar title
     *
     * @param title - Title
     */
    void setTitle(String title);

    /**
     * Show float action button
     */
    void showRunBuildFloatActionButton();

    /**
     * Hide float action button
     */
    void hideRunBuildFloatActionButton();

    /**
     * Show build queued success snack bar
     */
    void showBuildQueuedSuccessSnackBar();

    /**
     * Show error opening build snack bar
     */
    void showOpeningBuildErrorSnackBar();

    /**
     * Show build loading progress
     */
    void showBuildLoadingProgress();

    /**
     * Hide build loading progress
     */
    void hideBuildLoadingProgress();
}
