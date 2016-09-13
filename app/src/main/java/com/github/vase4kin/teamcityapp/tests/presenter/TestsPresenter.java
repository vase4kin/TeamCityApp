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

package com.github.vase4kin.teamcityapp.tests.presenter;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenter;

/**
 * Presenter to handle logic of {@link com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment}
 */
public interface TestsPresenter extends BaseListPresenter {

    /**
     * {@inheritDoc}
     * <p>
     * See {@link android.support.v4.app.Fragment#onCreateOptionsMenu(Menu, MenuInflater)} )}
     */
    void onCreateOptionsMenu(Menu menu, MenuInflater inflater);

    /**
     * {@inheritDoc}
     *
     * See {@link android.support.v4.app.Fragment#onPrepareOptionsMenu(Menu)} )}
     */
    void onPrepareOptionsMenu(Menu menu);

    /**
     * {@inheritDoc}
     *
     * See {@link android.support.v4.app.Fragment#onOptionsItemSelected(MenuItem)}
     */
    boolean onOptionsItemSelected(MenuItem item);
}
