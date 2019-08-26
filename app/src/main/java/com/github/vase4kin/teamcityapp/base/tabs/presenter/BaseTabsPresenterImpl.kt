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

package com.github.vase4kin.teamcityapp.base.tabs.presenter

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEventListener
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker

import javax.inject.Inject

/**
 * Base impl for [BaseTabsPresenter]
 */
open class BaseTabsPresenterImpl<T : BaseTabsViewModel, DT : BaseTabsDataManager, TR : ViewTracker> @Inject
constructor(
    protected var view: T,
    protected var tracker: TR,
    protected var interactor: DT
) : BaseTabsPresenter, OnTextTabChangeEventListener {

    /**
     * {@inheritDoc}
     */
    override fun onViewsCreated() {
        view.initViews()
    }

    /**
     * {@inheritDoc}
     */
    override fun onViewsDestroyed() {
        view.unBindViews()
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        interactor.registerEventBus()
        interactor.setListener(this)
        tracker.trackView()
    }

    /**
     * {@inheritDoc}
     */
    override fun onPause() {
        interactor.unregisterEventBus()
    }

    /**
     * {@inheritDoc}
     */
    override fun onUpdateTabTitle(tabPosition: Int, newTitle: String) {
        view.updateTabTitle(tabPosition, newTitle)
    }
}
