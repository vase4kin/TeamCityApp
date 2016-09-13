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

package com.github.vase4kin.teamcityapp.overview.data;

import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;

import java.util.List;

/**
 * Impl of {@link OverviewDataModel}
 */
public class OverviewDataModelImpl implements OverviewDataModel {

    private List<BuildElement> elements;

    public OverviewDataModelImpl(List<BuildElement> elements) {
        this.elements = elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIcon(int position) {
        return elements.get(position).getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription(int position) {
        return elements.get(position).getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeaderName(int position) {
        return elements.get(position).getSectionName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return elements.size();
    }
}
