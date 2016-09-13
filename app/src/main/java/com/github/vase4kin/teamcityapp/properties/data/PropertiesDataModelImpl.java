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

import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.properties.api.Properties;

import java.util.List;

/**
 * Impl of {@link PropertiesDataModel}
 */
public class PropertiesDataModelImpl implements PropertiesDataModel {

    @VisibleForTesting
    static final String EMPTY = "Empty";

    private List<Properties.Property> mProperties;

    public PropertiesDataModelImpl(List<Properties.Property> properties) {
        this.mProperties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(int position) {
        return mProperties.get(position).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(int position) {
        String value = mProperties.get(position).getValue();
        return !value.isEmpty()
                ? value
                : EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty(int position) {
        return getValue(position).equals(EMPTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mProperties.size();
    }
}
