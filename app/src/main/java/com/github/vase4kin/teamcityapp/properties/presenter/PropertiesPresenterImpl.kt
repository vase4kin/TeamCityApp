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

package com.github.vase4kin.teamcityapp.properties.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.properties.api.Properties
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataManager
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModelImpl
import com.github.vase4kin.teamcityapp.properties.data.PropertiesValueExtractor
import com.github.vase4kin.teamcityapp.properties.view.PropertiesView
import javax.inject.Inject

/**
 * Presenter handles logic of [com.github.vase4kin.teamcityapp.properties.view.PropertiesFragment]
 */
class PropertiesPresenterImpl @Inject constructor(
    view: PropertiesView,
    dataManager: PropertiesDataManager,
    tracker: ViewTracker,
    valueExtractor: PropertiesValueExtractor
) : BaseListPresenterImpl<PropertiesDataModel, Properties.Property, PropertiesView, PropertiesDataManager, ViewTracker, PropertiesValueExtractor>(view, dataManager, tracker, valueExtractor), PropertiesView.Listener {

    override fun initViews() {
        view.setListener(this)
        super.initViews()
    }

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<Properties.Property>>, update: Boolean) {
        dataManager.load(valueExtractor.buildDetails, loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: List<Properties.Property>): PropertiesDataModel {
        return PropertiesDataModelImpl(data)
    }

    /**
     * {@inheritDoc}
     */
    override fun onCardClick(header: String, value: String) {
        view.showCopyValueBottomSheet(header, value)
    }
}
