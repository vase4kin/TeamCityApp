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

package com.github.vase4kin.teamcityapp.queue.view;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListViewImpl;

/**
 * View to manage build queue interactions
 */
public class BuildQueueViewImpl extends RunningBuildsListViewImpl {

    public BuildQueueViewImpl(View mView, Activity activity, @StringRes int emptyMessage) {
        super(mView, activity, emptyMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTitle() {
        return mActivity.getString(R.string.build_queue_drawer_item);
    }
}
