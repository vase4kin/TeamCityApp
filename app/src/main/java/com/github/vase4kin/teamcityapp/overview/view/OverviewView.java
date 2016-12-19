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

package com.github.vase4kin.teamcityapp.overview.view;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel;

public interface OverviewView extends BaseListView<OverviewDataModel> {

    /**
     * Set {@link OverviewViewListener}
     *
     * @param listener - listener to receive UI updates
     */
    void setOverViewListener(OverviewViewListener listener);

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onCreateOptionsMenu(Menu, MenuInflater)} )}
     */
    void createStopBuildOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onCreateOptionsMenu(Menu, MenuInflater)} )}
     */
    void createRemoveBuildFromQueueOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * Show options menu
     *
     * @param menu - Menu options
     */
    void showOptionsMenu(Menu menu);

    /**
     * Hide options menu
     *
     * @param menu - Menu options
     */
    void hideOptionsMenu(Menu menu);

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onOptionsItemSelected(MenuItem)}
     */
    boolean onOptionsItemSelected(MenuItem item);

    /**
     * Listener to handle interactions between view and presenter
     */
    interface OverviewViewListener {
        /**
         * On stop build context menu clicked
         */
        void onCancelBuildContextMenuClick();
    }
}
