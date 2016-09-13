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

package com.github.vase4kin.teamcityapp.testdetails.view;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Custom selection callback listener
 */
public class CustomSelectionActionModeCallBackImpl implements ActionMode.Callback {

    private OnActionModeListener mOnActionModeListener;

    public CustomSelectionActionModeCallBackImpl(OnActionModeListener onActionModeListener) {
        this.mOnActionModeListener = onActionModeListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mOnActionModeListener.onCreate();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mOnActionModeListener.onDestroy();
    }
}
