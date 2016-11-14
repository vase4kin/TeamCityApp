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

package com.github.vase4kin.teamcityapp.properties.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataManager;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModelImpl;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter handles logic of {@link com.github.vase4kin.teamcityapp.properties.view.PropertiesFragment}
 */
public class PropertiesPresenterImpl extends BaseListPresenterImpl<
        PropertiesDataModel,
        Properties.Property,
        BaseListView,
        PropertiesDataManager,
        ViewTracker,
        BaseValueExtractor> {

    @Inject
    PropertiesPresenterImpl(@NonNull BaseListView view,
                            @NonNull PropertiesDataManager dataManager,
                            @NonNull ViewTracker tracker,
                            @NonNull BaseValueExtractor valueExtractor) {
        super(view, dataManager, tracker, valueExtractor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<Properties.Property>> loadingListener) {
        mDataManager.load(mValueExtractor.getBuild(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertiesDataModel createModel(List<Properties.Property> data) {
        return new PropertiesDataModelImpl(data);
    }
}
