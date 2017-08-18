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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.model;

import java.util.List;

/**
 * impl of {@link BottomSheetDataModel}
 */
public class BottomSheetDataModelImpl implements BottomSheetDataModel {

    private final List<BottomSheetItem> items;

    public BottomSheetDataModelImpl(List<BottomSheetItem> items) {
        this.items = items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(int position) {
        return items.get(position).getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return items.size();
    }
}
