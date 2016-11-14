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

package com.github.vase4kin.teamcityapp.navigation.data;

import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.api.Project;

import java.util.List;

/**
 * Impl of {@link NavigationDataModel}
 */
public class NavigationDataModelImpl implements NavigationDataModel {

    private List<NavigationItem> mItems;

    public NavigationDataModelImpl(List<NavigationItem> items) {
        this.mItems = items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(int position) {
        return mItems.get(position).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription(int position) {
        return mItems.get(position).getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDescription(int position) {
        return mItems.get(position).getDescription() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProject(int position) {
        return mItems.get(position) instanceof Project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigationItem getNavigationItem(int position) {
        return mItems.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
