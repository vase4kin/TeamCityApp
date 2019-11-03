/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.features.test_details.feature.view

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

/**
 * Custom selection callback listener
 */
class CustomSelectionActionModeCallBackImpl(
    private val onActionModeListener: OnActionModeListener
) : ActionMode.Callback {

    /**
     * {@inheritDoc}
     */
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        onActionModeListener.onCreate()
        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    /**
     * {@inheritDoc}
     */
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return false
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroyActionMode(mode: ActionMode) {
        onActionModeListener.onDestroy()
    }
}
