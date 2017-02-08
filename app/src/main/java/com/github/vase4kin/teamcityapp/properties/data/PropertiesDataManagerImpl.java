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

package com.github.vase4kin.teamcityapp.properties.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.properties.api.Properties;

import java.util.Collections;
import java.util.List;

/**
 * Impl of {@link PropertiesDataManager}
 */
public class PropertiesDataManagerImpl extends BaseListRxDataManagerImpl<Properties, Properties.Property> implements PropertiesDataManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull BuildDetails buildDetails, OnLoadingListener<List<Properties.Property>> loadingListener) {
        // Getting properties from the build
        Properties properties = buildDetails.getProperties();
        if (properties == null) {
            // Return empty collections if properties are empty
            Properties emptyProperties = new Properties() {
                @Override
                public List<Property> getObjects() {
                    return Collections.emptyList();
                }
            };
            loadingListener.onSuccess(emptyProperties.getObjects());
        } else {
            loadingListener.onSuccess(buildDetails.getProperties().getObjects());
        }
    }
}
