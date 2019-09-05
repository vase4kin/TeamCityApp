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

package com.github.vase4kin.teamcityapp.base.presenter

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import androidx.fragment.app.Fragment

/**
 * Base menu presenter to handle logic of menu
 */
interface BaseMenuPresenter {

    /**
     * {@inheritDoc}
     *
     * See [Fragment.onCreateOptionsMenu] )}
     */
    fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * {@inheritDoc}
     *
     * See [Fragment.onPrepareOptionsMenu] )}
     */
    fun onPrepareOptionsMenu(menu: Menu)

    /**
     * {@inheritDoc}
     *
     * See [Fragment.onOptionsItemSelected]
     */
    fun onOptionsItemSelected(item: MenuItem): Boolean
}
