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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items;

import android.content.Context;
import android.support.annotation.StringRes;

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetItem;

import java.util.List;

/**
 * Impl of {@link MenuItemsFactory}
 */
public abstract class BaseMenuItemsFactory implements MenuItemsFactory {

    private final Context context;
    private final String description;

    BaseMenuItemsFactory(Context context, String description) {
        this.context = context;
        this.description = description;
    }

    @Override
    public abstract List<BottomSheetItem> createMenuItems();

    protected String getString(@StringRes int title) {
        return context.getString(title);
    }

    public Context getContext() {
        return context;
    }

    public String getDescription() {
        return description;
    }
}
